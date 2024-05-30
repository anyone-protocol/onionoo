package org.torproject.metrics.onionoo.docs;

import java.util.List;

public class HardwareInfoDocument extends Document {

    private String id;
    private String company;
    private String format;
    private String wallet;
    private String fingerprint;
    private List<SerNum> serNums;
    private List<PubKey> pubKeys;
    private List<Cert> certs;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getWallet() {
        return wallet;
    }

    public void setWallet(String wallet) {
        this.wallet = wallet;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public List<SerNum> getSerNums() {
        return serNums;
    }

    public void setSerNums(List<SerNum> serNums) {
        this.serNums = serNums;
    }

    public List<PubKey> getPubKeys() {
        return pubKeys;
    }

    public void setPubKeys(List<PubKey> pubKeys) {
        this.pubKeys = pubKeys;
    }

    public List<Cert> getCerts() {
        return certs;
    }

    public void setCerts(List<Cert> certs) {
        this.certs = certs;
    }
}
