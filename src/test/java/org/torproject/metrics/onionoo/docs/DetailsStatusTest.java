package org.torproject.metrics.onionoo.docs;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

public class DetailsStatusTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Test()
  public void testDetailStatus() throws JsonProcessingException {
    DetailsStatus detailsStatus = new DetailsStatus();
    detailsStatus.setBandwidthRate(640000);
    detailsStatus.setOverloadGeneralTimestamp(1628168400000L);
    assertEquals(new Long(640000), new Long(detailsStatus.getBandwidthRate()));
    assertEquals(
        "{\"bandwidth_rate\":640000,"
        + "\"overload_general_timestamp\":1628168400000,"
        + "\"is_relay\":false,\"running\":false,"
        + "\"first_seen_millis\":0,\"last_seen_millis\":0,"
        + "\"or_port\":0,\"dir_port\":0,"
        + "\"consensus_weight\":0,\"last_changed_or_address_or_port\":0}",
        objectMapper.writeValueAsString(detailsStatus));
  }
}
