package org.torproject.metrics.onionoo.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.torproject.metrics.onionoo.docs.DetailsDocument;
import org.torproject.metrics.onionoo.docs.DetailsStatus;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.DummyDocumentStore;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.TreeSet;

public class DetailsDocumentWriterTest {

  private DummyDocumentStore documentStore;

  @Before
  public void createDummyDocumentStore() {
    this.documentStore = new DummyDocumentStore();
    DocumentStoreFactory.setDocumentStore(this.documentStore);
  }

  @Test
  public void testNoDetailsStatuses() {
    DetailsDocumentWriter writer = new DetailsDocumentWriter();
    writer.writeDocuments(-1L);
    assertEquals("Without statuses, no documents should be written.", 0,
        this.documentStore.getPerformedStoreOperations());
  }

  private static final String GABELMOO_FINGERPRINT =
      "F2044413DAC2E02E3D6BCF4735A19BCA1DE97281";

  private static final String GABELMOO_OR_ADDRESS =
      "[2001:638:a000:4140::ffff:189]:443]";

  @Test
  public void testAdvertisedAndReachableOrAddress() {
    DetailsStatus status = new DetailsStatus();
    status.setRelay(true);
    status.setAdvertisedOrAddresses(Arrays.asList(GABELMOO_OR_ADDRESS));
    status.setOrAddressesAndPorts(new TreeSet<>(Arrays.asList(
        GABELMOO_OR_ADDRESS)));
    this.documentStore.addDocument(status, GABELMOO_FINGERPRINT);
    DetailsDocumentWriter writer = new DetailsDocumentWriter();
    writer.writeDocuments(-1L);
    assertEquals("One document should be written.", 1,
        this.documentStore.getPerformedStoreOperations());
    DetailsDocument document = this.documentStore.getDocument(
        DetailsDocument.class, GABELMOO_FINGERPRINT);
    assertNotNull("There should be a document for the given fingerprint.",
        document);
    assertNull("Document should not contain any unreachable OR addresses.",
        document.getUnreachableOrAddresses());
  }

  @Test
  public void testUnadvertisedButSomehowReachableOrAddress() {
    DetailsStatus status = new DetailsStatus();
    status.setRelay(true);
    status.setOrAddressesAndPorts(new TreeSet<>(Arrays.asList(
        GABELMOO_OR_ADDRESS)));
    this.documentStore.addDocument(status, GABELMOO_FINGERPRINT);
    DetailsDocumentWriter writer = new DetailsDocumentWriter();
    writer.writeDocuments(-1L);
    assertEquals("One document should be written.", 1,
        this.documentStore.getPerformedStoreOperations());
    DetailsDocument document = this.documentStore.getDocument(
        DetailsDocument.class, GABELMOO_FINGERPRINT);
    assertNotNull("There should be a document for the given fingerprint.",
        document);
    assertNull("Document should not contain unreachable OR addresses.",
        document.getUnreachableOrAddresses());
  }

  @Test
  public void testAdvertisedButUnreachableOrAddress() {
    DetailsStatus status = new DetailsStatus();
    status.setRelay(true);
    status.setAdvertisedOrAddresses(Arrays.asList(GABELMOO_OR_ADDRESS));
    this.documentStore.addDocument(status, GABELMOO_FINGERPRINT);
    DetailsDocumentWriter writer = new DetailsDocumentWriter();
    writer.writeDocuments(-1L);
    assertEquals("One document should be written.", 1,
        this.documentStore.getPerformedStoreOperations());
    DetailsDocument document = this.documentStore.getDocument(
        DetailsDocument.class, GABELMOO_FINGERPRINT);
    assertNotNull("There should be a document for the given fingerprint.",
        document);
    assertEquals("Document should contain one unreachable OR address.",
        Arrays.asList(GABELMOO_OR_ADDRESS),
        document.getUnreachableOrAddresses());
  }

  @Test
  public void testContactDetails() {
    DetailsStatus status = new DetailsStatus();
    status.setRelay(true);
    status.setAdvertisedOrAddresses(Arrays.asList(GABELMOO_OR_ADDRESS));
    status.setOrAddressesAndPorts(new TreeSet<>(Arrays.asList(
        GABELMOO_OR_ADDRESS)));
    status.setContact("gabelmoo@torproject.org");
    this.documentStore.addDocument(status, GABELMOO_FINGERPRINT);
    DetailsDocumentWriter writer = new DetailsDocumentWriter();
    writer.writeDocuments(-1L);
    assertEquals("One document should be written.", 1,
        this.documentStore.getPerformedStoreOperations());
    DetailsDocument document = this.documentStore.getDocument(
        DetailsDocument.class, GABELMOO_FINGERPRINT);
    assertNotNull("There should be a document for the given fingerprint.",
        document);
    assertEquals("gabelmoo@torproject.org",
        document.getContact());
  }

  @Test
  public void testContactDetailsWithPercentCharacters() {
    DetailsStatus status = new DetailsStatus();
    status.setRelay(true);
    status.setAdvertisedOrAddresses(Arrays.asList(GABELMOO_OR_ADDRESS));
    status.setOrAddressesAndPorts(new TreeSet<>(Arrays.asList(
        GABELMOO_OR_ADDRESS)));
    status.setContact("gabelmoo%@torproject.org (% 2+6)");
    this.documentStore.addDocument(status, GABELMOO_FINGERPRINT);
    DetailsDocumentWriter writer = new DetailsDocumentWriter();
    writer.writeDocuments(-1L);
    assertEquals("One document should be written.", 1,
        this.documentStore.getPerformedStoreOperations());
    DetailsDocument document = this.documentStore.getDocument(
        DetailsDocument.class, GABELMOO_FINGERPRINT);
    assertNotNull("There should be a document for the given fingerprint.",
        document);
    assertEquals("gabelmoo@torproject.org ( 2+6)",
        document.getContact());
  }
}
