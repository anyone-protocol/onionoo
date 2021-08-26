package org.torproject.metrics.onionoo.docs;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

public class DetailsDocumentTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  private DetailsDocument createDetailsDocumentRelay() {
    return new DetailsDocument();
  }

  @Test()
  public void testDocumentExists() throws JsonProcessingException {
    DetailsDocument relay = this.createDetailsDocumentRelay();
    relay.setFingerprint("BE45FFE2F55E29DA327346E9D44A5203086E25B0");
    relay.setRunning(true);
    relay.setOverloadGeneralTimestamp(1628168400000L);
    assertEquals(
        "BE45FFE2F55E29DA327346E9D44A5203086E25B0",
        relay.getFingerprint());
    assertEquals(
        "{\"fingerprint\":\"BE45FFE2F55E29DA327346E9D44A5203086E25B0\","
        + "\"running\":true,\"overload_general_timestamp\":1628168400000}",
        objectMapper.writeValueAsString(relay));
  }

  @Test()
  public void testOverloadTimestampDoesNotExists()
        throws JsonProcessingException {
    DetailsDocument relay = this.createDetailsDocumentRelay();
    relay.setFingerprint("BE45FFE2F55E29DA327346E9D44A5203086E25B0");
    relay.setRunning(true);
    assertEquals(
        "BE45FFE2F55E29DA327346E9D44A5203086E25B0",
        relay.getFingerprint());
    assertEquals(
        "{\"fingerprint\":\"BE45FFE2F55E29DA327346E9D44A5203086E25B0\","
        + "\"running\":true}",
        objectMapper.writeValueAsString(relay));
  }
}
