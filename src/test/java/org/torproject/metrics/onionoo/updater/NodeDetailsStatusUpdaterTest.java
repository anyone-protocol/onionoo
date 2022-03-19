/* Copyright 2014--2020 The Tor Project
 * See LICENSE for licensing information */

package org.torproject.metrics.onionoo.updater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.torproject.descriptor.BridgePoolAssignment;
import org.torproject.descriptor.Descriptor;
import org.torproject.descriptor.DescriptorParser;
import org.torproject.descriptor.DescriptorSourceFactory;
import org.torproject.descriptor.ServerDescriptor;
import org.torproject.metrics.onionoo.docs.DetailsStatus;
import org.torproject.metrics.onionoo.docs.DocumentStoreFactory;
import org.torproject.metrics.onionoo.docs.DummyDocumentStore;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class NodeDetailsStatusUpdaterTest {

  private DummyDocumentStore docStore;

  @Before
  public void createDummyDocumentStore() {
    this.docStore = new DummyDocumentStore();
    DocumentStoreFactory.setDocumentStore(this.docStore);
  }

  @Test
  public void testExitReset() {
    NodeDetailsStatusUpdater ndsu = new NodeDetailsStatusUpdater(null, null);
    DescriptorParser dp = DescriptorSourceFactory.createDescriptorParser();
    String descString = RELAY1 + PUB1 + RELAY2 + POLICY1 + RELAY3;
    for (Descriptor desc : dp.parseDescriptors(descString.getBytes(),
        new File("dummy"), "dummy")) {
      assertTrue(desc.getClass().getName(), desc instanceof ServerDescriptor);
      ndsu.processDescriptor(desc, true);
    }
    assertEquals(1, this.docStore.getPerformedStoreOperations());
    assertEquals(1, this.docStore.storedDocuments.size());
    DetailsStatus dd = this.docStore.getDocument(DetailsStatus.class, FP);
    assertNotNull("docs: " + this.docStore.storedDocuments, dd);
    assertEquals("Found: " + dd.getExitPolicy(), 1, dd.getExitPolicy().size());
    assertEquals("Found: " + dd.getExitPolicy().get(0),
        "reject *:25", dd.getExitPolicy().get(0));
    List<String> acc = dd.getExitPolicyV6Summary().get("accept");
    List<String> rej = dd.getExitPolicyV6Summary().get("reject");
    assertNull(rej);
    assertEquals("Found: " + dd.getExitPolicyV6Summary(), "23", acc.get(0));
    assertEquals("Found: " + dd.getExitPolicyV6Summary(), "42", acc.get(1));

    descString = RELAY1 + PUB2 + RELAY2 + POLICY2 + RELAY3;
    for (Descriptor desc : dp.parseDescriptors(descString.getBytes(),
        new File("dummy"), "dummy")) {
      assertTrue(desc.getClass().getName(), desc instanceof ServerDescriptor);
      ndsu.processDescriptor(desc, true);
    }
    assertEquals(2, this.docStore.getPerformedStoreOperations());
    assertEquals(1, this.docStore.storedDocuments.size());
    dd = this.docStore.getDocument(DetailsStatus.class, FP);
    assertNotNull("docs: " + this.docStore.storedDocuments, dd);
    assertEquals("Found: " + dd.getExitPolicy(), 1, dd.getExitPolicy().size());
    assertEquals("Found: " + dd.getExitPolicy().get(0),
        "reject *:*", dd.getExitPolicy().get(0));
    assertNull(dd.getExitPolicyV6Summary());
  }

  @Test
  public void testBridgePoolAssignments() {
    NodeDetailsStatusUpdater ndsu = new NodeDetailsStatusUpdater(null, null);
    DescriptorParser dp = DescriptorSourceFactory.createDescriptorParser();
    String descString = BRIDGE1;
    for (Descriptor desc : dp.parseDescriptors(descString.getBytes(),
        new File("dummy"), "dummy")) {
      assertTrue(desc.getClass().getName(), desc instanceof ServerDescriptor);
      ndsu.processDescriptor(desc, true);
    }
    assertEquals(1, this.docStore.getPerformedStoreOperations());
    assertEquals(1, this.docStore.storedDocuments.size());
    DetailsStatus dd = this.docStore.getDocument(DetailsStatus.class, FPB);
    assertNotNull("docs: " + this.docStore.storedDocuments, dd);
    descString = BPA;
    for (Descriptor desc : dp.parseDescriptors(descString.getBytes(),
        new File("dummy"), "dummy")) {
      assertTrue(desc.getClass().getName(),
          desc instanceof BridgePoolAssignment);
      ndsu.processDescriptor(desc, true);
      ndsu.updateStatuses();
    }
    assertEquals(1, this.docStore.getPerformedStoreOperations());
    assertEquals(1, this.docStore.storedDocuments.size());
    dd = this.docStore.getDocument(DetailsStatus.class, FPB);
    assertNotNull(dd);
  }

  private static final String FP = "42CAF9C0588BBADDD338025E8F2D3CCF35CEEC25";
  private static final String FPB = "6306C870BE7415EB7167ED2E9C1224F28B7E6C61";

  private static final String BPA = "@type bridge-pool-assignment 1.0\n"
      + "bridge-pool-assignment 2022-03-18 23:33:42\n"
      + "6306C870BE7415EB7167ED2E9C1224F28B7E6C61 moat "
      + "transport=obfs4 ip=4 blocklist=ru\n";

  private static final String BRIDGE1 = "@type bridge-server-descriptor 1.2\n"
    + "router DockerObfs4Bridge 10.136.176.127 50451 0 0\n"
    + "master-key-ed25519 J6M6MmuZ2joRKaihSB1LDym+WiuUeId8+9nvtI1Sd1M\n"
    + "platform Tor 0.4.6.10 on Linux\n"
    + "proto Cons=1-2 Desc=1-2 DirCache=2 FlowCtrl=1 HSDir=2 HSIntro=4-5 "
    + "HSRend=1-2 Link=1-5 LinkAuth=1,3 Microdesc=1-2 Padding=2 Relay=1-3\n"
    + "published 2022-03-17 08:07:11\n"
    + "fingerprint 6306 C870 BE74 15EB 7167 ED2E 9C12 24F2 8B7E 6C61\n"
    + "uptime 113883\n"
    + "bandwidth 1073741824 1073741824 81352\n"
    + "extra-info-digest 29ECA238A04F0D00E941B2CC430393604430B7D4 "
    + "X0jHhL7ygWblKzjoTGMG4Eikz8Sqo2X9eIbrKzVANNw\n"
    + "hidden-service-dir\n"
    + "contact somebody\n"
    + "bridge-distribution-request any\n"
    + "ntor-onion-key kysCjXGfMAx6AdejV2r34d59atkXFkvLNT/9ADcd73M\n"
    + "reject *:*\n"
    + "tunnelled-dir-server\n"
    + "router-digest-sha256 t+rUo6bm75/Qywapm5TTTnY2HGmxZqFAI1/ON2IxNP4\n"
    + "router-digest 78E7EDA527387064BB4410FC8BAED6DA0902BFE3\n";

  private static final String RELAY1 = "@type server-descriptor 1.0\n"
      + "router impedance 84.201.150.89 443 0 0\n"
      + "identity-ed25519\n"
      + "-----BEGIN ED25519 CERT-----\n"
      + "AQQABkIAAS889JQwk/Acb0kWwkX1lolsL72YAGUvNgrLjz8fxczJAQAgBACw4otT\n"
      + "Ajr+/0iRTRaCbdHs7BHSZ5qA3c4DG6a71zMzM0ismH/bVLbTuIsRrOuBfpOux+Cs\n"
      + "ONO0fGuk5uazrY0iGcKJtZckyW4W7RYLkeGdNnYJ6BpshTEP0Rd61roeBQs=\n"
      + "-----END ED25519 CERT-----\n"
      + "master-key-ed25519 sOKLUwI6/v9IkU0Wgm3R7OwR0meagN3OAxumu9czMzM\n"
      + "platform Tor 0.2.7.6 on Linux\n"
      + "protocols Link 1 2 Circuit 1\n";

  private static final String PUB1 = "published 2016-10-02 15:01:09\n";

  private static final String PUB2 = "published 2016-10-03 15:01:09\n";

  private static final String RELAY2
      = "fingerprint 42CA F9C0 588B BADD D338 025E 8F2D 3CCF 35CE EC25\n"
      + "uptime 329888\n"
      + "bandwidth 10240000 10240000 167976\n"
      + "extra-info-digest 43A1C5D7EBA550CEC7465E20E3027772D5C552DD "
      + "O3s96zE6cCXRrv3+KKpHm20GATS1s/9jyxbEv91v9mk\n"
      + "onion-key\n"
      + "-----BEGIN RSA PUBLIC KEY-----\n"
      + "MIGJAoGBAMyCYbuR3p6mQDGSjkeO37/BUsGEPiFtKsd7e3m59cXB5oXmEqz0n2dp\n"
      + "6qQAfyjPLP7Q1VZvvHxhD9n/qVXOmJRJGdlzhNBx1kIfvFhqKPop68cbwkcOlJ8r\n"
      + "c3RjwDiv24q2kVAAq+NsYEhVECeshodf1A0off3J/cNsP6zXHn2HAgMBAAE=\n"
      + "-----END RSA PUBLIC KEY-----\n"
      + "signing-key\n"
      + "-----BEGIN RSA PUBLIC KEY-----\n"
      + "MIGJAoGBAMl++uLczhgkVvFLWuPON5Ynk820A/5W4Sm/tAg5klhRj1fvn5SxnSIZ\n"
      + "l4VvAHSCZvojqEDCQvWwJUDEfEgT7tSlkq0Xjfsr7IhipnOkqhJFceLVCCdPHkkn\n"
      + "L4maXPKcJ+JjhRQABEUM/+HOhBJHWyJ2V1Dc2C/8VISH+bG6BE/1AgMBAAE=\n"
      + "-----END RSA PUBLIC KEY-----\n"
      + "onion-key-crosscert\n"
      + "-----BEGIN CROSSCERT-----\n"
      + "AY2c8r2+VIHDo5ga+vSCfOt327496XcMSMJp/9DNQrWiXk+Xwr9z4cTTvcHun7Tc\n"
      + "A17/Gjyo2yXDJABOQyZFbbKlCNUaoddOJc/s9hLCnkB//pxkO3sz7BwF+eOxqr9z\n"
      + "u4TV8c2yOPqPybsm5ctmNmmP6eecUcNFmBA4uX7QSMs=\n"
      + "-----END CROSSCERT-----\n"
      + "ntor-onion-key-crosscert 0\n"
      + "-----BEGIN ED25519 CERT-----\n"
      + "AQoABkGYAbDii1MCOv7/SJFNFoJt0ezsEdJnmoDdzgMbprvXMzMzAAN9vMECBiJ2\n"
      + "4Gbu9L3oZ983R5LwaE92TObml1anqe7W5uQDSskkIpEauL8tt029rS+xzQ7pCpdy\n"
      + "EgyktjqXGQM=\n"
      + "-----END ED25519 CERT-----\n"
      + "hidden-service-dir\n"
      + "contact dimanne dimanne@ya.ru\n"
      + "ntor-onion-key DpZv/Br0X2JgxtohD5Kr7A7NgAK4HjBgTbTB+uVSolE=\n";

  private static final String POLICY1
      = "reject *:25\nipv6-policy accept 23,42\n";

  private static final String POLICY2 = "reject *:*\n";

  private static final String RELAY3
      = "router-sig-ed25519 8pfPgYjlpwDoyESOZJHQwMwpmoyWCFg9dcswb8RTra4FT5jgol"
      + "HTgkX51h/yUXBx7jUibs2EVaRTOPm9TuiVDA\n"
      + "router-signature\n"
      + "-----BEGIN SIGNATURE-----\n"
      + "el2XtigqzbiSbUW11POx+l4kThP7c12JMyLlMPJXDncEYEa4F0+M0dQQF0BOeEIT\n"
      + "D0lj1h4iWT4R+uO5/7umXyAhZidfbhLoQsWa/dGs5BfO4ROgvVLc8o4Za6PYcPE9\n"
      + "DXd4yh+SZ86zaAWLUbr1VhRvSLWbFJwNn/aAQdAu70M=\n"
      + "-----END SIGNATURE-----\n";

}
