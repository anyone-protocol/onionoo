/* Copyright 2014--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import static org.junit.Assert.assertEquals;

import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorParser;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ExtraInfoDescriptor;
import org.torproject.metrics.onionoo.docs.BandwidthStatus;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.DummyDocumentStore;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class BandwidthStatusUpdaterTest {

  private DummyDocumentStore docStore;

  @Before
  public void createDummyDocumentStore() {
    this.docStore = new DummyDocumentStore();
    DocumentStoreFactory.setDocumentStore(this.docStore);
  }

  @Test
  public void testDescriptors() {
    BandwidthStatusUpdater ndsu = new BandwidthStatusUpdater();
    DescriptorParser dp = DescriptorSourceFactory.createDescriptorParser();
    String descString = RELAY1 + RELAY2;
    for (Descriptor desc : dp.parseDescriptors(descString.getBytes(),
        new File("dummy"), "dummy")) {
      assertTrue(desc.getClass().getName(),
          desc instanceof ExtraInfoDescriptor);
      ndsu.processDescriptor(desc, true);
    }
    assertEquals(2, this.docStore.getPerformedStoreOperations());
    assertEquals(2, this.docStore.storedDocuments.size());
    BandwidthStatus bs = this.docStore.getDocument(BandwidthStatus.class, FP1);
    assertEquals(false, bs.isDirty());
  }

  @Test
  public void testWithOverloadRatelimits() {
    BandwidthStatusUpdater ndsu = new BandwidthStatusUpdater();
    DescriptorParser dp = DescriptorSourceFactory.createDescriptorParser();
    String descString = RELAY1 + RELAY3;
    for (Descriptor desc : dp.parseDescriptors(descString.getBytes(),
        new File("dummy"), "dummy")) {
      assertTrue(desc.getClass().getName(),
          desc instanceof ExtraInfoDescriptor);
      ndsu.processDescriptor(desc, true);
    }
    assertEquals(2, this.docStore.getPerformedStoreOperations());
    assertEquals(2, this.docStore.storedDocuments.size());
    BandwidthStatus bs = this.docStore.getDocument(BandwidthStatus.class, FP3);
    assertEquals(1633582800000L, bs.getOverloadRatelimitsTimestamp());
  }

  @Test
  public void testWithoutOverloadRatelimits() {
    BandwidthStatusUpdater ndsu = new BandwidthStatusUpdater();
    DescriptorParser dp = DescriptorSourceFactory.createDescriptorParser();
    String descString = RELAY1 + RELAY3;
    for (Descriptor desc : dp.parseDescriptors(descString.getBytes(),
        new File("dummy"), "dummy")) {
      assertTrue(desc.getClass().getName(),
          desc instanceof ExtraInfoDescriptor);
      ndsu.processDescriptor(desc, true);
    }
    assertEquals(2, this.docStore.getPerformedStoreOperations());
    assertEquals(2, this.docStore.storedDocuments.size());
    BandwidthStatus bs = this.docStore.getDocument(BandwidthStatus.class, FP1);
    assertEquals(-1L, bs.getOverloadRatelimitsTimestamp());
  }

  private static final String FP1 = "01EE53C3542326D2EA98E77658D40DE8C960EE0F";
  private static final String FP3 = "C1FFF27A38DF8DC8B310D078C13E23F080AF2957";

  private static final String RELAY1 = "@type extra-info 1.0\n"
      + "extra-info Unnamed 01EE53C3542326D2EA98E77658D40DE8C960EE0F\n"
      + "identity-ed25519\n"
      + "-----BEGIN ED25519 CERT-----\n"
      + "AQQABu1dAb6zQYMFNmDbcdhsQosaVv0rNa7Pu1tg+gwVAl+/ooyDAQAgBAD1RavW\n"
      + "KwG0NyDAO2JRLELgJe9zVkOKJ3GGR2odHO18VqNGVhFTarRbmpF72V2sFOoK/gVM\n"
      + "z4Mi562CQKgFQ4im1e3VHmNXmqS+v/D78fCRaqFzPKGCCEjzg0wpjIUG7AY=\n"
      + "-----END ED25519 CERT-----\n"
      + "published 2021-10-07 12:19:12\n"
      + "write-history 2021-10-06 20:21:16 (86400 s) 2417664,3562496,2278400,"
      + "2227200,2203648\n"
      + "read-history 2021-10-06 20:21:16 (86400 s) 23422976,25457664,26384384,"
      + "21670912,20871168\n"
      + "dirreq-write-history 2021-09-23 20:21:16 (86400 s) 10240,0,0,0,0\n"
      + "dirreq-read-history 2021-09-23 20:21:16 (86400 s) 20480,0,0,0,0\n"
      + "hidserv-stats-end 2021-10-06 20:22:47 (86400 s)\n"
      + "hidserv-rend-relayed-cells -975 delta_f=2048 epsilon=0.30 "
      + "bin_size=1024\n"
      + "hidserv-dir-onions-seen 35 delta_f=8 epsilon=0.30 bin_size=8\n"
      + "padding-counts 2021-10-06 20:22:47 (86400 s) bin-size=10000 "
      + "write-drop=0"
      + "write-pad=10000 write-total=10000 read-drop=0 read-pad=10000 "
      + "read-total=10000 enabled-read-pad=0 enabled-read-total=0 "
      + "enabled-write-pad=0 enabled-write-total=0 max-chanpad-timers=0\n"
      + "router-sig-ed25519 Y5EUmgHUdDzpsFuy24sUit5kI3tmyKPQ6XPPSn52+p+6JAt"
      + "UL3xSBlo93QMYNFmrtKQsqjKe2hbxWpT932C7Cw\n"
      + "router-signature\n"
      + "-----BEGIN SIGNATURE-----\n"
      + "QnUQwORMwogeYihwDreLdQSl7nITE0FU8r+AbTaqn+huif3R+tEoQ0wjVOC2jQ1M\n"
      + "JhghiyXH/PxNAe/wH10N8Ee2Z3gYhtd3cCJkk9nRzE0xuIWBFSKwFRXN6ZpijRQB\n"
      + "gWQhwwIJkBFktik96pugE7ND/hurQJOoBZb1U8yG8P8=\n"
      + "-----END SIGNATURE-----\n";

  private static final String RELAY2 = "@type extra-info 1.0\n"
      + "extra-info Unnamed 1EF36916CFF8F154AF8A9A5B92E022318B89A125\n"
      + "identity-ed25519\n"
      + "-----BEGIN ED25519 CERT-----\n"
      + "AQQABuzSAcfp+fZ40a4zmiFKJ0OsK1LYL28XamIaiLaajLCRyktzAQAgBABsXYiZ\n"
      + "bn1AT0KFYSWI3Qa1u9GS7GmdREmJtrvfDq4dBaQEFxTf0CXyabjVx0bOfHZT72YM\n"
      + "Dkw62Xf0y9HaaFy+D/K+Za5OfquF+2BTRYRx0bNhbz2dnxsSuXbFDrVI9gQ=\n"
      + "-----END ED25519 CERT-----\n"
      + "published 2021-10-07 11:24:01\n"
      + "write-history 2021-10-07 01:33:25 (86400 s) 3277824,1985536,1930240,"
      + "1893376,1833984\n"
      + "read-history 2021-10-07 01:33:25 (86400 s) 25129984,21022720,24909824,"
      + "23346176,20610048\n"
      + "dirreq-write-history 2021-09-13 01:33:25 (86400 s) 0,0,14336\n"
      + "dirreq-read-history 2021-09-13 01:33:25 (86400 s) 0,0,20480\n"
      + "hidserv-stats-end 2021-10-07 01:35:32 (86400 s)\n"
      + "hidserv-rend-relayed-cells 539 delta_f=2048 epsilon=0.30 "
      + "bin_size=1024\n"
      + "hidserv-dir-onions-seen 10 delta_f=8 epsilon=0.30 bin_size=8\n"
      + "padding-counts 2021-10-07 01:35:45 (86400 s) bin-size=10000 "
      + "write-drop=0 write-pad=10000 write-total=10000 read-drop=0 "
      + "read-pad=10000 read-total=10000 enabled-read-pad=0 "
      + "enabled-read-total=0 "
      + "enabled-write-pad=0 enabled-write-total=0 max-chanpad-timers=0\n"
      + "router-sig-ed25519 9SFdJut+OmqI6Yu6fZ6nguaCu3oIu6kwMWIWJrAAMYK6G7lO2"
      + "NJWQ0NOKhtIh+83Pm1xiByvPXT8XVyVhxZNAA\n"
      + "router-signature\n"
      + "-----BEGIN SIGNATURE-----\n"
      + "R1q5rHANL80WKIA84nFVX/K75OKwLZoO1ZNCUQPClXkiRJbzG/nL18VM9mC6E/pE\n"
      + "T+Nq6nzlQKCvtQX5gSKowysgjqH0Vx/T+xNW9hWbPl8dvVuA1vXiBbE304D++98W\n"
      + "gZ00NPSPHiwqYTYZEwn9MLGB8Ga+8lQKJja+stxI7bQ=\n"
      + "-----END SIGNATURE-----\n";

  private static final String RELAY3 = "@type extra-info 1.0\n"
      + "extra-info ylxdzsw C1FFF27A38DF8DC8B310D078C13E23F080AF2957\n"
      + "identity-ed25519\n"
      + "-----BEGIN ED25519 CERT-----\n"
      + "AQQABu6iAWv2WTGXw1SLjnmA97j/rUgNL4DSu+ASR0x7KpTrk6ZLAQAgBACqbSsR\n"
      + "F3GIjW+bEsQpqh3x5hDjsQHeTWAkUmgeiLZopEnRG0h3YlKROXFYuBPgPg/+qNDg\n"
      + "Pxo++hxIsvI68Vgj99SNxmBIgYEtDOUaN/37PyhqgWLz/UGlaCRiaJALzQE=\n"
      + "-----END ED25519 CERT-----\n"
      + "published 2021-10-07 13:27:27\n"
      + "write-history 2021-10-07 11:29:19 (86400 s) 4590592,6473728,4418560,"
      + "4402176,4314112\n"
      + "read-history 2021-10-07 11:29:19 (86400 s) 20384768,26002432,20875264,"
      + "19668992,18417664\n"
      + "ipv6-write-history 2021-07-23 12:39:10 (86400 s) 57667584,45329408,"
      + "53468160,62139392,50221056\n"
      + "ipv6-read-history 2021-07-23 12:39:10 (86400 s) 83062784,56497152,"
      + "76729344,85133312,56424448\n"
      + "dirreq-write-history 2021-07-23 12:39:10 (86400 s) 2280448,2203648,"
      + "1436672,1805312,1504256\n"
      + "dirreq-read-history 2021-07-23 12:39:10 (86400 s) 187392,99328,112640,"
      + "140288,118784\n"
      + "geoip-db-digest 900E9BD6C8B8679A1539F7F316974E86618038B2\n"
      + "geoip6-db-digest 456BCA0796A27F8C80674EE89B1323F8310BED39\n"
      + "dirreq-stats-end 2021-10-06 17:16:59 (86400 s)\n"
      + "dirreq-v3-ips\n"
      + "dirreq-v3-reqs\n"
      + "dirreq-v3-resp ok=0,not-enough-sigs=0,unavailable=0,not-found=0,"
      + "not-modified=0,busy=0\n"
      + "dirreq-v3-direct-dl complete=0,timeout=0,running=0\n"
      + "dirreq-v3-tunneled-dl complete=0,timeout=0,running=0\n"
      + "hidserv-stats-end 2021-10-06 17:16:59 (86400 s)\n"
      + "hidserv-rend-relayed-cells 3112 delta_f=2048 epsilon=0.30 "
      + "bin_size=1024\n"
      + "hidserv-dir-onions-seen -9 delta_f=8 epsilon=0.30 bin_size=8\n"
      + "hidserv-v3-stats-end 2021-10-07 12:00:00 (86400 s)\n"
      + "hidserv-rend-v3-relayed-cells -839 delta_f=2048 epsilon=0.30 "
      + "bin_size=1024\n"
      + "hidserv-dir-v3-onions-seen 6 delta_f=8 epsilon=0.30 bin_size=8\n"
      + "padding-counts 2021-10-06 17:17:14 (86400 s) bin-size=10000 "
      + "write-drop=0 write-pad=10000 write-total=10000 read-drop=0 "
      + "read-pad=10000 read-total=10000 enabled-read-pad=10000 "
      + "enabled-read-total=10000 enabled-write-pad=10000 "
      + "enabled-write-total=10000 max-chanpad-timers=0\n"
      + "overload-ratelimits 1 2021-10-07 05:00:00 122880 819200 221 105\n"
      + "router-sig-ed25519 mMnzvnEA3ex7oPiYjEDj3E8D3bsDhNIMtqgEHSAMfk+k1zY5NK"
      + "0ZsOGLLfNVadBjFc7bGE30PJgnzxPWgDt6Bg\n"
      + "router-signature\n"
      + "-----BEGIN SIGNATURE-----\n"
      + "Hq/0n126zkG7Os2wWLpWTJbWHE8Pk4gC70t0PjXnG+YRKT23J4q9Yb4aOhQVW4aK\n"
      + "/LThNLNSHRywpw0ZoBM50lQ46IuuHrL3oetpgZ+++SNpGSx0XphacM3UCgMY4NqD\n"
      + "h1g/V8idCb7CUdW45alifQ+IgHklTbHJEs4ZYDerR0U=\n"
      + "-----END SIGNATURE-----\n";
}
