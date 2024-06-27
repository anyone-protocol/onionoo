package org.torproject.metrics.onionoo.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.torproject.metrics.onionoo.docs.Cert;
import org.torproject.metrics.onionoo.docs.PubKey;
import org.torproject.metrics.onionoo.docs.SerNum;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HardwareInfoUpdateRequest {

    private String id;
    private String company;
    private String format;
    private String wallet;
    private String fingerprint;
    private String nftid;
    private String build;
    private String flags;
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

    public String getNftid() {
        return nftid;
    }

    public void setNftid(String nftid) {
        this.nftid = nftid;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
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
