# Changes in version 8.0-1.xx.x - 2021-xx-xx

# Changes in version 8.0-1.26.0 - 2021-06-15

 * Medium changes
   - Use IPFire geolocation database instead of MaxMind.
   - Fix inconsistencies between ASN/GeoIP/rDNS lookups in
     summary and details documents.
   - Decode percent-encoded characters in the search parameter in the
     same way as in the other parameters.
   - Stop decoding a + sign to a space character in any of the
     parameters.

 * Minor changes
   - Simplify logging configuration.
   - Set default locale `US` and default time zone `UTC` at the
     beginning of the execution.
   - Update to metrics-lib 2.17.0.


# Changes in version 8.0-1.25.0 - 2020-02-20

 * Major changes
   - Include graph history objects even if the time period they cover
     are already contained in other graph history objects with shorter
     time periods and higher data resolutions.
   - Remove "3 days" and "1 week" bandwidth graphs, change "1 month"
     bandwidth graph to a data resolution of 24 hours, add back "1
     month" clients graph, and remove "1 week" uptime and weights
     graphs.


# Changes in version 7.1-1.24.1 - 2020-02-14

 * Minor changes
   - Update protocol version to 7.1.
   - Announce next major protocol version update for February 20,
     2020.


# Changes in version 7.0-1.24.0 - 2020-02-14

 * Medium changes
   - Process bridge pool assignments to include the BridgeDB
     distributor in bridge details documents.

 * Minor changes
   - Make Jetty host and port configurable.
   - Update to metrics-lib 2.10.0.


# Changes in version 7.0-1.23.0 - 2019-12-06

 * Medium changes
   - Only write status files and document files if their content has
     changed.


# Changes in version 7.0-1.22.0 - 2019-11-28

 * Medium changes
   - Remove declared/alleged family members after they disappear from
     server descriptors.


# Changes in version 7.0-1.21.1 - 2019-11-09

 * Minor changes
   - Set `Access-Control-Allow-Origin *` response header for bad
     requests, too.
   - Update to metrics-lib 2.9.1.


# Changes in version 7.0-1.21.0 - 2019-10-18

 * Medium changes
   - Add previously missing Jetty servlets dependency.
   - Fix a bug where old reverse DNS lookups were sometimes not
     cleared properly.


# Changes in version 7.0-1.20.0 - 2019-10-04

 * Medium changes
   - Use Ivy for resolving external dependencies rather than relying
     on files found in Debian stable packages. Requires installing Ivy
     (using `apt-get install ivy`, `brew install ivy`, or similar) and
     running `ant resolve` (or `ant -lib /usr/share/java resolve`).
     Retrieved files are then copied to the `lib/` directory, except
     for dependencies on other metrics libraries that still need to be
     copied to the `lib/` directory manually. Current dependency
     versions resolved by Ivy are the same as in Debian stretch with
     few exceptions.
   - Remove Cobertura from the build process.


# Changes in version 7.0-1.19.1 - 2018-11-20

 * Minor changes
   - Accept empty AS names in GeoLite2 ASN database files.


# Changes in version 7.0-1.19.0 - 2018-11-15

 * Medium changes
   - Update to the GeoLite2 ASN database format.

 * Minor changes
   - Rename root package org.torproject.onionoo to
     org.torproject.metrics.onionoo.
   - Rename all [Bb]oolean field getter methods to follow isXY()
     pattern.


# Changes in version 7.0-1.18.1 - 2018-09-11

 * Medium changes
   - Ignore unknown properties when parsing JSON files, which includes
     previously deprecated and later removed fields like "as_number"
     in details documents.


# Changes in version 7.0-1.18.0 - 2018-09-10

 * Medium changes
   - Extend "version" parameter to support lists and ranges.
   - Remove redundant "1_week" and "1_month" graphs from clients
     documents.
   - Change "3_months" graphs to "6_months" graphs in all documents
     containing history objects.
   - Remove the "fingerprint" parameter.
   - Remove the previously deprecated "as_number" field from details
     documents.


# Changes in version 6.2-1.17.1 - 2018-08-17

 * Minor changes
   - Parsing of the "as" parameter allows AS0 to be specified. It will
     now strip leading zeros. Specifying an AS number larger than the
     maximum possible with 32-bits will be treated as an error.


# Changes in version 6.2-1.17.0 - 2018-08-16

 * Medium changes
   - The "host_name" field will no longer appear in details documents.
   - Names in the "verified_host_names" and "unverified_host_names"
     fields are written in a deterministic order.
   - If a reverse domain name lookup results in either no names being
     found or an error then the lookup will be repeated at the next
     updater run.


# Changes in version 6.2-1.16.1 - 2018-08-13

 * Medium changes
   - Fix JSON serialization of history objects.


# Changes in version 6.2-1.16.0 - 2018-08-03

 * Medium changes
   - Support a comma-separated list of fingerprints in the lookup
     parameter to allow for URLs that specify a list of relays or
     bridges.
   - Add "as" field as a copy of the "as_number" field in preparation
     of removing the "as_number" field in the future.
   - Add new "as_name" parameter to search relays by AS name.
   - Support a comma-separated list of AS numbers in the "as"
     parameter.
   - Fix a thread-safety bug in the recently extended reverse DNS
     lookup code.

 * Minor changes
   - Extend internal statistics to log less frequently requested
     resources and parameter combinations without counts.
   - Provide a thin jar file without dependencies.


# Changes in version 6.1-1.15.0 - 2018-07-16

 * Medium changes
   - Provide more accurate DNS results in "verified_host_names" and
     "unverified_host_names".
   - Allow filtering by operating system using the new "os" parameter.

 * Minor changes
   - Index relays with no known country code or autonomous system
     number using the special values "xz" and "AS0" respectively.
   - Avoid running into an IllegalStateException when CollecTor is
     missing a whole descriptor directory.


# Changes in version 6.0-1.14.0 - 2018-05-29

 * Medium changes
   - Replace Gson with Jackson.


# Changes in version 6.0-1.13.0 - 2018-04-17

 * Medium changes
   - Change the "exit_addresses" field to not exclude current OR
     addresses anymore.

 * Minor changes
   - Turn valid utf-8 escape sequences into utf-8 characters.


# Changes in version 5.2-1.12.0 - 2018-04-06

 * Medium changes
   - Add version_status field to details documents.
   - Fetch descriptors from both CollecTor instances.

 * Minor changes
   - Don't attempt to un-escape character sequences in contact lines
     (like "\uk") that only happen to start like escaped utf-8
     characters (like "\u0055").


# Changes in version 5.1-1.11.0 - 2018-03-14

 * Medium changes
   - Stop omitting "n" in summary docs for "Unnamed" relays/bridges.
   - Always add a relay to its own "effective_family".

 * Minor changes
   - Make responses deterministic by always sorting results by
     fingerprint, either if no specific order was requested or to
     break ties after ordering results as requested.
   - Announce next major protocol version update on April 14, 2018.


# Changes in version 5.0-1.10.1 - 2018-02-07

 * Medium changes
   - Change 3 month weights graph to 24 hours detail.


# Changes in version 5.0-1.10.0 - 2018-02-07

 * Medium changes
   - Make writing of bandwidth, clients, uptime, and weights documents
     independent of system time.
   - Change 3 month bandwidth graph to 24 hours detail.


# Changes in version 5.0-1.9.0 - 2017-12-20

 * Medium changes
   - Remove the $ from fingerprints in fields "alleged_family",
     "effective_family", and "indirect_family".


# Changes in version 4.4-1.8.0 - 2017-11-28

 * Medium changes
   - Add a "version" field to relay details documents with the Tor
     software version listed in the consensus and similarly to bridge
     details documents with the Tor software version found in the
     server descriptor.
   - Extend the "version" parameter to also return bridges with the
     given version or version prefix.
   - Add a "recommended_version" field to bridge details documents
     based on whether the directory authorities recommend the bridge's
     version.
   - Add a "recommended_version" parameter to return only relays and
     bridges running a Tor software version that is recommended or not
     recommended by the directory authorities.


# Changes in version 4.3-1.7.1 - 2017-11-17

 * Minor changes
   - Include "unreachable_or_addresses" as accepted value in the
     "fields" parameter.


# Changes in version 4.3-1.7.0 - 2017-11-17

 * Medium changes
   - Support quoted qualified search terms.
   - Skip unrecognized descriptors when importing archives rather than
     aborting the entire import.
   - Add new "host_name" parameter to filter by host name.
   - Add new "unreachable_or_addresses" field with declared but
     unreachable OR addresses.


# Changes in version 4.2-1.6.1 - 2017-10-26

 * Medium changes
   - Fix two NullPointerExceptions caused by accessing optional parts
     of relay server descriptors and consensuses without checking
     first whether they're available or not.


# Changes in version 4.2-1.6.0 - 2017-10-09

 * Medium changes
   - Only set the "running" field in a bridge's details document to
     true if the bridge is both contained in the last known bridge
     network status and has the "Running" flag assigned there.
   - Add build_revision to documents, if available.
   - Update to metrics-lib 2.1.1.

 * Minor changes
   - Remove placeholder page on index.html.


# Changes in version 4.1-1.5.0 - 2017-09-15

 * Major changes
   - Update to metrics-lib 2.1.0 and to Java 8.


# Changes in version 4.1-1.4.1 - 2017-08-31

 * Medium changes
   - Fix a NullPointerException in the recently added "version"
     parameter.


# Changes in version 4.1-1.4.0 - 2017-08-30

 * Medium changes
   - Reset IPv6 exit-policy summary in details status if a newer
     server descriptor doesn't contain such a summary anymore.
   - Remove optional fields "countries", "transports", and "versions"
     from clients objects which were still labeled as beta.
   - Add new "version" parameter to filter for Tor version.

 * Minor changes
   - Switch from our own CollecTor downloader to metrics-lib's
     DescriptorCollector.
   - Add a new Java property "onionoo.basedir" to re-configure the
     base directory used by the web server component.


# Changes in version 4.0-1.3.0 - 2017-08-04

 * Medium changes
   - Add a parse history for imported descriptor archives.
   - Upgrade to Jetty9 and other Debian stretch dependencies.


# Changes in version 4.0-1.2.0 - 2017-02-28

 * Medium changes
   - Accept searches by IPv6 addresses even without leading or
     enclosing square brackets.


# Changes in version 3.2-1.1.0 - 2017-01-27

 * Major changes
   - Fix a bug where we'd believe that we have first seen a bridge on
     January 1, 1970 when in fact we have never seen it in a bridge
     network status and only learned about it from its self-published
     bridge server descriptor.

 * Medium changes
   - Unify the build process by adding git-submodule metrics-base in
     src/build and removing all centralized parts of the build
     process.
   - Accept the same characters in qualified search terms as in their
     parameter equivalents.
   - Exclude bandwidth history values from the future.
   - Extend order parameter to "first_seen".
   - Add response meta data fields "relays_skipped",
     "relays_truncated", "bridges_skipped", and "bridges_truncated".

 * Minor changes
   - Include XZ binaries in release binaries.


# Changes in version 3.1-1.0.0 - 2016-11-23

 * Major changes
   - This is the initial release after over five years of development.

