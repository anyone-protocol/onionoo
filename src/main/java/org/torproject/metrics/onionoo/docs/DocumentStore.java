/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.docs;

import org.torproject.metrics.onionoo.docs.onionperf.*;
import org.torproject.metrics.onionoo.docs.userstats.MergedStatus;
import org.torproject.metrics.onionoo.util.FormattingUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

// TODO For later migration from disk to database, do the following:
// - read from database and then from disk if not found
// - write only to database, delete from disk once in database
// - move entirely to database once disk is "empty"
// TODO Also look into simple key-value stores instead of real databases.
public class DocumentStore {

  private static final Logger logger = LoggerFactory.getLogger(
      DocumentStore.class);

  private static ObjectMapper objectMapper = new ObjectMapper();

  private final File statusDir = new File("data/status");

  private File outDir = null;

  public void setOutDir(File outDir) {
    this.outDir = outDir;
  }

  private long listOperations = 0L;

  private long listedFiles = 0L;

  private long storedFiles = 0L;

  private long storedBytes = 0L;

  private long unchangedFiles = 0L;

  private long unchangedBytes = 0L;

  private long retrievedFiles = 0L;

  private long retrievedBytes = 0L;

  private long removedFiles = 0L;

  /* Node statuses and summary documents are cached in memory, as opposed
   * to all other document types.  These caches are initialized when first
   * accessing or modifying a NodeStatus or SummaryDocument document,
   * respectively. */
  private SortedMap<String, NodeStatus> cachedNodeStatuses;

  private SortedMap<String, SummaryDocument> cachedSummaryDocuments;

  /* Last-modified timestamp of cached network statuses and summary
   * documents when reading them from disk. */
  private long lastModifiedNodeStatuses = 0L;

  private long lastModifiedSummaryDocuments = 0L;

  /* Fingerprints of updated node statuses and summary documents that are
   * not yet written to disk. */
  private SortedSet<String> updatedNodeStatuses;

  private SortedSet<String> updatedSummaryDocuments;

  public <T extends Document> SortedSet<String> list(
      Class<T> documentType) {
    return this.list(documentType, 0L);
  }

  /** Returns all fingerprints of documents of the given type that have
   * been updated after the given time in milliseconds since the epoch. */
  public <T extends Document> SortedSet<String> list(
      Class<T> documentType, long updatedAfter) {
    if (documentType.equals(NodeStatus.class)) {
      return this.listNodeStatuses(updatedAfter);
    } else if (documentType.equals(SummaryDocument.class)) {
      return this.listSummaryDocuments(updatedAfter);
    } else {
      return this.listDocumentFiles(documentType, updatedAfter);
    }
  }

  private SortedSet<String> listNodeStatuses(long updatedAfter) {
    if (this.cachedNodeStatuses == null) {
      this.cacheNodeStatuses();
    }
    if (updatedAfter >= this.lastModifiedNodeStatuses) {
      return new TreeSet<>(this.updatedNodeStatuses);
    } else {
      return new TreeSet<>(this.cachedNodeStatuses.keySet());
    }
  }

  private void cacheNodeStatuses() {
    SortedMap<String, NodeStatus> parsedNodeStatuses = new TreeMap<>();
    File directory = this.statusDir;
    if (directory != null) {
      File summaryFile = new File(directory, "summary");
      if (summaryFile.exists()) {
        try (BufferedReader br = new BufferedReader(new FileReader(
            summaryFile))) {
          String line;
          while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
              continue;
            }
            NodeStatus node = NodeStatus.fromString(line);
            if (node != null) {
              parsedNodeStatuses.put(node.getFingerprint(), node);
            }
          }
          this.lastModifiedNodeStatuses = summaryFile.lastModified();
          this.listedFiles += parsedNodeStatuses.size();
          this.listOperations++;
        } catch (IOException e) {
          logger.error("Could not read file '{}'.",
              summaryFile.getAbsolutePath(), e);
        }
      }
    }
    this.cachedNodeStatuses = parsedNodeStatuses;
    this.updatedNodeStatuses = new TreeSet<>();
  }

  private SortedSet<String> listSummaryDocuments(long updatedAfter) {
    if (this.cachedSummaryDocuments == null) {
      this.cacheSummaryDocuments();
    }
    if (updatedAfter >= this.lastModifiedSummaryDocuments) {
      return new TreeSet<>(this.updatedSummaryDocuments);
    } else {
      return new TreeSet<>(this.cachedSummaryDocuments.keySet());
    }
  }

  private void cacheSummaryDocuments() {
    SortedMap<String, SummaryDocument> parsedSummaryDocuments = new TreeMap<>();
    if (this.outDir != null) {
      File summaryFile = new File(this.outDir, "summary");
      if (summaryFile.exists()) {
        String line = null;
        try (BufferedReader br = new BufferedReader(new FileReader(
            summaryFile))) {
          while ((line = br.readLine()) != null) {
            if (line.length() == 0) {
              continue;
            }
            SummaryDocument summaryDocument = objectMapper.readValue(line,
                SummaryDocument.class);
            if (summaryDocument != null) {
              parsedSummaryDocuments.put(summaryDocument.getFingerprint(),
                  summaryDocument);
            }
          }
          this.lastModifiedSummaryDocuments = summaryFile.lastModified();
          this.listedFiles += parsedSummaryDocuments.size();
          this.listOperations++;
        } catch (IOException e) {
          logger.error("Could not parse summary document '{}' from file '{}'.",
              line, summaryFile.getAbsolutePath(), e);
        }
      }
    }
    this.cachedSummaryDocuments = parsedSummaryDocuments;
    this.updatedSummaryDocuments = new TreeSet<>();
  }

  private <T extends Document> SortedSet<String> listDocumentFiles(
      Class<T> documentType, long updatedAfter) {
    SortedSet<String> fingerprints = new TreeSet<>();
    File directory = null;
    String subdirectory = null;
    if (documentType.equals(DetailsStatus.class)) {
      directory = this.statusDir;
      subdirectory = "details";
    } else if (documentType.equals(BandwidthStatus.class)) {
      directory = this.statusDir;
      subdirectory = "bandwidth";
    } else if (documentType.equals(WeightsStatus.class)) {
      directory = this.statusDir;
      subdirectory = "weights";
    } else if (documentType.equals(ClientsStatus.class)) {
      directory = this.statusDir;
      subdirectory = "clients";
    } else if (documentType.equals(UptimeStatus.class)) {
      directory = this.statusDir;
      subdirectory = "uptimes";
    } else if (documentType.equals(DetailsDocument.class)) {
      directory = this.outDir;
      subdirectory = "details";
    } else if (documentType.equals(BandwidthDocument.class)) {
      directory = this.outDir;
      subdirectory = "bandwidth";
    } else if (documentType.equals(WeightsDocument.class)) {
      directory = this.outDir;
      subdirectory = "weights";
    } else if (documentType.equals(ClientsDocument.class)) {
      directory = this.outDir;
      subdirectory = "clients";
    } else if (documentType.equals(UptimeDocument.class)) {
      directory = this.outDir;
      subdirectory = "uptimes";
    }
    if (directory != null && subdirectory != null) {
      Stack<File> files = new Stack<>();
      files.add(new File(directory, subdirectory));
      while (!files.isEmpty()) {
        File file = files.pop();
        if (file.isDirectory()) {
          files.addAll(Arrays.asList(file.listFiles()));
        } else if (file.getName().length() == 40
            && (updatedAfter == 0L
            || file.lastModified() > updatedAfter)) {
          fingerprints.add(file.getName());
        }
      }
    }
    this.listOperations++;
    this.listedFiles += fingerprints.size();
    return fingerprints;
  }

  public <T extends Document> boolean store(T document) {
    return this.store(document, null);
  }

  /** Stores the given document using the given fingerprint as
   * identifier. */
  public <T extends Document> boolean store(T document,
      String fingerprint) {
    if (document instanceof NodeStatus) {
      return this.storeNodeStatus((NodeStatus) document, fingerprint);
    } else if (document instanceof SummaryDocument) {
      return this.storeSummaryDocument((SummaryDocument) document,
          fingerprint);
    } else {
      return this.storeDocumentFile(document, fingerprint);
    }
  }

  private <T extends Document> boolean storeNodeStatus(
      NodeStatus nodeStatus, String fingerprint) {
    if (this.cachedNodeStatuses == null) {
      this.cacheNodeStatuses();
    }
    this.updatedNodeStatuses.add(fingerprint);
    this.cachedNodeStatuses.put(fingerprint, nodeStatus);
    return true;
  }

  private <T extends Document> boolean storeSummaryDocument(
      SummaryDocument summaryDocument, String fingerprint) {
    if (this.cachedSummaryDocuments == null) {
      this.cacheSummaryDocuments();
    }
    this.updatedSummaryDocuments.add(fingerprint);
    this.cachedSummaryDocuments.put(fingerprint, summaryDocument);
    return true;
  }

  private static final long ONE_BYTE = 1L;

  private static final long ONE_KIBIBYTE = 1024L * ONE_BYTE;

  private static final long ONE_MIBIBYTE = 1024L * ONE_KIBIBYTE;

  private <T extends Document> boolean storeDocumentFile(T document,
      String fingerprint) {
    File documentFile = this.getDocumentFile(document.getClass(),
        fingerprint);
    if (documentFile == null) {
      return false;
    }
    String documentString;
    if (document.getDocumentString() != null) {
      documentString = document.getDocumentString();
    } else if (document instanceof DetailsStatus
        || document instanceof DetailsDocument
        || document instanceof BandwidthDocument
        || document instanceof HardwareInfoDocument
        || document instanceof WeightsDocument
        || document instanceof ClientsDocument
        || document instanceof OnionperfStatus
        || document instanceof UserStatsStatus
        || document instanceof UptimeDocument) {
      try {
        documentString = objectMapper.writeValueAsString(document);
      } catch (JsonProcessingException e) {
        logger.error("Serializing failed for type {}.",
            document.getClass().getName(), e);
        return false;
      }
    } else if (document instanceof BandwidthStatus
        || document instanceof WeightsStatus
        || document instanceof ClientsStatus
        || document instanceof UptimeStatus
        || document instanceof MergedStatus
        || document instanceof UpdateStatus) {
      documentString = document.toDocumentString();
    } else {
      logger.error("Serializing is not supported for type {}.",
          document.getClass().getName());
      return false;
    }
    try {
      if (documentString.length() > ONE_MIBIBYTE) {
        logger.info("Attempting to store very large document file: path='{}', "
            + "bytes={}", documentFile.getAbsolutePath(),
            documentString.length());
      }
      documentFile.getParentFile().mkdirs();
      if (documentFile.exists()) {
        try (InputStream stream = Files.newInputStream(documentFile.toPath())) {
          String existingFileDigest = DigestUtils.sha256Hex(stream);
          String newFileDigest = DigestUtils.sha256Hex(documentString);
          if (existingFileDigest.equals(newFileDigest)) {
            this.unchangedFiles++;
            this.unchangedBytes += documentString.length();
            return true;
          }
        }
      }
      File documentTempFile = new File(
          documentFile.getAbsolutePath() + ".tmp");
      writeToFile(documentTempFile, documentString);
      documentFile.delete();
      documentTempFile.renameTo(documentFile);
      this.storedFiles++;
      this.storedBytes += documentString.length();
    } catch (IOException e) {
      logger.error("Could not write file '{}'.", documentFile.getAbsolutePath(),
          e);
      return false;
    }
    return true;
  }

  public <T extends Document> T retrieve(Class<T> documentType,
      boolean parse) {
    return this.retrieve(documentType, parse, null);
  }

  /** Retrieves the document with given type and identified by the given
   * fingerprint, and either parses it or returns it unparsed. */
  public <T extends Document> T retrieve(Class<T> documentType,
      boolean parse, String fingerprint) {
    if (documentType.equals(NodeStatus.class)) {
      return documentType.cast(this.retrieveNodeStatus(fingerprint));
    } else if (documentType.equals(SummaryDocument.class)) {
      return documentType.cast(this.retrieveSummaryDocument(fingerprint));
    } else {
      return this.retrieveDocumentFile(documentType, parse, fingerprint);
    }
  }

  private NodeStatus retrieveNodeStatus(String fingerprint) {
    if (this.cachedNodeStatuses == null) {
      this.cacheNodeStatuses();
    }
    return this.cachedNodeStatuses.get(fingerprint);
  }

  private SummaryDocument retrieveSummaryDocument(String fingerprint) {
    if (this.cachedSummaryDocuments == null) {
      this.cacheSummaryDocuments();
    }
    if (this.cachedSummaryDocuments.containsKey(fingerprint)) {
      return this.cachedSummaryDocuments.get(fingerprint);
    }
    /* TODO This is an evil hack to support looking up relays or bridges
     * that haven't been running for a week without having to load
     * 500,000 NodeStatus instances into memory.  Maybe there's a better
     * way?  Or do we need to switch to a real database for this? */
    DetailsDocument detailsDocument = this.retrieveDocumentFile(
        DetailsDocument.class, true, fingerprint);
    if (detailsDocument == null) {
      /* There is no details document available that we could serve as
       * basis for generating a summary document on-the-fly.  Nothing to
       * worry about. */
      return null;
    }
    boolean isRelay = detailsDocument.getHashedFingerprint() == null;
    boolean running = false;
    String nickname = detailsDocument.getNickname();
    List<String> addresses = new ArrayList<>();
    String countryCode = null;
    String asNumber = null;
    String asName = null;
    String contact = null;
    for (String orAddressAndPort : detailsDocument.getOrAddresses()) {
      if (!orAddressAndPort.contains(":")) {
        logger.warn("Attempt to create summary document from details document "
            + "for fingerprint {} failed because of invalid OR address/port: "
            + "'{}'. Not returning a summary document in this case.",
            fingerprint, orAddressAndPort);
        return null;
      }
      String orAddress = orAddressAndPort.substring(0,
          orAddressAndPort.lastIndexOf(":"));
      if (!addresses.contains(orAddress)) {
        addresses.add(orAddress);
      }
    }
    if (detailsDocument.getExitAddresses() != null) {
      for (String exitAddress : detailsDocument.getExitAddresses()) {
        if (!addresses.contains(exitAddress)) {
          addresses.add(exitAddress);
        }
      }
    }
    SortedSet<String> relayFlags = new TreeSet<>();
    SortedSet<String> family = null;
    String version = null;
    String operatingSystem = null;
    long lastSeenMillis = -1L;
    long consensusWeight = -1L;
    long firstSeenMillis = -1L;
    SortedSet<String> verifiedHostNames = null;
    SortedSet<String> unverifiedHostNames = null;
    Boolean recommendedVersion = null;
    Boolean overloadStatus = null;
    List<String> transports = null;
    return new SummaryDocument(isRelay,
        nickname, fingerprint, addresses, lastSeenMillis, running,
        relayFlags, consensusWeight, countryCode, firstSeenMillis,
        asNumber, asName, contact, family, version, operatingSystem,
        verifiedHostNames, unverifiedHostNames,
        recommendedVersion, overloadStatus, transports);
  }

  private <T extends Document> T retrieveDocumentFile(
      Class<T> documentType, boolean parse, String fingerprint) {
    File documentFile = this.getDocumentFile(documentType, fingerprint);
    if (documentFile == null || !documentFile.exists()) {
      /* Document file does not exist.  That's okay. */
      return null;
    } else if (documentFile.isDirectory()) {
      logger.error("Could not read file '{}', because it is a directory.",
          documentFile.getAbsolutePath());
      return null;
    }
    String documentString;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedInputStream bis = new BufferedInputStream(
        new FileInputStream(documentFile))) {
      int len;
      byte[] data = new byte[1024];
      while ((len = bis.read(data, 0, 1024)) >= 0) {
        baos.write(data, 0, len);
      }
      byte[] allData = baos.toByteArray();
      if (allData.length == 0) {
        /* Document file is empty. */
        return null;
      }
      documentString = new String(allData, StandardCharsets.UTF_8);
      this.retrievedFiles++;
      this.retrievedBytes += documentString.length();
    } catch (IOException e) {
      logger.error("Could not read file '{}'.", documentFile.getAbsolutePath(),
          e);
      return null;
    }
    if (documentString.length() > ONE_MIBIBYTE) {
      logger.info("Retrieved very large document file: path='{}', bytes={}",
          documentFile.getAbsolutePath(), documentString.length());
    }
    T result = null;
    if (!parse) {
      return this.retrieveUnparsedDocumentFile(documentType,
          documentString);
    } else if (documentType.equals(DetailsDocument.class)
        || documentType.equals(DetailsStatus.class)) {
      /* Details statuses and documents written by older versions had UTF-8
       * characters escaped as \\u1234 in order to store them encoded as ASCII.
       * This was undone in memory in getters and setters. This version does not
       * escape or unescape UTF-8 characters in memory anymore, but it expects
       * files to either use UTF-8 encoding or to contain valid escaped UTF-8
       * characters like \u1234. Further, details statuses written by older
       * versions did not start and end with braces. Fixing both things here. */
      StringBuilder documentStringBuilder = new StringBuilder();
      if (!documentString.startsWith("{")) {
        documentStringBuilder.append('{');
      }
      documentStringBuilder.append(documentString.replaceAll(
          "\\\\(\\\\u[0-9a-fA-F]{4})", "$1"));
      if (!documentString.endsWith("}")) {
        documentStringBuilder.append('}');
      }
      return this.retrieveParsedDocumentFile(documentType,
          documentStringBuilder.toString());
    } else if (documentType.equals(BandwidthDocument.class)
        || documentType.equals(WeightsDocument.class)
        || documentType.equals(HardwareInfoDocument.class)
        || documentType.equals(ClientsDocument.class)
        || documentType.equals(OnionperfStatus.class)
        || documentType.equals(UptimeDocument.class)) {
      return this.retrieveParsedDocumentFile(documentType,
          documentString);
    } else if (documentType.equals(BandwidthStatus.class)
        || documentType.equals(WeightsStatus.class)
        || documentType.equals(ClientsStatus.class)
        || documentType.equals(UptimeStatus.class)
        || documentType.equals(MergedStatus.class)
        || documentType.equals(UpdateStatus.class)) {
      return this.retrieveParsedStatusFile(documentType, documentString);
    } else {
      logger.error("Parsing is not supported for type {}.",
          documentType.getName());
    }
    return result;
  }

  private <T extends Document> T retrieveParsedStatusFile(
      Class<T> documentType, String documentString) {
    T result = null;
    try {
      result = documentType.getDeclaredConstructor().newInstance();
      result.setFromDocumentString(documentString);
    } catch (ReflectiveOperationException e) {
      /* Handle below. */
      logger.error(e.getMessage(), e);
    }
    if (result == null) {
      logger.error("Could not initialize parsed status file of type {}.",
          documentType.getName());
    }
    return result;
  }

  private <T extends Document> T retrieveParsedDocumentFile(
      Class<T> documentType, String documentString) {
    T result = null;
    try {
      if (documentString != null && !documentString.isEmpty() && !documentString.equals("{}")) {
        result = objectMapper.readValue(documentString, documentType);
      }
    } catch (Throwable e) {
      /* Handle below. */
      logger.error(documentString);
      logger.error(e.getMessage(), e);
    }
    if (result == null) {
      logger.error("Could not initialize parsed document of type {}.",
          documentType.getName());
    }
    return result;
  }

  private <T extends Document> T retrieveUnparsedDocumentFile(
      Class<T> documentType, String documentString) {
    T result = null;
    try {
      result = documentType.getDeclaredConstructor().newInstance();
      result.setDocumentString(documentString);
    } catch (ReflectiveOperationException e) {
      /* Handle below. */
      logger.error(e.getMessage(), e);
    }
    if (result == null) {
      logger.error("Could not initialize unparsed document of type {}.",
          documentType.getName());
    }
    return result;
  }

  public <T extends Document> boolean remove(Class<T> documentType) {
    return this.remove(documentType, null);
  }

  /** Removes the document with given type and identified by the given
   * fingerprint. */
  public <T extends Document> boolean remove(Class<T> documentType,
      String fingerprint) {
    if (documentType.equals(NodeStatus.class)) {
      return this.removeNodeStatus(fingerprint);
    } else if (documentType.equals(SummaryDocument.class)) {
      return this.removeSummaryDocument(fingerprint);
    } else {
      return this.removeDocumentFile(documentType, fingerprint);
    }
  }

  private boolean removeNodeStatus(String fingerprint) {
    if (this.cachedNodeStatuses == null) {
      this.cacheNodeStatuses();
    }
    this.updatedNodeStatuses.remove(fingerprint);
    return this.cachedNodeStatuses.remove(fingerprint) != null;
  }

  private boolean removeSummaryDocument(String fingerprint) {
    if (this.cachedSummaryDocuments == null) {
      this.cacheSummaryDocuments();
    }
    this.updatedSummaryDocuments.remove(fingerprint);
    return this.cachedSummaryDocuments.remove(fingerprint) != null;
  }

  private <T extends Document> boolean removeDocumentFile(
      Class<T> documentType, String fingerprint) {
    File documentFile = this.getDocumentFile(documentType, fingerprint);
    if (documentFile == null || !documentFile.delete()) {
      logger.error("Could not delete file '{}'.",
          documentFile.getAbsolutePath());
      return false;
    }
    this.removedFiles++;
    return true;
  }

  private <T extends Document> File getDocumentFile(Class<T> documentType,
      String fingerprint) {
    File documentFile = null;
    if (fingerprint == null && !documentType.equals(UpdateStatus.class)
        && !documentType.equals(OnionperfStatus.class)
        && !documentType.equals(MergedStatus.class)
        && !documentType.equals(UserStatsStatus.class)
        && !documentType.equals(CircuitDocument.class)
        && !documentType.equals(DownloadDocument.class)
        && !documentType.equals(FailureDocument.class)
        && !documentType.equals(LatencyDocument.class)
        && !documentType.equals(ThroughputDocument.class)
        && !documentType.equals(UptimeStatus.class)) {
      logger.warn("Attempted to locate a document file of type {} without "
          + "providing a fingerprint.  Such a file does not exist.",
          documentType.getName());
      return null;
    }
    File directory = null;
    String fileName = null;
    if (documentType.equals(DetailsStatus.class)) {
      directory = this.statusDir;
      fileName = String.format("details/%s/%s/%s",
          fingerprint.substring(0, 1), fingerprint.substring(1, 2),
          fingerprint);
    } else if (documentType.equals(BandwidthStatus.class)) {
      directory = this.statusDir;
      fileName = String.format("bandwidth/%s/%s/%s",
          fingerprint.substring(0, 1), fingerprint.substring(1, 2),
          fingerprint);
    } else if (documentType.equals(WeightsStatus.class)) {
      directory = this.statusDir;
      fileName = String.format("weights/%s/%s/%s",
          fingerprint.substring(0, 1), fingerprint.substring(1, 2),
          fingerprint);
    } else if (documentType.equals(ClientsStatus.class)) {
      directory = this.statusDir;
      fileName = String.format("clients/%s/%s/%s",
          fingerprint.substring(0, 1), fingerprint.substring(1, 2),
          fingerprint);
    } else if (documentType.equals(UptimeStatus.class)) {
      directory = this.statusDir;
      if (fingerprint == null) {
        fileName = "uptime";
      } else {
        fileName = String.format("uptimes/%s/%s/%s",
            fingerprint.substring(0, 1), fingerprint.substring(1, 2),
            fingerprint);
      }
    } else if (documentType.equals(OnionperfStatus.class)) {
      directory = this.statusDir;
      fileName = "performance";
    } else if (documentType.equals(MergedStatus.class)) {
      directory = this.statusDir;
      fileName = "merged";
    } else if (documentType.equals(UserStatsStatus.class)) {
      directory = this.statusDir;
      fileName = "userstats";
    } else if (documentType.equals(UpdateStatus.class)) {
      directory = this.outDir;
      fileName = "update";
    } else if (documentType.equals(DetailsDocument.class)) {
      directory = this.outDir;
      fileName = String.format("details/%s", fingerprint);
    } else if (documentType.equals(BandwidthDocument.class)) {
      directory = this.outDir;
      fileName = String.format("bandwidth/%s", fingerprint);
    } else if (documentType.equals(HardwareInfoDocument.class)) {
      directory = this.outDir;
      fileName = String.format("hardware/%s", fingerprint);
    } else if (documentType.equals(WeightsDocument.class)) {
      directory = this.outDir;
      fileName = String.format("weights/%s", fingerprint);
    } else if (documentType.equals(ClientsDocument.class)) {
      directory = this.outDir;
      fileName = String.format("clients/%s", fingerprint);
    } else if (documentType.equals(UptimeDocument.class)) {
      directory = this.outDir;
      fileName = String.format("uptimes/%s", fingerprint);
    } else if (documentType.equals(CircuitDocument.class)) {
      directory = this.outDir;
      fileName = "performance/circuit.csv";
    } else if (documentType.equals(DownloadDocument.class)) {
      directory = this.outDir;
      fileName = "performance/download.csv";
    } else if (documentType.equals(FailureDocument.class)) {
      directory = this.outDir;
      fileName = "performance/failure.csv";
    } else if (documentType.equals(LatencyDocument.class)) {
      directory = this.outDir;
      fileName = "performance/latency.csv";
    } else if (documentType.equals(ThroughputDocument.class)) {
      directory = this.outDir;
      fileName = "performance/throughput.csv";
    }
    if (directory != null && fileName != null) {
      documentFile = new File(directory, fileName);
    }
    return documentFile;
  }

  /** Writes cached node statuses, cached summary documents, and then the
   * update file to disk. */
  public void flushDocumentCache() {
    /* Write cached node statuses to disk, and write update file
     * containing current time.  It's important to write the update file
     * now, not earlier, because the front-end should not read new node
     * statuses until all details, bandwidths, and weights are ready. */
    if (this.cachedNodeStatuses != null
        || this.cachedSummaryDocuments != null) {
      if (this.cachedNodeStatuses != null) {
        this.writeNodeStatuses();
      }
      if (this.cachedSummaryDocuments != null) {
        this.writeSummaryDocuments();
      }
      this.writeUpdateStatus();
    }
  }

  /** Invalidates the document cache, so that it will be freshly populated
   * during the next execution. */
  public void invalidateDocumentCache() {
    this.cachedNodeStatuses = null;
    this.cachedSummaryDocuments = null;
    this.lastModifiedNodeStatuses = 0L;
    this.lastModifiedSummaryDocuments = 0L;
    this.updatedNodeStatuses = null;
    this.updatedSummaryDocuments = null;
  }

  private void writeNodeStatuses() {
    File directory = this.statusDir;
    if (directory == null) {
      logger.error("Unable to write node statuses without knowing the "
          + "'status' directory to write to!");
      return;
    }
    File summaryFile = new File(directory, "summary");
    SortedMap<String, NodeStatus> cachedRelays = new TreeMap<>();
    SortedMap<String, NodeStatus> cachedBridges = new TreeMap<>();
    for (Map.Entry<String, NodeStatus> e :
        this.cachedNodeStatuses.entrySet()) {
      if (e.getValue().isRelay()) {
        cachedRelays.put(e.getKey(), e.getValue());
      } else {
        cachedBridges.put(e.getKey(), e.getValue());
      }
    }
    StringBuilder sb = new StringBuilder();
    for (NodeStatus relay : cachedRelays.values()) {
      String line = relay.toString();
      if (line != null) {
        sb.append(line).append("\n");
      } else {
        logger.error("Could not serialize relay node status '{}'",
            relay.getFingerprint());
      }
    }
    for (NodeStatus bridge : cachedBridges.values()) {
      String line = bridge.toString();
      if (line != null) {
        sb.append(line).append("\n");
      } else {
        logger.error("Could not serialize bridge node status '{}'",
            bridge.getFingerprint());
      }
    }
    String documentString = sb.toString();
    try {
      summaryFile.getParentFile().mkdirs();
      writeToFile(summaryFile, documentString);
      this.lastModifiedNodeStatuses = summaryFile.lastModified();
      this.updatedNodeStatuses.clear();
      this.storedFiles++;
      this.storedBytes += documentString.length();
    } catch (IOException e) {
      logger.error("Could not write file '{}'.", summaryFile.getAbsolutePath(),
          e);
    }
  }

  private static void writeToFile(File file, String content)
      throws IOException {
    try (BufferedOutputStream bos = new BufferedOutputStream(
        new FileOutputStream(file))) {
      bos.write(content.getBytes(StandardCharsets.UTF_8));
    }
  }

  static void writeStatistics(Path path, List<String> data) throws IOException {
    path.toFile().getParentFile().mkdirs();
    logger.info("Writing {} lines to {}.", data.size(), path.toFile().getAbsolutePath());
    Files.write(path, data, StandardCharsets.UTF_8);
  }

  private void writeSummaryDocuments() {
    if (this.outDir == null) {
      /* Can't write out/summary without knowing the path of out/. */
      return;
    }
    StringBuilder sb = new StringBuilder();
    for (SummaryDocument summaryDocument :
        this.cachedSummaryDocuments.values()) {
      String line;
      try {
        line = objectMapper.writeValueAsString(summaryDocument);
      } catch (JsonProcessingException e) {
        line = null;
      }
      if (line != null) {
        sb.append(line).append("\n");
      } else {
        logger.error("Could not serialize relay summary document '{}'",
            summaryDocument.getFingerprint());
      }
    }
    String documentString = sb.toString();
    File summaryFile = new File(this.outDir, "summary");
    try {
      summaryFile.getParentFile().mkdirs();
      writeToFile(summaryFile, documentString);
      this.lastModifiedSummaryDocuments = summaryFile.lastModified();
      this.updatedSummaryDocuments.clear();
      this.storedFiles++;
      this.storedBytes += documentString.length();
    } catch (IOException e) {
      logger.error("Could not write file '{}'.", summaryFile.getAbsolutePath(),
          e);
    }
  }

  private void writeUpdateStatus() {
    if (this.outDir == null) {
      /* Can't write out/update without knowing the path of out/. */
      return;
    }
    UpdateStatus updateStatus = new UpdateStatus();
    updateStatus.setUpdatedMillis(System.currentTimeMillis());
    this.store(updateStatus);
  }

  /** Returns a string with statistics on document storage operations. */
  public String getStatsString() {
    return String.format("    %s list operations performed\n"
        + "    %s files listed\n"
        + "    %s files stored\n"
        + "    %s stored\n"
        + "    %s files not rewritten\n"
        + "    %s not rewritten\n"
        + "    %s files retrieved\n"
        + "    %s retrieved\n"
        + "    %s files removed\n",
        FormattingUtils.formatDecimalNumber(listOperations),
        FormattingUtils.formatDecimalNumber(listedFiles),
        FormattingUtils.formatDecimalNumber(storedFiles),
        FormattingUtils.formatBytes(storedBytes),
        FormattingUtils.formatDecimalNumber(unchangedFiles),
        FormattingUtils.formatBytes(unchangedBytes),
        FormattingUtils.formatDecimalNumber(retrievedFiles),
        FormattingUtils.formatBytes(retrievedBytes),
        FormattingUtils.formatDecimalNumber(removedFiles));
  }

}
