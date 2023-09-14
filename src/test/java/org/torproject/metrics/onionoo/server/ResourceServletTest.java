/* Copyright 2013--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.torproject.metrics.onionoo.docs.DateTimeHelper;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.DummyDocumentStore;
import org.torproject.metrics.onionoo.docs.UpdateStatus;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/* TODO This test class could (should?) be split into ResponseBuilderTest
 * which tests ResponseBuilder and a much shorter ResourceServletTest
 * which tests servlet specifics. */
public class ResourceServletTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  private SortedMap<String, org.torproject.metrics.onionoo.docs.SummaryDocument>
      relays;
  private SortedMap<String, org.torproject.metrics.onionoo.docs.SummaryDocument>
      bridges;

  private static long TEST_TIME = DateTimeHelper.millisNow();

  private static class TestingHttpServletRequestWrapper
      extends HttpServletRequestWrapper {
    private String requestUri;
    private String queryString;
    private Map<String, String[]> parameterMap;

    private TestingHttpServletRequestWrapper(String requestUri,
        String queryString, Map<String, String[]> parameterMap) {
      super(null);
      this.requestUri = requestUri;
      this.queryString = queryString;
      this.parameterMap = parameterMap == null
          ? new HashMap<>() : parameterMap;
    }

    @Override
    protected String getRequestURI() {
      return this.requestUri;
    }

    protected Map<String, String[]> getParameterMap() {
      return this.parameterMap;
    }

    protected String[] getParameterValues(String parameterKey) {
      return this.parameterMap.get(parameterKey);
    }

    protected String getQueryString() {
      return this.queryString;
    }
  }

  private static class TestingHttpServletResponseWrapper extends
      HttpServletResponseWrapper {

    private TestingHttpServletResponseWrapper() {
      super(null);
    }

    private int errorStatusCode;

    protected void sendError(int errorStatusCode) {
      this.errorStatusCode = errorStatusCode;
    }

    private Map<String, String> headers = new HashMap<>();

    protected void setHeader(String headerName, String headerValue) {
      this.headers.put(headerName, headerValue);
    }

    protected void setContentType(String contentType) {
    }

    protected void setCharacterEncoding(String characterEncoding) {
    }

    private StringWriter stringWriter;

    protected PrintWriter getWriter() throws IOException {
      if (this.stringWriter == null) {
        this.stringWriter = new StringWriter();
        return new PrintWriter(this.stringWriter);
      } else {
        throw new IOException("Can only request writer once");
      }
    }

    private String getWrittenContent() {
      return this.stringWriter == null ? null
          : this.stringWriter.toString();
    }
  }

  private TestingHttpServletRequestWrapper request;

  private TestingHttpServletResponseWrapper response;

  private String responseString;

  private SummaryDocument summaryDocument;

  @SuppressWarnings("JavadocMethod")
  @Before
  public void createSampleRelaysAndBridges() {
    this.relays = new TreeMap<>();
    org.torproject.metrics.onionoo.docs.SummaryDocument relayTorkaZ =
        new org.torproject.metrics.onionoo.docs.SummaryDocument(true, "TorkaZ",
        "000C5F55BD4814B917CC474BD537F1A3B33CCE2A", Arrays.asList(
            "62.216.201.221", "62.216.201.222",
            "62.216.201.223"),
            DateTimeHelper.daysFromDate(5, 7, 22, 22, TEST_TIME),
        false, new TreeSet<>(Arrays.asList("Running",
            "Valid")), 20L, "de",
        DateTimeHelper.daysFromDate(6, 7, 22, 22, TEST_TIME), "AS8767",
        "m-net telekommunikations gmbh",
        "torkaz <klaus dot zufall at gmx dot de> "
        + "<fb-token:np5_g_83jmf=>",
        new TreeSet<>(Arrays.asList(
            "001C13B3A55A71B977CA65EC85539D79C653A3FC")),
        "0.2.3.25", "linux",
        new TreeSet<>(Arrays.asList(
            "ppp-62-216-201-221.dynamic.mnet-online.de")),
        null, true, false, Arrays.asList("obfs4"));
    this.relays.put("000C5F55BD4814B917CC474BD537F1A3B33CCE2A",
        relayTorkaZ);
    org.torproject.metrics.onionoo.docs.SummaryDocument relayFerrari458 =
        new org.torproject.metrics.onionoo.docs.SummaryDocument(true,
            "Ferrari458", "001C13B3A55A71B977CA65EC85539D79C653A3FC",
            Arrays.asList("68.38.171.200", "[2001:4f8:3:2e::51]"),
        DateTimeHelper.daysFromDate(0, 0, 22, 22, TEST_TIME), true,
        new TreeSet<>(Arrays.asList("Fast", "Named",
            "Running", "V2Dir", "Valid")), 1140L, "us",
        DateTimeHelper.daysFromDate(7, 18, 22, 22, TEST_TIME), "AS7922",
        "comcast cable communications, llc", null,
        new TreeSet<>(Arrays.asList(
            "000C5F55BD4814B917CC474BD537F1A3B33CCE2A")),
        null, null,
        new TreeSet<>(Arrays.asList(
            "c-68-38-171-200.hsd1.in.comcast.net")),
        null, null, false, null);
    this.relays.put("001C13B3A55A71B977CA65EC85539D79C653A3FC",
        relayFerrari458);
    org.torproject.metrics.onionoo.docs.SummaryDocument relayTimMayTribute =
        new org.torproject.metrics.onionoo.docs.SummaryDocument(true,
            "TimMayTribute", "0025C136C1F3A9EEFE2AE3F918F03BFA21B5070B",
            Arrays.asList("89.69.68.246"),
        DateTimeHelper.daysFromDate(1, 16, 22, 22, TEST_TIME), false,
        new TreeSet<>(Arrays.asList("Fast",
            "Running", "Unnamed", "V2Dir", "Valid")), 63L, "a1",
        DateTimeHelper.daysFromDate(7, 18, 22, 22, TEST_TIME), null,
        "liberty global operations b.v.",
        "1024d/51e2a1c7 \"steven j. murdoch\" "
        + "<tor+steven.murdoch@cl.cam.ac.uk> <fb-token:5sr_k_zs2wm=>",
        new TreeSet<>(), "0.2.3.24-rc-dev",
        "windows xp", null, null, false, false, null);
    this.relays.put("0025C136C1F3A9EEFE2AE3F918F03BFA21B5070B",
        relayTimMayTribute);
    this.bridges = new TreeMap<>();
    org.torproject.metrics.onionoo.docs.SummaryDocument
        bridgeec2bridgercc7f31fe =
        new org.torproject.metrics.onionoo.docs.SummaryDocument(false,
        "ec2bridgercc7f31fe", "0000831B236DFF73D409AD17B40E2A728A53994F",
        Arrays.asList("10.199.7.176"),
        DateTimeHelper.daysFromDate(2, 18, 15, 19, TEST_TIME), false,
        new TreeSet<>(Arrays.asList("Valid")), -1L,
        null, DateTimeHelper.daysFromDate(3, 20, 45, 18, TEST_TIME),
        null, null, null, null, "0.2.2.39", null, null, null,
        true, false, Arrays.asList("obfs4"));
    this.bridges.put("0000831B236DFF73D409AD17B40E2A728A53994F",
        bridgeec2bridgercc7f31fe);
    org.torproject.metrics.onionoo.docs.SummaryDocument bridgeUnnamed =
        new org.torproject.metrics.onionoo.docs.SummaryDocument(false,
            "Unnamed", "0002D9BDBBC230BD9C78FF502A16E0033EF87E0C",
            Arrays.asList("10.0.52.84"),
        DateTimeHelper.daysFromDate(3, 20, 45, 18, TEST_TIME), false,
        new TreeSet<>(Arrays.asList("Valid")), -1L,
        null, DateTimeHelper.daysFromDate(10, 5, 15, 17, TEST_TIME),
        null, null, null, null, null, null, null, null, null, false,
        Arrays.asList("obfs4"));
    this.bridges.put("0002D9BDBBC230BD9C78FF502A16E0033EF87E0C",
        bridgeUnnamed);
    org.torproject.metrics.onionoo.docs.SummaryDocument bridgegummy =
        new org.torproject.metrics.onionoo.docs.SummaryDocument(false, "gummy",
        "1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB756", Arrays.asList(
            "10.63.169.98"),
        DateTimeHelper.daysFromDate(0, 11, 15, 18, TEST_TIME), true,
        new TreeSet<>(Arrays.asList("Running",
            "Valid")), -1L, null,
        DateTimeHelper.daysFromDate(7, 15, 15, 18, TEST_TIME), null, null,
        null, null, "0.2.4.4-alpha-dev", "windows 7", null, null,
        false, false, Arrays.asList("obfs4"));
    this.bridges.put("1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB756",
        bridgegummy);
  }

  private void runTest(String request) {
    try {
      this.createDummyDocumentStore();
      this.createNodeIndexer();
      this.makeRequest(request);
      this.parseResponse();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void createDummyDocumentStore() {
    DummyDocumentStore documentStore = new DummyDocumentStore();
    UpdateStatus updateStatus = new UpdateStatus();
    updateStatus.setUpdatedMillis(TEST_TIME);
    documentStore.addDocument(updateStatus, null);
    for (Map.Entry<String, org.torproject.metrics.onionoo.docs.SummaryDocument>
             e : this.relays.entrySet()) {
      documentStore.addDocument(e.getValue(), e.getKey());
    }
    for (Map.Entry<String, org.torproject.metrics.onionoo.docs.SummaryDocument>
             e : this.bridges.entrySet()) {
      documentStore.addDocument(e.getValue(), e.getKey());
    }
    DocumentStoreFactory.setDocumentStore(documentStore);
  }

  private void createNodeIndexer() {
    NodeIndexer newNodeIndexer = new NodeIndexer();
    try {
      Field specialTimeField = newNodeIndexer.getClass()
          .getDeclaredField("specialTime");
      specialTimeField.setAccessible(true);
      specialTimeField.set(newNodeIndexer, TEST_TIME);
    } catch (Exception ex) {
      fail("Cannot manipulate test-time.  Failing all.");
    }
    newNodeIndexer.startIndexing();
    NodeIndexerFactory.setNodeIndexer(newNodeIndexer);
  }

  private void makeRequest(String request) throws IOException {
    ResourceServlet rs = new ResourceServlet();
    String[] requestParts = request.split("\\?");
    String path = requestParts[0];
    String queryString = requestParts.length > 1 ? requestParts[1] : null;
    Map<String, String[]> parameterMap = parseParameters(request);
    this.request = new TestingHttpServletRequestWrapper(path, queryString,
        parameterMap);
    this.response = new TestingHttpServletResponseWrapper();
    rs.doGet(this.request, this.response, TEST_TIME);
  }

  private void parseResponse() throws IOException {
    this.responseString = this.response.getWrittenContent();
    if (this.responseString != null) {
      this.summaryDocument = objectMapper.readValue(this.responseString,
          SummaryDocument.class);
    }
  }

  private void assertErrorStatusCode(String request,
      int errorStatusCode) {
    this.runTest(request);
    assertEquals(errorStatusCode, this.response.errorStatusCode);
  }

  private void assertSummaryDocument(String request,
      int expectedRelaysNumber, String[] expectedRelaysNicknames,
      int expectedBridgesNumber, String[] expectedBridgesNicknames) {
    this.runTest(request);
    assertNotNull("Summary document is null, status code is "
        + this.response.errorStatusCode, this.summaryDocument);
    assertEquals("Unexpected number of relays.", expectedRelaysNumber,
        this.summaryDocument.relays.length);
    if (expectedRelaysNicknames != null) {
      for (int i = 0; i < expectedRelaysNumber; i++) {
        assertEquals("Unexpected relay nickname.", expectedRelaysNicknames[i],
            this.summaryDocument.relays[i].n);
      }
    }
    assertEquals("Unexpected number of bridges.", expectedBridgesNumber,
        this.summaryDocument.bridges.length);
    if (expectedBridgesNicknames != null) {
      for (int i = 0; i < expectedBridgesNumber; i++) {
        assertEquals("Unexpected bridge nickname.", expectedBridgesNicknames[i],
            this.summaryDocument.bridges[i].n);
      }
    }
  }

  private void assertSkippedReturnedTruncated(String request,
      int expectedRelaysSkipped, int expectedRelaysReturned,
      int expectedRelaysTruncated, int expectedBridgesSkipped,
      int expectedBridgesReturned, int expectedBridgesTruncated) {
    this.runTest(request);
    assertNotNull(this.summaryDocument);
    assertEquals(expectedRelaysSkipped, this.summaryDocument.relays_skipped);
    assertEquals(expectedRelaysReturned, this.summaryDocument.relays.length);
    assertEquals(expectedRelaysTruncated,
        this.summaryDocument.relays_truncated);
    assertEquals(expectedBridgesSkipped, this.summaryDocument.bridges_skipped);
    assertEquals(expectedBridgesReturned, this.summaryDocument.bridges.length);
    assertEquals(expectedBridgesTruncated,
        this.summaryDocument.bridges_truncated);
  }

  private Map<String, String[]> parseParameters(String request) {
    Map<String, String[]> parameters = null;
    String[] uriParts = request.split("\\?");
    if (uriParts.length == 2) {
      Map<String, List<String>> parameterLists =
          new HashMap<>();
      for (String parameter : uriParts[1].split("&")) {
        String[] parameterParts = parameter.split("=");
        parameterLists.putIfAbsent(parameterParts[0], new ArrayList<>());
        parameterLists.get(parameterParts[0]).add(parameterParts[1]);
      }
      parameters = new HashMap<>();
      for (Map.Entry<String, List<String>> e :
          parameterLists.entrySet()) {
        parameters.put(e.getKey(),
            e.getValue().toArray(new String[e.getValue().size()]));
      }
    }
    return parameters;
  }

  @SuppressWarnings("MemberName")
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  private static class SummaryDocument {
    private String version;
    private String next_major_version_scheduled;
    private String build_revision;
    private String relays_published;
    private int relays_skipped;
    private int relays_truncated;
    private RelaySummary[] relays;
    private String bridges_published;
    private int bridges_skipped;
    private int bridges_truncated;
    private BridgeSummary[] bridges;
  }

  @SuppressWarnings("MemberName")
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  private static class RelaySummary {
    private String n;
    private String f;
    private String[] a;
    private boolean r;
  }

  @SuppressWarnings("MemberName")
  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  private static class BridgeSummary {
    private String n;
    private String h;
    private boolean r;
  }

  @Test(timeout = 100)
  public void testValidSummaryRelay() {
    this.runTest("/summary");
    long relaysPublishedMillis = DateTimeHelper.daysFromDate(0, 0, 22, 22,
        TEST_TIME);
    String relaysPublished = DateTimeHelper.format(relaysPublishedMillis);
    assertEquals(relaysPublished,
        this.summaryDocument.relays_published);
    assertEquals(3, this.summaryDocument.relays.length);
    RelaySummary relay = null;
    for (RelaySummary r : this.summaryDocument.relays) {
      if (r.f.equals("000C5F55BD4814B917CC474BD537F1A3B33CCE2A")) {
        relay = r;
        break;
      }
    }
    assertNotNull(relay);
    assertEquals("TorkaZ", relay.n);
    assertEquals(3, relay.a.length);
    assertEquals("62.216.201.221", relay.a[0]);
    assertFalse(relay.r);
  }

  @Test(timeout = 100)
  public void testValidSummaryBridge() {
    this.runTest("/summary");
    long bridgesPublishedMillis = DateTimeHelper.daysFromDate(0, 11, 15, 18,
        TEST_TIME);
    String bridgesPublished = DateTimeHelper.format(bridgesPublishedMillis);
    assertEquals(bridgesPublished,
        this.summaryDocument.bridges_published);
    assertEquals(3, this.summaryDocument.bridges.length);
    BridgeSummary bridge = null;
    for (BridgeSummary b : this.summaryDocument.bridges) {
      if (b.h.equals("0000831B236DFF73D409AD17B40E2A728A53994F")) {
        bridge = b;
        break;
      }
    }
    assertNotNull(bridge);
    assertEquals("ec2bridgercc7f31fe", bridge.n);
    assertFalse(bridge.r);
  }

  @Test(timeout = 100)
  public void testNonExistantDocumentType() {
    this.assertErrorStatusCode(
        "/doesnotexist", 400);
  }

  @Test(timeout = 100)
  public void testSummaryUpperCaseDocument() {
    this.assertErrorStatusCode(
        "/SUMMARY", 400);
  }

  @Test(timeout = 100)
  public void testTypeRelay() {
    this.assertSummaryDocument(
        "/summary?type=relay", 3, null, 0, null);
  }

  @Test(timeout = 100)
  public void testTypeBridge() {
    this.assertSummaryDocument(
        "/summary?type=bridge", 0, null, 3, null);
  }

  @Test(timeout = 100)
  public void testTypeBridgerelay() {
    this.assertErrorStatusCode(
        "/summary?type=bridgerelay", 400);
  }

  @Test(timeout = 100)
  public void testTypeRelayBridge() {
    this.assertSummaryDocument(
        "/summary?type=relay&type=bridge", 3, null, 0, null);
  }

  @Test(timeout = 100)
  public void testTypeBridgeRelay() {
    this.assertSummaryDocument(
        "/summary?type=bridge&type=relay", 0, null, 3, null);
  }

  @Test(timeout = 100)
  public void testTypeRelayRelay() {
    this.assertSummaryDocument(
        "/summary?type=relay&type=relay", 3, null, 0, null);
  }

  @Test(timeout = 100)
  public void testTypeUpperCaseRelay() {
    this.assertErrorStatusCode(
        "/summary?TYPE=relay", 400);
  }

  @Test(timeout = 100)
  public void testTypeRelayUpperCase() {
    this.assertSummaryDocument(
        "/summary?type=RELAY", 3, null, 0, null);
  }

  @Test(timeout = 100)
  public void testRunningTrue() {
    this.assertSummaryDocument(
        "/summary?running=true", 1, new String[] { "Ferrari458" }, 1,
        new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testRunningFalse() {
    this.assertSummaryDocument(
        "/summary?running=false", 2, null, 2, null);
  }

  @Test(timeout = 100)
  public void testRunningTruefalse() {
    this.assertErrorStatusCode(
        "/summary?running=truefalse", 400);
  }

  @Test(timeout = 100)
  public void testRunningTrueFalse() {
    this.assertSummaryDocument(
        "/summary?running=true&running=false", 1,
        new String[] { "Ferrari458" }, 1,  new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testRunningFalseTrue() {
    this.assertSummaryDocument(
        "/summary?running=false&running=true", 2, null, 2, null);
  }

  @Test(timeout = 100)
  public void testRunningTrueTrue() {
    this.assertSummaryDocument(
        "/summary?running=true&running=true", 1,
        new String[] { "Ferrari458" }, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testRunningUpperCaseTrue() {
    this.assertErrorStatusCode(
        "/summary?RUNNING=true", 400);
  }

  @Test(timeout = 100)
  public void testRunningTrueUpperCase() {
    this.assertSummaryDocument(
        "/summary?running=TRUE", 1, null, 1, null);
  }

  @Test(timeout = 100)
  public void testSearchTorkaZ() {
    this.assertSummaryDocument(
        "/summary?search=TorkaZ", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchTorkaX() {
    this.assertSummaryDocument(
        "/summary?search=TorkaX", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchOrkaZ() {
    this.assertSummaryDocument(
        "/summary?search=orkaZ", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchTorka() {
    this.assertSummaryDocument(
        "/summary?search=Torka", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchTorkazUpperCase() {
    this.assertSummaryDocument(
        "/summary?search=TORKAZ", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDollarFingerprint() {
    this.assertSummaryDocument(
        "/summary?search=$000C5F55BD4814B917CC474BD537F1A3B33CCE2A", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchFingerprint() {
    this.assertSummaryDocument(
        "/summary?search=000C5F55BD4814B917CC474BD537F1A3B33CCE2A", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDollarFingerprint39() {
    this.assertSummaryDocument(
        "/summary?search=$000C5F55BD4814B917CC474BD537F1A3B33CCE2", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDollarFingerprintLowerCase39() {
    this.assertSummaryDocument(
        "/summary?search=$000c5f55bd4814b917cc474bd537f1a3b33cce2", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchFingerprintLowerCase39() {
    this.assertSummaryDocument(
        "/summary?search=000c5f55bd4814b917cc474bd537f1a3b33cce2", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDollarHashedFingerprint() {
    this.assertSummaryDocument(
        "/summary?search=$5aa14c08d62913e0057a9ad5863b458c0ce94cee", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDollarHashedFingerprint39() {
    this.assertSummaryDocument(
        "/summary?search=$5aa14c08d62913e0057a9ad5863b458c0ce94ce", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDollarHashedFingerprint41() {
    this.assertErrorStatusCode(
        "/summary?search=$5aa14c08d62913e0057a9ad5863b458c0ce94ceee",
        400);
  }

  @Test(timeout = 100)
  public void testSearchBase64FingerprintAlphaNum() {
    this.assertSummaryDocument(
        "/summary?search=AAxfVb1IFLkXzEdL1Tfxo7M8zio", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchBase64FingerprintSlash() {
    this.assertSummaryDocument(
        "/summary?search=ABwTs6Vacbl3ymXshVOdecZTo/w", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchBase64FingerprintPlus() {
    this.assertSummaryDocument(
        "/summary?search=ACXBNsHzqe7+KuP5GPA7+iG1Bws", 1,
        new String[] { "TimMayTribute" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchBase64FingerprintPlusEncoded() {
    this.assertSummaryDocument(
        "/summary?search=ACXBNsHzqe7%2BKuP5GPA7+iG1Bws", 1,
        new String[] { "TimMayTribute" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchBase64FingerprintBridge() {
    this.assertSummaryDocument(
        "/summary?search=AACDGyNt/3PUCa0XtA4qcopTmU8", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchBase64FingerprintPartial() {
    this.assertSummaryDocument(
        "/summary?search=AAx", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchBase64HashedFingerprintTorkaZ() {
    this.assertSummaryDocument(
        "/summary?search=WqFMCNYpE+AFeprVhjtFjAzpTO4", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchBase64Fingerprint28() {
    this.assertErrorStatusCode(
        "/summary?search=AAAAAAAAAAAA//AAAAAAAAAAAAAA", 400);
  }

  @Test(timeout = 100)
  public void testSearchSpaceSeparatedFingerprintFourty() {
    this.assertSummaryDocument(
        "/summary?search=000C 5F55 BD48 14B9 17CC 474B D537 F1A3 B33C "
        + "CE2A", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchSpaceSeparatedFingerprintLastEight() {
    this.assertSummaryDocument(
        "/summary?search=F1A3 B33C", 1, new String[] { "TorkaZ" }, 0,
        null);
  }

  @Test(timeout = 100)
  public void testSearchPlusSeparatedFingerprintLastEight() {
    this.assertSummaryDocument(
        "/summary?search=F1A3+B33C", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchSpaceSeparatedFingerprintLastThree() {
    this.assertSummaryDocument(
        "/summary?search=33C", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIp() {
    this.assertSummaryDocument(
        "/summary?search=62.216.201.221", 1, new String[] { "TorkaZ" }, 0,
        null);
  }

  @Test(timeout = 100)
  public void testSearchIp24Network() {
    this.assertSummaryDocument(
        "/summary?search=62.216.201", 1, new String[] { "TorkaZ" }, 0,
        null);
  }

  @Test(timeout = 100)
  public void testSearchIpExit() {
    this.assertSummaryDocument(
        "/summary?search=62.216.201.222", 1, new String[] { "TorkaZ" }, 0,
        null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6() {
    this.assertSummaryDocument(
        "/summary?search=[2001:4f8:3:2e::51]", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6Slash64NoTrailingBracket() {
    this.assertSummaryDocument(
        "/summary?search=[2001:4f8:3:2e::", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6Slash64TrailingBracket() {
    this.assertSummaryDocument(
        "/summary?search=[2001:4f8:3:2e::]", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6Slash64NoBrackets() {
    this.assertSummaryDocument(
        "/summary?search=2001:4f8:3:2e::", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6Slash8Colon() {
    this.assertSummaryDocument(
        "/summary?search=[2001:", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6Slash8NoColon() {
    this.assertSummaryDocument(
        "/summary?search=[2001", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6Slash8NoColonNoBrackets() {
    this.assertSummaryDocument(
        "/summary?search=2001", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6Uncompressed() {
    this.assertSummaryDocument(
        "/summary?search=[2001:04f8:0003:002e:0000:0000:0000:0051]", 0,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6UpperCase() {
    this.assertSummaryDocument(
        "/summary?search=[2001:4F8:3:2E::51]", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6ThreeColons() {
    this.assertSummaryDocument(
        "/summary?search=[2001:4f8:3:2e:::51]", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6FiveHex() {
    this.assertSummaryDocument(
        "/summary?search=[20014:f80:3:2e::51]", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6NineGroups() {
    this.assertSummaryDocument(
        "/summary?search=[1:2:3:4:5:6:7:8:9]", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchIpv6TcpPort() {
    this.assertErrorStatusCode(
        "/summary?search=[2001:4f8:3:2e::51]:9001", 400);
  }

  @Test(timeout = 100)
  public void testSearchGummy() {
    this.assertSummaryDocument(
        "/summary?search=gummy", 0, null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchGummi() {
    this.assertSummaryDocument(
        "/summary?search=gummi", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchUmmy() {
    this.assertSummaryDocument(
        "/summary?search=ummy", 0, null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchGumm() {
    this.assertSummaryDocument(
        "/summary?search=gumm", 0, null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchGummyUpperCase() {
    this.assertSummaryDocument(
        "/summary?search=GUMMY", 0, null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeDollarHashedFingerprint() {
    this.assertSummaryDocument(
        "/summary?search=$1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB756", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeHashedFingerprint() {
    this.assertSummaryDocument(
        "/summary?search=1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB756", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeDollarHashedFingerprint39() {
    this.assertSummaryDocument(
        "/summary?search=$1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB75", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeDollarHashedFingerprintLowerCase39() {
    this.assertSummaryDocument(
        "/summary?search=$1fede50ed8dba1dd9f9165f78c8131e4a44ab75", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeHashedFingerprintLowerCase39() {
    this.assertSummaryDocument(
        "/summary?search=1fede50ed8dba1dd9f9165f78c8131e4a44ab75", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeDollarHashedHashedFingerprint() {
    this.assertSummaryDocument(
        "/summary?search=$CE52F898DB3678BCE33FAC28C92774DE90D618B5", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeDollarHashedHashedFingerprint39() {
    this.assertSummaryDocument(
        "/summary?search=$CE52F898DB3678BCE33FAC28C92774DE90D618B", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testSearchBridgeDollarOriginalFingerprint() {
    this.assertSummaryDocument(
        "/summary?search=$0010D49C6DA1E46A316563099F41BFE40B6C7183", 0,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchUnderscore() {
    this.assertErrorStatusCode(
        "/summary?search=_", 400);
  }

  @Test(timeout = 100)
  public void testSearchTypeRelay() {
    this.assertSummaryDocument("/summary?search=type:relay", 3, null, 0,
        null);
  }

  @Test(timeout = 100)
  public void testSearchTypeRelayTorkaZ() {
    this.assertSummaryDocument("/summary?search=type:relay TorkaZ", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchTorkaZTypeRelay() {
    this.assertSummaryDocument("/summary?search=TorkaZ type:relay", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchTorkaZEscapedSpaceTypeRelay() {
    this.assertSummaryDocument("/summary?search=TorkaZ%20type:relay", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchTypeRelayTypeDirectory() {
    this.assertSummaryDocument(
        "/summary?search=type:relay type:directory", 3, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchTypeDirectoryTypeRelay() {
    this.assertErrorStatusCode(
        "/summary?search=type:directory type:relay", 400);
  }

  @Test(timeout = 100)
  public void testSearchFooBar() {
    this.assertErrorStatusCode("/summary?search=foo:bar", 400);
  }

  @Test(timeout = 100)
  public void testSearchSearchTorkaZ() {
    this.assertErrorStatusCode("/summary?search=search:TorkaZ", 400);
  }

  @Test(timeout = 100)
  public void testSearchLimitOne() {
    this.assertErrorStatusCode("/summary?search=limit:1", 400);
  }

  @Test(timeout = 100)
  public void testSearchDeadBeef() {
    /* This does not return 400 Bad Request, even though "dead" is not a valid
     * search term qualifier, because this could be the start of an IPv6 address
     * without leading bracket. */
    this.assertSummaryDocument("/summary?search=dead:beef", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchEmailAddress() {
    this.assertSummaryDocument(
        "/summary?search=contact:<tor+steven.murdoch@cl.cam.ac.uk>", 1,
        new String[] { "TimMayTribute" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchEmailAddressEncodedPlus() {
    this.assertSummaryDocument(
        "/summary?search=contact:<tor%2Bsteven.murdoch@cl.cam.ac.uk>", 1,
        new String[] { "TimMayTribute" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDoubleQuotedEmailAddress() {
    this.assertSummaryDocument(
        "/summary?search=contact:\"klaus dot zufall at gmx dot de\"", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDoubleQuotedEmailAddressReversed() {
    this.assertSummaryDocument(
        "/summary?search=contact:\"de dot gmx at zufall dot klaus\"", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchDoubleQuotedContactAndNickname() {
    this.assertSummaryDocument(
        "/summary?search=contact:\"dot de\" TorkaZ", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchMissingEndingDoubleQuote() {
    this.assertErrorStatusCode(
        "/summary?search=contact:\"klaus dot zufall at gmx dot de", 400);
  }

  @Test(timeout = 100)
  public void testSearchEvenNumberOfDoubleQuotes() {
    this.assertSummaryDocument(
        "/summary?search=contact:\"\"\" \"\"\"", 0,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testSearchContactEscapedDoubleQuotes() {
    this.assertSummaryDocument(
        "/summary?search=contact:\"1024D/51E2A1C7 \\\"Steven J. Murdoch\\\"\"",
        1, new String[] { "TimMayTribute" }, 0, null);
  }

  @Test(timeout = 100)
  public void testLookupFingerprint() {
    this.assertSummaryDocument(
        "/summary?lookup=000C5F55BD4814B917CC474BD537F1A3B33CCE2A", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testLookupDollarFingerprint() {
    this.assertErrorStatusCode(
        "/summary?lookup=$000C5F55BD4814B917CC474BD537F1A3B33CCE2A", 400);
  }

  @Test(timeout = 100)
  public void testLookupDollarFingerprint39() {
    this.assertErrorStatusCode(
        "/summary?lookup=$000C5F55BD4814B917CC474BD537F1A3B33CCE2", 400);
  }

  @Test(timeout = 100)
  public void testLookupFingerprintLowerCase39() {
    this.assertErrorStatusCode(
        "/summary?lookup=000c5f55bd4814b917cc474bd537f1a3b33cce2", 400);
  }

  @Test(timeout = 100)
  public void testLookupHashedFingerprint() {
    this.assertSummaryDocument(
        "/summary?lookup=5aa14c08d62913e0057a9ad5863b458c0ce94cee", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testLookupBridgeHashedFingerprint() {
    this.assertSummaryDocument(
        "/summary?lookup=1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB756", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testLookupBridgeHashedHashedFingerprint() {
    this.assertSummaryDocument(
        "/summary?lookup=CE52F898DB3678BCE33FAC28C92774DE90D618B5", 0,
        null, 1, new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testLookupBridgeOriginalFingerprint() {
    this.assertSummaryDocument(
        "/summary?lookup=0010D49C6DA1E46A316563099F41BFE40B6C7183", 0,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testLookupNonExistantFingerprint() {
    this.assertSummaryDocument(
        "/summary?lookup=0000000000000000000000000000000000000000", 0,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testLookupTwoFingerprints() {
    this.assertSummaryDocument(
        "/summary?lookup=000C5F55BD4814B917CC474BD537F1A3B33CCE2A,"
        + "1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB756", 1,
        new String[] { "TorkaZ" }, 1,   new String[] { "gummy" });
  }

  @Test(timeout = 100)
  public void testLookupFingerprintTrailingComma() {
    this.assertErrorStatusCode(
        "/summary?lookup=000C5F55BD4814B917CC474BD537F1A3B33CCE2A,", 400);
  }

  @Test(timeout = 100)
  public void testFingerprintRelayFingerprint() {
    this.assertErrorStatusCode(
        "/summary?fingerprint=000C5F55BD4814B917CC474BD537F1A3B33CCE2A",
        400);
  }

  @Test(timeout = 100)
  public void testFingerprintRelayHashedFingerprint() {
    this.assertErrorStatusCode(
        "/summary?fingerprint=4aa14c08d62913e0057a9ad5863b458c0ce94cee",
        400);
  }

  @Test(timeout = 100)
  public void testFingerprintBridgeHashedFingerprint() {
    this.assertErrorStatusCode(
        "/summary?fingerprint=1FEDE50ED8DBA1DD9F9165F78C8131E4A44AB756",
        400);
  }

  @Test(timeout = 100)
  public void testFingerprintBridgeHashedHashedFingerprint() {
    this.assertErrorStatusCode(
        "/summary?fingerprint=CE52F898DB3678BCE33FAC28C92774DE90D618B5",
        400);
  }

  @Test(timeout = 100)
  public void testFingerprintBridgeOriginalFingerprint() {
    this.assertErrorStatusCode(
        "/summary?fingerprint=0010D49C6DA1E46A316563099F41BFE40B6C7183",
        400);
  }

  @Test(timeout = 100)
  public void testCountryDe() {
    this.assertSummaryDocument(
        "/summary?country=de", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testCountryFr() {
    this.assertSummaryDocument(
        "/summary?country=fr", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testCountryZz() {
    this.assertSummaryDocument(
        "/summary?country=zz", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testCountryDeUpperCase() {
    this.assertSummaryDocument(
        "/summary?country=DE", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testCountryDeu() {
    this.assertErrorStatusCode(
        "/summary?country=deu", 400);
  }

  @Test(timeout = 100)
  public void testCountryD() {
    this.assertErrorStatusCode(
        "/summary?country=d", 400);
  }

  @Test(timeout = 100)
  public void testCountryA1() {
    this.assertSummaryDocument(
        "/summary?country=a1", 1, new String[] { "TimMayTribute" }, 0,
        null);
  }

  @Test(timeout = 800)
  public void testCountryDeDe() {
    this.assertSummaryDocument(
        "/summary?country=de&country=de", 1, new String[] { "TorkaZ" }, 0,
        null);
  }

  @Test(timeout = 100)
  public void testAsAS8767() {
    this.assertSummaryDocument(
        "/summary?as=AS8767", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testAs8767() {
    this.assertSummaryDocument(
        "/summary?as=8767", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testAsAs() {
    this.assertErrorStatusCode(
        "/summary?as=AS", 400);
  }

  @Test(timeout = 100)
  public void testAsas8767() {
    this.assertSummaryDocument(
        "/summary?as=as8767", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testAsas8767WithLeadingZeros() {
    this.assertSummaryDocument(
        "/summary?as=as008767", 1, new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testAsAsSpace8767() {
    this.assertErrorStatusCode(
        "/summary?as=AS 8767", 400);
  }

  @Test(timeout = 100)
  public void testAs8767Or7922() {
    this.assertSummaryDocument("/summary?as=8767,7922", 2,
        new String[] { "TorkaZ", "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testAsUnknown() {
    this.assertSummaryDocument("/summary?as=0", 1,
        new String[] {"TimMayTribute"}, 0, null);
  }

  @Test(timeout = 100)
  public void testAsAsUnknown() {
    this.assertSummaryDocument("/summary?as=as0", 1,
        new String[] {"TimMayTribute"}, 0, null);
  }

  @Test(timeout = 100)
  public void testAsAsUnknownWithLeadingZeros() {
    this.assertSummaryDocument("/summary?as=as0000", 1,
        new String[] {"TimMayTribute"}, 0, null);
  }

  @Test(timeout = 100)
  public void testAsTooLarge() {
    this.assertErrorStatusCode("/summary?as=4294967296", 400);
  }

  @Test(timeout = 100)
  public void testAsNegative() {
    this.assertErrorStatusCode("/summary?as=-3", 400);
  }

  @Test(timeout = 100)
  public void testAsNameComcast() {
    this.assertSummaryDocument("/summary?as_name=Comcast", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testAsNameComcastCable() {
    this.assertSummaryDocument("/summary?as_name=Comcast Cable",
        1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testAsNameCableComcast() {
    this.assertSummaryDocument("/summary?as_name=Cable Comcast",
        1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testAsNameMit() {
    this.assertSummaryDocument(
        "/summary?as_name=Massachusetts Institute of Technology",
        0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFlagRunning() {
    this.assertSummaryDocument(
        "/summary?flag=Running", 3, null, 1, null);
  }

  @Test(timeout = 100)
  public void testFlagValid() {
    this.assertSummaryDocument(
        "/summary?flag=Valid", 3, null, 3, null);
  }

  @Test(timeout = 100)
  public void testFlagFast() {
    this.assertSummaryDocument(
        "/summary?flag=Fast", 2, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFlagNamed() {
    this.assertSummaryDocument(
        "/summary?flag=Named", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFlagUnnamed() {
    this.assertSummaryDocument(
        "/summary?flag=Unnamed", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFlagV2Dir() {
    this.assertSummaryDocument(
        "/summary?flag=V2Dir", 2, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFlagGuard() {
    this.assertSummaryDocument(
        "/summary?flag=Guard", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFlagCool() {
    this.assertSummaryDocument(
        "/summary?flag=Cool", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenSince() {
    long dateMillis = DateTimeHelper.daysFromDate(4, 0, 0, 0, TEST_TIME);
    String dateString = DateTimeHelper.format(dateMillis,
        DateTimeHelper.ISO_YEARMONTHDAY_FORMAT);
    this.assertSummaryDocument(
        "/summary?first_seen_since=" + dateString, 0, null, 1, null);
  }

  @Test(timeout = 100)
  public void testLastSeenSince() {
    long dateMillis = DateTimeHelper.daysFromDate(3, 0, 0, 0, TEST_TIME);
    String dateString = DateTimeHelper.format(dateMillis,
        DateTimeHelper.ISO_YEARMONTHDAY_FORMAT);
    this.assertSummaryDocument(
        "/summary?last_seen_since=" + dateString, 2, null, 3, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysZeroToFour() {
    this.assertSummaryDocument(
        "/summary?first_seen_days=0-4", 0, null, 1, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysUpToThree() {
    this.assertSummaryDocument(
        "/summary?first_seen_days=-3", 0, null, 1, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysThree() {
    this.assertSummaryDocument(
        "/summary?first_seen_days=3", 0, null, 1, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysTwoToFive() {
    this.assertSummaryDocument(
        "/summary?first_seen_days=2-5", 0, null, 1, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysSevenToSixteen() {
    this.assertSummaryDocument(
        "/summary?first_seen_days=7-16", 2, null, 2, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysNinetysevenOrMore() {
    this.assertSummaryDocument(
        "/summary?first_seen_days=97-", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysNinetyeightOrMore() {
    this.assertSummaryDocument(
        "/summary?first_seen_days=98-", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysDashDash() {
    this.assertErrorStatusCode(
        "/summary?first_seen_days=--", 400);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysDashOneDash() {
    this.assertErrorStatusCode(
        "/summary?first_seen_days=-1-", 400);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysZeroDotDotOne() {
    this.assertErrorStatusCode(
        "/summary?first_seen_days=0..1", 400);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysElevenDigits() {
    this.assertErrorStatusCode(
        "/summary?first_seen_days=12345678901", 400);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysLargeTenDigitNumber() {
    this.assertErrorStatusCode(
        "/summary?first_seen_days=9999999999", 400);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysMaxInt() {
    this.assertSummaryDocument(
        "/summary?last_seen_days=" + Integer.MAX_VALUE, 0,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testFirstSeenDaysMaxIntPlusOne() {
    this.assertErrorStatusCode(
        "/summary?first_seen_days="
        + (((long) Integer.MAX_VALUE) + 1L), 400);
  }

  @Test(timeout = 100)
  public void testLastSeenDaysZero() {
    this.assertSummaryDocument(
        "/summary?last_seen_days=0", 1, null, 1, null);
  }

  @Test(timeout = 100)
  public void testLastSeenDaysUpToZero() {
    this.assertSummaryDocument(
        "/summary?last_seen_days=-0", 1, null, 1, null);
  }

  @Test(timeout = 100)
  public void testLastSeenDaysOneToThree() {
    this.assertSummaryDocument(
        "/summary?last_seen_days=1-3", 1, null, 2, null);
  }

  @Test(timeout = 100)
  public void testLastSeenDaysSixOrMore() {
    this.assertSummaryDocument(
        "/summary?last_seen_days=6-", 0, null, 0, null);
  }

  @Test(timeout = 100)
  public void testContactSteven() {
    this.assertSummaryDocument(
        "/summary?contact=Steven", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testContactStevenMurdoch() {
    this.assertSummaryDocument(
        "/summary?contact=Steven Murdoch", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testContactMurdochSteven() {
    this.assertSummaryDocument(
        "/summary?contact=Murdoch Steven", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testContactStevenDotMurdoch() {
    this.assertSummaryDocument(
        "/summary?contact=Steven.Murdoch", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testContactFbTokenFive() {
    this.assertSummaryDocument(
        "/summary?contact=<fb-token:5sR_K_zs2wM=>", 1, null, 0, null);
  }

  @Test(timeout = 100)
  public void testContactFbToken() {
    this.assertSummaryDocument(
        "/summary?contact=<fb-token:", 2, null, 0, null);
  }

  @Test(timeout = 100)
  public void testContactDash() {
    this.assertSummaryDocument(
        "/summary?contact=-", 2, null, 0, null);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightAscending() {
    this.assertSummaryDocument(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_ASC, 3,
        new String[] { "TorkaZ", "TimMayTribute", "Ferrari458" }, 3,
        null);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightDescending() {
    this.assertSummaryDocument(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_DES, 3,
        new String[] { "Ferrari458", "TimMayTribute", "TorkaZ" }, 3,
        null);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightAscendingTwice() {
    this.assertErrorStatusCode(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_ASC
        + "," + OrderParameterValues.CONSENSUS_WEIGHT_ASC, 400);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightAscendingThenDescending() {
    this.assertErrorStatusCode(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_ASC + ","
        + OrderParameterValues.CONSENSUS_WEIGHT_DES + "", 400);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightThenNickname() {
    this.assertErrorStatusCode(
        "/summary?order=consensus_weight,nickname", 400);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeight() {
    this.assertSummaryDocument(
        "/summary?order=CONSENSUS_WEIGHT", 3,
        new String[] { "TorkaZ", "TimMayTribute", "Ferrari458" }, 3,
        null);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightAscendingLimit1() {
    this.assertSummaryDocument(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_ASC
        + "&limit=1", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightDescendingLimit1() {
    this.assertSummaryDocument(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_DES
        + "&limit=1", 1,
        new String[] { "Ferrari458" }, 0, null);
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightFiveTimes() {
    this.assertErrorStatusCode(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_ASC + ","
        + OrderParameterValues.CONSENSUS_WEIGHT_ASC + ","
        + OrderParameterValues.CONSENSUS_WEIGHT_ASC + ","
        + OrderParameterValues.CONSENSUS_WEIGHT_ASC + ","
        + OrderParameterValues.CONSENSUS_WEIGHT_ASC, 400);
  }

  @Test(timeout = 100)
  public void testOrderFirstSeenThenConsensusWeight() {
    this.assertSummaryDocument(
        "/summary?order=" + OrderParameterValues.FIRST_SEEN_ASC + ","
        + OrderParameterValues.CONSENSUS_WEIGHT_ASC, 3,
        new String[] { "TimMayTribute", "Ferrari458", "TorkaZ" }, 3,
        new String[] { "Unnamed", "gummy", "ec2bridgercc7f31fe" });
  }

  @Test(timeout = 100)
  public void testOrderFirstSeenDescendingThenConsensusWeight() {
    this.assertSummaryDocument("/summary?order="
        + OrderParameterValues.FIRST_SEEN_DES + ","
        + OrderParameterValues.CONSENSUS_WEIGHT_ASC, 3,
        new String[] { "TorkaZ", "TimMayTribute", "Ferrari458" }, 3,
        new String[] { "ec2bridgercc7f31fe", "gummy", "Unnamed" });
  }

  @Test(timeout = 100)
  public void testOrderConsensusWeightThenFirstSeenDescending() {
    this.assertSummaryDocument(
        "/summary?order=" + OrderParameterValues.CONSENSUS_WEIGHT_ASC + ","
        + OrderParameterValues.FIRST_SEEN_DES, 3,
        new String[] { "TorkaZ", "TimMayTribute", "Ferrari458" }, 3,
        null);
  }

  @Test(timeout = 100)
  public void testOffsetOne() {
    this.assertSkippedReturnedTruncated(
        "/summary?offset=1", 1, 2, 0, 0, 3, 0);
  }

  @Test(timeout = 100)
  public void testOffsetAllRelays() {
    this.assertSkippedReturnedTruncated(
        "/summary?offset=3", 3, 0, 0, 0, 3, 0);
  }

  @Test(timeout = 100)
  public void testOffsetAllRelaysAndOneBridge() {
    this.assertSkippedReturnedTruncated(
        "/summary?offset=4", 3, 0, 0, 1, 2, 0);
  }

  @Test(timeout = 100)
  public void testOffsetAllRelaysAndAllBridges() {
    this.assertSkippedReturnedTruncated(
        "/summary?offset=6", 3, 0, 0, 3, 0, 0);
  }

  @Test(timeout = 100)
  public void testOffsetMoreThanAllRelaysAndAllBridges() {
    this.assertSkippedReturnedTruncated(
        "/summary?offset=7", 3, 0, 0, 3, 0, 0);
  }

  @Test(timeout = 100)
  public void testOffsetZero() {
    this.assertSkippedReturnedTruncated(
        "/summary?offset=0", 0, 3, 0, 0, 3, 0);
  }

  @Test(timeout = 100)
  public void testOffsetMinusOne() {
    this.assertSkippedReturnedTruncated(
        "/summary?offset=-1", 0, 3, 0, 0, 3, 0);
  }

  @Test(timeout = 100)
  public void testOffsetOneWord() {
    this.assertErrorStatusCode(
        "/summary?offset=one", 400);
  }

  @Test(timeout = 100)
  public void testLimitOne() {
    this.assertSkippedReturnedTruncated(
        "/summary?limit=1", 0, 1, 2, 0, 0, 3);
  }

  @Test(timeout = 100)
  public void testLimitAllRelays() {
    this.assertSkippedReturnedTruncated(
        "/summary?limit=3", 0, 3, 0, 0, 0, 3);
  }

  @Test(timeout = 100)
  public void testLimitAllRelaysAndOneBridge() {
    this.assertSkippedReturnedTruncated(
        "/summary?limit=4", 0, 3, 0, 0, 1, 2);
  }

  @Test(timeout = 100)
  public void testLimitAllRelaysAndAllBridges() {
    this.assertSkippedReturnedTruncated(
        "/summary?limit=6", 0, 3, 0, 0, 3, 0);
  }

  @Test(timeout = 100)
  public void testLimitMoreThanAllRelaysAndAllBridges() {
    this.assertSkippedReturnedTruncated(
        "/summary?limit=7", 0, 3, 0, 0, 3, 0);
  }

  @Test(timeout = 100)
  public void testLimitZero() {
    this.assertSkippedReturnedTruncated(
        "/summary?limit=0", 0, 0, 3, 0, 0, 3);
  }

  @Test(timeout = 100)
  public void testLimitMinusOne() {
    this.assertSkippedReturnedTruncated(
        "/summary?limit=-1", 0, 0, 3, 0, 0, 3);
  }

  @Test(timeout = 100)
  public void testLimitOneWord() {
    this.assertErrorStatusCode(
        "/summary?limit=one", 400);
  }

  @Test(timeout = 100)
  public void testFamilyTorkaZ() {
    this.assertSummaryDocument(
        "/summary?family=000C5F55BD4814B917CC474BD537F1A3B33CCE2A", 2,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testFamilyFerrari458() {
    this.assertSummaryDocument(
        "/summary?family=001C13B3A55A71B977CA65EC85539D79C653A3FC", 2,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testFamilyTimMayTribute() {
    this.assertSummaryDocument(
        "/summary?family=0025C136C1F3A9EEFE2AE3F918F03BFA21B5070B", 1,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testFamilyBridgegummy() {
    this.assertSummaryDocument(
        "/summary?family=0000831B236DFF73D409AD17B40E2A728A53994F", 0,
        null, 0, null);
  }

  @Test(timeout = 100)
  public void testFamily39Characters() {
    this.assertErrorStatusCode(
        "/summary?family=00000000000000000000000000000000000000", 400);
  }

  @Test
  public void testVersion02325() {
    this.assertSummaryDocument("/summary?version=0.2.3.25", 1,
        new String[] { "TorkaZ" }, 0, null);
  }

  @Test
  public void testVersion02324() {
    this.assertSummaryDocument("/summary?version=0.2.3.24-rc-dev", 1,
        new String[] { "TimMayTribute" }, 0, null);
  }

  @Test
  public void testVersion02326() {
    this.assertSummaryDocument("/summary?version=0.2.3.26", 0, null, 0, null);
  }

  @Test
  public void testVersion12345() {
    this.assertSummaryDocument("/summary?version=1.2.3.4.5", 0, null, 0, null);
  }

  @Test
  public void testVersionBlaBlaBla() {
    this.assertErrorStatusCode("/summary?version=bla-bla-bla", 400);
  }

  @Test
  public void testVersion0() {
    this.assertSummaryDocument("/summary?version=0", 2, null, 2, null);
  }

  @Test
  public void testVersion02() {
    this.assertSummaryDocument("/summary?version=0.2", 2, null, 2, null);
  }

  @Test
  public void testVersion023() {
    this.assertSummaryDocument("/summary?version=0.2.3", 2, null, 0, null);
  }

  @Test
  public void testVersion0232() {
    /* This is only correct when comparing strings, not when comparing parsed
     * version numbers. */
    this.assertSummaryDocument("/summary?version=0.2.3.2", 0, null, 0, null);
  }

  @Test
  public void testVersion023Dot() {
    /* This is also correct when comparing strings. */
    this.assertSummaryDocument("/summary?version=0.2.3.", 2, null, 0, null);
  }

  @Test
  public void testVersionStar() {
    this.assertErrorStatusCode("/summary?version=*", 400);
  }

  @Test
  public void testVersionRangeTo() {
    this.assertSummaryDocument("/summary?version=..0.2.3.24", 1, null, 1, null);
  }

  @Test
  public void testVersionRangeFrom() {
    this.assertSummaryDocument("/summary?version=0.2.3.25..", 1, null, 1, null);
  }

  @Test
  public void testVersionRangeFromTo() {
    this.assertSummaryDocument("/summary?version=0.2.3.24..0.2.3.25", 2, null,
        0, null);
  }

  @Test
  public void testVersionRangeFromToExchanged() {
    this.assertErrorStatusCode("/summary?version=0.2.3.25..0.2.3.24", 400);
  }

  @Test
  public void testVersionTwoSingles() {
    this.assertSummaryDocument("/summary?version=0.2.2.39,0.2.3.24", 1, null, 1,
        null);
  }

  @Test
  public void testVersionTwoOtherSingles() {
    this.assertSummaryDocument("/summary?version=0.2.2.39,0.2.4.4", 0, null, 2,
        null);
  }

  @Test
  public void testVersionSingleAndRange() {
    this.assertSummaryDocument("/summary?version=0.2.2.39,0.2.4..", 0, null, 2,
        null);
  }

  @Test
  public void testVersion0AndLater() {
    this.assertSummaryDocument("/summary?version=0..", 2, null, 2, null);
  }

  @Test
  public void testVersionJustTwoDots() {
    /* Need at least a start or an end. */
    this.assertErrorStatusCode("/summary?version=..", 400);
  }

  @Test
  public void testVersion0ThreeDots() {
    /* Parses as "all versions starting at 0.". */
    this.assertSummaryDocument("/summary?version=0...", 2, null, 2, null);
  }

  @Test
  public void testVersion0FourDots() {
    this.assertErrorStatusCode("/summary?version=0....", 400);
  }

  @Test
  public void testVersion1AndEarlier() {
    this.assertSummaryDocument("/summary?version=..1", 2, null, 2, null);
  }

  @Test(timeout = 100)
  public void testOperatingSystemLinux() {
    this.assertSummaryDocument(
        "/summary?os=linux", 1, new String[] {"TorkaZ"}, 0, null);
  }

  @Test(timeout = 100)
  public void testOperatingSystemLinuxMixedCaps() {
    this.assertSummaryDocument(
        "/summary?os=LiNUx", 1, new String[] {"TorkaZ"}, 0, null);
  }

  @Test(timeout = 100)
  public void testOperatingSystemLin() {
    this.assertSummaryDocument(
        "/summary?os=lin", 1, new String[] {"TorkaZ"}, 0, null);
  }

  @Test(timeout = 100)
  public void testOperatingSystemWindows() {
    this.assertSummaryDocument(
        "/summary?os=windows", 1, new String[] {"TimMayTribute"},
        1, new String[] {"gummy"});
  }

  @Test(timeout = 100)
  public void testOperatingSystemWindowsExperience() {
    this.assertSummaryDocument(
        "/summary?os=windows xp", 1, new String[] {"TimMayTribute"},
        0, null);
  }

  @Test(timeout = 100)
  public void testOperatingSystemWindows7() {
    this.assertSummaryDocument(
        "/summary?os=windows 7", 0, null, 1, new String[] {"gummy"});
  }

  @Test
  public void testHostNameDe() {
    this.assertSummaryDocument("/summary?host_name=de", 1, null, 0, null);
  }

  @Test
  public void testHostNameE() {
    this.assertSummaryDocument("/summary?host_name=e", 1, null, 0, null);
  }

  @Test
  public void testHostNameDotDe() {
    this.assertSummaryDocument("/summary?host_name=.de", 1, null, 0, null);
  }

  @Test
  public void testHostNameOnlineDe() {
    this.assertSummaryDocument("/summary?host_name=online.de", 1, null, 0,
        null);
  }

  @Test
  public void testHostNameOnlineDeSomeCapitalized() {
    this.assertSummaryDocument("/summary?host_name=onLiNe.dE", 1, null, 0,
        null);
  }

  @Test
  public void testHostNameOnline() {
    this.assertSummaryDocument("/summary?host_name=online", 0, null, 0, null);
  }

  @Test
  public void testHostNameTorkaZFull() {
    this.assertSummaryDocument(
        "/summary?host_name=ppp-62-216-201-221.dynamic.mnet-online.de",
        1, null, 0, null);
  }

  @Test
  public void testHostNameTorkaZSub() {
    this.assertSummaryDocument(
        "/summary?host_name=sub.ppp-62-216-201-221.dynamic.mnet-online.de",
        0, null, 0, null);
  }

  @Test
  public void testHostNameNet() {
    this.assertSummaryDocument("/summary?host_name=net", 1, null, 0, null);
  }

  @Test
  public void testHostNameCom() {
    this.assertSummaryDocument("/summary?host_name=com", 0, null, 0, null);
  }

  @Test
  public void testHostNameDollar() {
    this.assertErrorStatusCode("/summary?host_name=$", 400);
  }

  @Test
  public void testHostNameUmlaut() {
    this.assertErrorStatusCode("/summary?host_name=äöü", 400);
  }

  @Test
  public void testRecommendedVersionTrue() {
    this.assertSummaryDocument("/summary?recommended_version=true", 1,
        new String[] { "TorkaZ" }, 1, new String[] { "ec2bridgercc7f31fe" });
  }

  @Test
  public void testRecommendedVersionFalse() {
    this.assertSummaryDocument("/summary?recommended_version=false", 1,
        new String[] { "TimMayTribute" }, 1, new String[] { "gummy" });
  }

  @Test
  public void testRecommendedVersionTrueCapitalized() {
    this.assertSummaryDocument("/summary?recommended_version=TRUE", 1,
        new String[] { "TorkaZ" }, 1,new String[] { "ec2bridgercc7f31fe" });
  }

  @Test
  public void testRecommendedVersionNull() {
    this.assertErrorStatusCode("/summary?recommended_version=null", 400);
  }
}
