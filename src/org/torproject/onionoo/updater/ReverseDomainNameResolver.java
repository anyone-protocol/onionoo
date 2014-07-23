/* Copyright 2013 The Tor Project
 * See LICENSE for licensing information */
package org.torproject.onionoo.updater;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.torproject.onionoo.util.ApplicationFactory;
import org.torproject.onionoo.util.DateTimeHelper;
import org.torproject.onionoo.util.Logger;
import org.torproject.onionoo.util.Time;

public class ReverseDomainNameResolver {

  private class RdnsLookupWorker extends Thread {
    public void run() {
      while (time.currentTimeMillis() - RDNS_LOOKUP_MAX_DURATION_MILLIS
          <= startedRdnsLookups) {
        String rdnsLookupJob = null;
        synchronized (rdnsLookupJobs) {
          for (String job : rdnsLookupJobs) {
            rdnsLookupJob = job;
            rdnsLookupJobs.remove(job);
            break;
          }
        }
        if (rdnsLookupJob == null) {
          break;
        }
        RdnsLookupRequest request = new RdnsLookupRequest(this,
            rdnsLookupJob);
        request.setDaemon(true);
        request.start();
        try {
          Thread.sleep(RDNS_LOOKUP_MAX_REQUEST_MILLIS);
        } catch (InterruptedException e) {
          /* Getting interrupted should be the default case. */
        }
        String hostName = request.getHostName();
        if (hostName != null) {
          synchronized (rdnsLookupResults) {
            rdnsLookupResults.put(rdnsLookupJob, hostName);
          }
        }
        long lookupMillis = request.getLookupMillis();
        if (lookupMillis >= 0L) {
          synchronized (rdnsLookupMillis) {
            rdnsLookupMillis.add(lookupMillis);
          }
        }
      }
    }
  }

  private class RdnsLookupRequest extends Thread {
    private RdnsLookupWorker parent;
    private String address, hostName;
    private long lookupStartedMillis = -1L, lookupCompletedMillis = -1L;
    public RdnsLookupRequest(RdnsLookupWorker parent, String address) {
      this.parent = parent;
      this.address = address;
    }
    public void run() {
      this.lookupStartedMillis = time.currentTimeMillis();
      try {
        String result = InetAddress.getByName(this.address).getHostName();
        synchronized (this) {
          this.hostName = result;
        }
      } catch (UnknownHostException e) {
        /* We'll try again the next time. */
      }
      this.lookupCompletedMillis = time.currentTimeMillis();
      this.parent.interrupt();
    }
    public synchronized String getHostName() {
      return hostName;
    }
    public synchronized long getLookupMillis() {
      return this.lookupCompletedMillis - this.lookupStartedMillis;
    }
  }

  private Time time;

  public ReverseDomainNameResolver() {
    this.time = ApplicationFactory.getTime();
  }

  private static final long RDNS_LOOKUP_MAX_REQUEST_MILLIS =
      DateTimeHelper.TEN_SECONDS;
  private static final long RDNS_LOOKUP_MAX_DURATION_MILLIS =
      DateTimeHelper.FIVE_MINUTES;
  private static final long RDNS_LOOKUP_MAX_AGE_MILLIS =
      DateTimeHelper.TWELVE_HOURS;
  private static final int RDNS_LOOKUP_WORKERS_NUM = 5;

  private Map<String, Long> addressLastLookupTimes;

  private Set<String> rdnsLookupJobs;

  private Map<String, String> rdnsLookupResults;

  private List<Long> rdnsLookupMillis;

  private long startedRdnsLookups;

  private List<RdnsLookupWorker> rdnsLookupWorkers;

  public void setAddresses(Map<String, Long> addressLastLookupTimes) {
    this.addressLastLookupTimes = addressLastLookupTimes;
  }

  public void startReverseDomainNameLookups() {
    this.startedRdnsLookups = this.time.currentTimeMillis();
    this.rdnsLookupJobs = new HashSet<String>();
    for (Map.Entry<String, Long> e :
        this.addressLastLookupTimes.entrySet()) {
      if (e.getValue() < this.startedRdnsLookups
          - RDNS_LOOKUP_MAX_AGE_MILLIS) {
        this.rdnsLookupJobs.add(e.getKey());
      }
    }
    this.rdnsLookupResults = new HashMap<String, String>();
    this.rdnsLookupMillis = new ArrayList<Long>();
    this.rdnsLookupWorkers = new ArrayList<RdnsLookupWorker>();
    for (int i = 0; i < RDNS_LOOKUP_WORKERS_NUM; i++) {
      RdnsLookupWorker rdnsLookupWorker = new RdnsLookupWorker();
      this.rdnsLookupWorkers.add(rdnsLookupWorker);
      rdnsLookupWorker.setDaemon(true);
      rdnsLookupWorker.start();
    }
  }

  public void finishReverseDomainNameLookups() {
    for (RdnsLookupWorker rdnsLookupWorker : this.rdnsLookupWorkers) {
      try {
        rdnsLookupWorker.join();
      } catch (InterruptedException e) {
        /* This is not something that we can take care of.  Just leave the
         * worker thread alone. */
      }
    }
  }

  public Map<String, String> getLookupResults() {
    synchronized (this.rdnsLookupResults) {
      return new HashMap<String, String>(this.rdnsLookupResults);
    }
  }

  public long getLookupStartMillis() {
    return this.startedRdnsLookups;
  }

  public String getStatsString() {
    StringBuilder sb = new StringBuilder();
    sb.append("    " + Logger.formatDecimalNumber(rdnsLookupMillis.size())
        + " lookups performed\n");
    if (rdnsLookupMillis.size() > 0) {
      Collections.sort(rdnsLookupMillis);
      sb.append("    " + Logger.formatMillis(rdnsLookupMillis.get(0))
          + " minimum lookup time\n");
      sb.append("    " + Logger.formatMillis(rdnsLookupMillis.get(
          rdnsLookupMillis.size() / 2)) + " median lookup time\n");
      sb.append("    " + Logger.formatMillis(rdnsLookupMillis.get(
          rdnsLookupMillis.size() - 1)) + " maximum lookup time\n");
    }
    return sb.toString();
  }
}
