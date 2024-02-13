import json
import re
import requests
import time
import os
from datetime import datetime, timedelta

from prometheus_client import CollectorRegistry, Gauge, write_to_textfile

def __check_time_delta(node, field, interval):
    if node.get(field):
        timestamp = 0
        if type(node.get(field)) is int:
            timestamp = datetime.fromtimestamp(node.get(field)/1000)
        else:
            timestamp = datetime.fromtimestamp(node.get(field).get('timestamp')/1000)


        if (datetime.utcnow() - timestamp) < timedelta(hours=interval):
            return True
        else:
            return False

if __name__ == '__main__':

    onionoo_host = os.getenv('ONIONOO_HOST')

    details = json.loads(requests.get(f'{onionoo_host}/details').text)
    bandwidth = json.loads(requests.get(f'{onionoo_host}/bandwidth').text)

    timestamp = time.time_ns()

    total_relays = []
    online_relays = []
    offline_relays = []

    total_bridges = []
    online_bridges = []
    offline_bridges = []

    total_overload_general_relays = []
    total_overload_general_relays_version = {}
    online_overload_general_relays = []
    offline_overload_general_relays = []

    total_overload_general_bridges = []
    total_overload_general_bridges_version = {}
    online_overload_general_bridges = []
    offline_overload_general_bridges = []

    total_overload_ratelimits_relays = []

    total_overload_ratelimits_bridges = []

    total_overload_fd_exhausted_relays = []

    total_overload_fd_exhausted_bridges = []

    total_eol_relays = []
    online_eol_relays = []
    offline_eol_relays = []

    total_eol_bridges = []
    online_eol_bridges = []
    offline_eol_bridges = []

    total_tor_versions_relays = {}
    online_tor_versions_relays = {}
    offline_tor_versions_relays = {}

    total_tor_platforms_relays = {}
    online_tor_platforms_relays = {}
    offline_tor_platforms_relays = {}

    total_as_relays = {}
    online_as_relays = {}
    offline_as_relays = {}

    total_contact_string_relays = {}
    online_contact_string_relays = {}
    offline_contact_string_relays = {}

    total_nickname_prefix_relays = {}
    online_nickname_prefix_relays = {}
    offline_nickname_prefix_relays = {}

    total_tor_versions_bridges = {}
    online_tor_versions_bridges = {}
    offline_tor_versions_bridges = {}

    total_tor_platforms_bridges = {}
    online_tor_platforms_bridges = {}
    offline_tor_platforms_bridges = {}

    total_countries_relays = {}
    online_countries_relays = {}
    offline_countries_relays = {}

    total_flags_relays = {}
    online_flags_relays = {}
    offline_flags_relays = {}

    total_flags_bridges = {}
    online_flags_bridges = {}
    offline_flags_bridges = {}

    total_obfs4_bridges = []
    online_obfs4_bridges = []
    offline_obfs4_bridges = []

    total_moat_bridges = []
    online_moat_bridges = []
    offline_moat_bridges = []

    total_contact_string_bridges = {}
    online_contact_string_bridges = {}
    offline_contact_string_bridges = {}

    total_nickname_prefix_bridges = {}
    online_nickname_prefix_bridges = {}
    offline_nickname_prefix_bridges = {}

    total_observed_bandwidth = 0
    online_observed_bandwidth = 0
    offline_observed_bandwidth = 0

    total_bandwidth_rate = 0
    online_bandwidth_rate = 0
    offline_bandwidth_rate = 0

    total_relays = details['relays']

    for relay in total_relays:
        autonomous_system = relay.get('as')
        country = relay.get('country')
        version = "None"
        nickname = relay.get('nickname')
        contact = "None"

        if len(nickname) > 4:
            prefix = nickname[:5]
            if total_nickname_prefix_relays.get(prefix):
                total_nickname_prefix_relays[prefix] += 1
            else:
                total_nickname_prefix_relays[prefix] = 1
        else:
            if total_nickname_prefix_relays.get(nickname):
                total_nickname_prefix_relays[nickname] += 1
            else:
                total_nickname_prefix_relays[nickname] = 1

        if relay.get('contact'):
            contact = relay.get('contact')
        if total_contact_string_relays.get(contact):
            total_contact_string_relays[contact] += 1
        else:
            total_contact_string_relays[contact] = 1

        if relay.get('version'):
            version = "{}".format(re.sub('\W+', '_', relay.get('version')))
        flags = []
        if relay.get('flags'):
            flags = relay.get('flags')
        platform = "None"
        if relay.get('platform'):
            sanitized_platform = re.sub('\W+', '_', relay.get('platform').lower())
            platform = "{}".format(re.sub('tor_' + version + '_' + 'on_', '', sanitized_platform))

        if relay.get('overload_general_timestamp'):
            if __check_time_delta(relay, 'overload_general_timestamp', 19):
                total_overload_general_relays.append(relay)
                if relay.get('version'):
                    version = "{}".format(re.sub('\W+', '_', relay.get('version')))

                    if total_overload_general_relays_version.get(version):
                        total_overload_general_relays_version[version] += 1
                    else:
                        total_overload_general_relays_version[version] = 1


        if relay.get('recommended_version') == False:
            total_eol_relays.append(relay)

        if total_tor_versions_relays.get(version):
            total_tor_versions_relays[version] += 1
        else:
            total_tor_versions_relays[version] = 1

        if total_tor_platforms_relays.get(platform):
            total_tor_platforms_relays[platform] += 1
        else:
            total_tor_platforms_relays[platform] = 1

        if total_as_relays.get(autonomous_system):
            total_as_relays[autonomous_system] += 1
        else:
            total_as_relays[autonomous_system] = 1

        if total_countries_relays.get(country):
            total_countries_relays[country] += 1
        else:
            total_countries_relays[country] = 1

        for flag in flags:
            if total_flags_relays.get(flag):
                total_flags_relays[flag] += 1
            else:
                total_flags_relays[flag] = 1

        observed_bandwidth = relay.get('observed_bandwidth')
        total_observed_bandwidth += observed_bandwidth

        bandwidth_rate = relay.get('bandwidth_rate')
        total_bandwidth_rate += bandwidth_rate

        if relay.get('running') == True:
            online_relays.append(relay)
            contact = "None"

            if len(nickname) > 4:
                prefix = nickname[:5]
                if online_nickname_prefix_relays.get(prefix):
                    online_nickname_prefix_relays[prefix] += 1
                else:
                    online_nickname_prefix_relays[prefix] = 1
            else:
                if online_nickname_prefix_relays.get(nickname):
                    online_nickname_prefix_relays[nickname] += 1
                else:
                    online_nickname_prefix_relays[nickname] = 1

            if relay.get('contact'):
                contact = relay.get('contact')
            if online_contact_string_relays.get(contact):
                online_contact_string_relays[contact] += 1
            else:
                online_contact_string_relays[contact] = 1

            if relay.get('overload_general_timestamp'):
                if __check_time_delta(relay, 'overload_general_timestamp', 19):
                    online_overload_general_relays.append(relay)

            if relay.get('recommended_version') == False:
                online_eol_relays.append(relay)

            if online_tor_versions_relays.get(version):
                online_tor_versions_relays[version] += 1
            else:
                online_tor_versions_relays[version] = 1

            if online_tor_platforms_relays.get(platform):
                online_tor_platforms_relays[platform] += 1
            else:
                online_tor_platforms_relays[platform] = 1

            if online_as_relays.get(autonomous_system):
                online_as_relays[autonomous_system] += 1
            else:
                online_as_relays[autonomous_system] = 1

            if online_countries_relays.get(country):
                online_countries_relays[country] += 1
            else:
                online_countries_relays[country] = 1

            for flag in flags:
                if online_flags_relays.get(flag):
                    online_flags_relays[flag] += 1
                else:
                    online_flags_relays[flag] = 1

            online_observed_bandwidth += observed_bandwidth

            online_bandwidth_rate += bandwidth_rate

        else:
            offline_relays.append(relay)
            contact = "None"

            if len(nickname) > 4:
                prefix = nickname[:5]
                if offline_nickname_prefix_relays.get(prefix):
                    offline_nickname_prefix_relays[prefix] += 1
                else:
                    offline_nickname_prefix_relays[prefix] = 1
            else:
                if offline_nickname_prefix_relays.get(nickname):
                    offline_nickname_prefix_relays[nickname] += 1
                else:
                    offline_nickname_prefix_relays[nickname] = 1

            if relay.get('contact'):
                contact = relay.get('contact')
            if offline_contact_string_relays.get(contact):
                offline_contact_string_relays[contact] += 1
            else:
                offline_contact_string_relays[contact] = 1

            if relay.get('overload_general_timestamp'):
                if __check_time_delta(relay, 'overload_general_timestamp', 19):
                    offline_overload_general_relays.append(relay)

            if relay.get('recommended_version') == False:
                offline_eol_relays.append(relay)

            if offline_tor_versions_relays.get(version):
                offline_tor_versions_relays[version] += 1
            else:
                offline_tor_versions_relays[version] = 1

            if offline_tor_platforms_relays.get(platform):
                offline_tor_platforms_relays[platform] += 1
            else:
                offline_tor_platforms_relays[platform] = 1

            if offline_as_relays.get(autonomous_system):
                offline_as_relays[autonomous_system] += 1
            else:
                offline_as_relays[autonomous_system] = 1

            if offline_countries_relays.get(country):
                offline_countries_relays[country] += 1
            else:
                offline_countries_relays[country] = 1

            for flag in flags:
                if offline_flags_relays.get(flag):
                    offline_flags_relays[flag] += 1
                else:
                    offline_flags_relays[flag] = 1

            offline_observed_bandwidth += observed_bandwidth

            offline_bandwidth_rate += bandwidth_rate

    total_bridges = details['bridges']

    for bridge in total_bridges:
        version = "None"
        nickname = relay.get('nickname')
        contact = "None"

        if len(nickname) > 4:
            prefix = nickname[:5]
            if total_nickname_prefix_bridges.get(prefix):
                total_nickname_prefix_bridges[prefix] += 1
            else:
                total_nickname_prefix_bridges[prefix] = 1
        else:
            if total_nickname_prefix_bridges.get(nickname):
                total_nickname_prefix_bridges[nickname] += 1
            else:
                total_nickname_prefix_bridges[nickname] = 1

        if relay.get('contact'):
            contact = relay.get('contact')
        if total_contact_string_bridges.get(contact):
            total_contact_string_bridges[contact] += 1
        else:
            total_contact_string_bridges[contact] = 1

        if bridge.get('version'):
            version = "{}".format(re.sub('\W+', '_', bridge.get('version')))
        flags = []
        if bridge.get('flags'):
            flags = bridge.get('flags')
        platform = "None"
        if bridge.get('platform'):
            sanitized_platform = re.sub('\W+', '_', bridge.get('platform').lower())
            platform = "{}".format(re.sub('tor_' + version + '_' + 'on_', '', sanitized_platform))
        transports = bridge.get('transports')
        distributor = bridge.get('bridgedb_distributor')

        if transports is not None and "obfs4" in transports:
            total_obfs4_bridges.append(bridge)

        if distributor == "moat":
            total_moat_bridges.append(bridge)

        if bridge.get('overload_general_timestamp'):
            if __check_time_delta(bridge, 'overload_general_timestamp', 19):
                total_overload_general_bridges.append(bridge)
                if bridge.get('version'):
                    version = "{}".format(re.sub('\W+', '_', bridge.get('version')))

                    if total_overload_general_bridges_version.get(version):
                        total_overload_general_bridges_version[version] += 1
                    else:
                        total_overload_general_bridges_version[version] = 1

        if bridge.get('recommended_version') == False:
            total_eol_bridges.append(bridge)

        if total_tor_versions_bridges.get(version):
            total_tor_versions_bridges[version] += 1
        else:
            total_tor_versions_bridges[version] = 1

        if total_tor_platforms_bridges.get(platform):
            total_tor_platforms_bridges[platform] += 1
        else:
            total_tor_platforms_bridges[platform] = 1

        if len(flags) > 0:
            for flag in flags:
                if total_flags_bridges.get(flag):
                    total_flags_bridges[flag] += 1
                else:
                    total_flags_bridges[flag] = 1

        if bridge.get('running') == True:
            online_bridges.append(bridge)

            if len(nickname) > 4:
                prefix = nickname[:5]
                if online_nickname_prefix_bridges.get(prefix):
                    online_nickname_prefix_bridges[prefix] += 1
                else:
                    online_nickname_prefix_bridges[prefix] = 1
            else:
                if online_nickname_prefix_bridges.get(nickname):
                    online_nickname_prefix_bridges[nickname] += 1
                else:
                    online_nickname_prefix_bridges[nickname] = 1

            if relay.get('contact'):
                contact = relay.get('contact')
            if online_contact_string_bridges.get(contact):
                online_contact_string_bridges[contact] += 1
            else:
                online_contact_string_bridges[contact] = 1

            if transports is not None and "obfs4" in transports:
                online_obfs4_bridges.append(bridge)

            if distributor == "moat":
                online_moat_bridges.append(bridge)

            if bridge.get('overload_general_timestamp'):
                if __check_time_delta(bridge, 'overload_general_timestamp', 19):
                    online_overload_general_bridges.append(bridge)

            if bridge.get('recommended_version') == False:
                online_eol_bridges.append(bridge)

            if online_tor_versions_bridges.get(version):
                online_tor_versions_bridges[version] += 1
            else:
                online_tor_versions_bridges[version] = 1

            if online_tor_platforms_bridges.get(platform):
                online_tor_platforms_bridges[platform] += 1
            else:
                online_tor_platforms_bridges[platform] = 1

            if len(flags) > 0:
                for flag in flags:
                    if online_flags_bridges.get(flag):
                        online_flags_bridges[flag] += 1
                    else:
                        online_flags_bridges[flag] = 1

        else:
            offline_bridges.append(bridge)

            if len(nickname) > 4:
                prefix = nickname[:5]
                if offline_nickname_prefix_bridges.get(prefix):
                    offline_nickname_prefix_bridges[prefix] += 1
                else:
                    offline_nickname_prefix_bridges[prefix] = 1
            else:
                if offline_nickname_prefix_bridges.get(nickname):
                    offline_nickname_prefix_bridges[nickname] += 1
                else:
                    offline_nickname_prefix_bridges[nickname] = 1

            if relay.get('contact'):
                contact = relay.get('contact')
            if offline_contact_string_bridges.get(contact):
                offline_contact_string_bridges[contact] += 1
            else:
                offline_contact_string_bridges[contact] = 1

            if transports is not None and "obfs4" in transports:
                offline_obfs4_bridges.append(bridge)

            if distributor == "moat":
                offline_moat_bridges.append(bridge)

            if bridge.get('overload_general_timestamp'):
                if __check_time_delta(bridge, 'overload_general_timestamp', 19):
                    offline_overload_general_bridges.append(bridge)

            if bridge.get('recommended_version') == False:
                offline_eol_bridges.append(bridge)

            if offline_tor_versions_bridges.get(version):
                offline_tor_versions_bridges[version] += 1
            else:
                offline_tor_versions_bridges[version] = 1

            if offline_tor_platforms_bridges.get(platform):
                offline_tor_platforms_bridges[platform] += 1
            else:
                offline_tor_platforms_bridges[platform] = 1

            if len(flags) > 0:
                for flag in flags:
                    if offline_flags_bridges.get(flag):
                        offline_flags_bridges[flag] += 1
                    else:
                        offline_flags_bridges[flag] = 1

    relays = bandwidth['relays']
    for relay in relays:
        if relay.get('overload_ratelimits'):
            if __check_time_delta(relay, 'overload_ratelimits', 24):
                total_overload_ratelimits_relays.append(relay)

        if relay.get('overload_fd_exhausted'):
            if __check_time_delta(relay, 'overload_fd_exhausted', 72):
                total_overload_fd_exhausted_relays.append(relay)

    bridges = bandwidth['bridges']
    for bridge in bridges:
        if bridge.get('overload_ratelimits'):
            if __check_time_delta(bridge, 'overload_ratelimits', 24):
                total_overload_ratelimits_bridges.append(bridge)

        if bridge.get('overload_fd_exhausted'):
            if __check_time_delta(bridge, 'overload_fd_exhausted', 72):
                total_overload_fd_exhausted_bridges.append(bridge)

    registry = CollectorRegistry()

    network_relays = Gauge('total_relays', 'Current number of relays on the network', ['status'], registry=registry)
    network_relays.labels(status='all').set(len(total_relays))
    network_relays.labels(status='online').set(len(online_relays))
    network_relays.labels(status='offline').set(len(offline_relays))

    network_bridges = Gauge('total_bridges', 'Current number of bridges on the network', ['status'], registry=registry)
    network_bridges.labels(status='all').set(len(total_bridges))
    network_bridges.labels(status='online').set(len(online_bridges))
    network_bridges.labels(status='offline').set(len(offline_bridges))

    network_transport_bridges = Gauge('total_transport_bridges', 'Current number of bridges per transport on the network', ['status', 'transport'], registry=registry)
    network_transport_bridges.labels(status='all', transport='obfs4').set(len(total_obfs4_bridges))
    network_transport_bridges.labels(status='all', transport='moat').set(len(total_moat_bridges))
    network_transport_bridges.labels(status='online', transport='obfs4').set(len(online_obfs4_bridges))
    network_transport_bridges.labels(status='online', transport='moat').set(len(online_moat_bridges))
    network_transport_bridges.labels(status='offline', transport='obfs4').set(len(offline_obfs4_bridges))
    network_transport_bridges.labels(status='offline', transport='moat').set(len(offline_moat_bridges))

    network_overload_general_relays = Gauge('total_overload_general_relays', 'Current number of relays in overload_general state on the network', ['status'], registry=registry)
    network_overload_general_relays.labels(status='all').set(len(total_overload_general_relays))
    network_overload_general_relays.labels(status='online').set(len(online_overload_general_relays))
    network_overload_general_relays.labels(status='offline').set(len(offline_overload_general_relays))

    network_overload_general_relays_version = Gauge('total_overload_general_relays_version', 'Current number of relays in overload_general state on the network per version', ['version'], registry=registry)
    for v in total_overload_general_relays_version.keys():
        network_overload_general_relays_version.labels(version=v).set(total_overload_general_relays_version[v])

    network_overload_general_bridges = Gauge('total_overload_general_bridges', 'Current number of bridges in overload_general state on the network', ['status'], registry=registry)
    network_overload_general_bridges.labels(status='all').set(len(total_overload_general_bridges))
    network_overload_general_bridges.labels(status='online').set(len(online_overload_general_bridges))
    network_overload_general_bridges.labels(status='offline').set(len(offline_overload_general_bridges))

    network_overload_general_bridges_version = Gauge('total_overload_general_bridges_version', 'Current number of bridges in overload_general state on the network per version', ['version'], registry=registry)
    for v in total_overload_general_bridges_version.keys():
        network_overload_general_bridges_version.labels(version=v).set(total_overload_general_bridges_version[v])

    total_network_overload_ratelimits_relays = Gauge('total_overload_ratelimits_relays', 'Current number of relays in overload_ratelimits state', registry=registry)
    total_network_overload_ratelimits_relays.set(len(total_overload_ratelimits_relays))

    total_network_overload_ratelimits_bridges = Gauge('total_overload_ratelimits_bridges', 'Current number of bridges in overload_ratelimits state', registry=registry)
    total_network_overload_ratelimits_bridges.set(len(total_overload_ratelimits_bridges))

    total_network_overload_fd_exhausted_relays = Gauge('total_overload_fd_exhausted_relays', 'Current number of relays in overload_fd_exhausted state', registry=registry)
    total_network_overload_fd_exhausted_relays.set(len(total_overload_fd_exhausted_relays))

    total_network_overload_fd_exhausted_bridges = Gauge('total_overload_fd_exhausted_bridges', 'Current number of bridges in overload_fd_exhausted state', registry=registry)
    total_network_overload_fd_exhausted_bridges.set(len(total_overload_fd_exhausted_bridges))

    network_eol_version_relays = Gauge('total_eol_version_relays', 'Current number of eol relays', ['status'], registry=registry)
    network_eol_version_relays.labels(status='all').set(len(total_eol_relays))
    network_eol_version_relays.labels(status='online').set(len(online_eol_relays))
    network_eol_version_relays.labels(status='offline').set(len(offline_eol_relays))

    network_eol_version_bridges = Gauge('total_eol_version_bridges', 'Current number of eol bridges', ['status'], registry=registry)
    network_eol_version_bridges.labels(status='all').set(len(total_eol_bridges))
    network_eol_version_bridges.labels(status='online').set(len(online_eol_bridges))
    network_eol_version_bridges.labels(status='offline').set(len(offline_eol_bridges))

    network_countries_relays = Gauge("total_network_relays_country", "Current number of total relays per country", ['status', 'country'], registry=registry)
    for country in total_countries_relays.keys():
        if country is not None and country.isalpha():
            network_countries_relays.labels(status='all', country=country).set(total_countries_relays[country])
    for country in online_countries_relays.keys():
        if country is not None and country.isalpha():
            network_countries_relays.labels(status='online', country=country).set(online_countries_relays[country])
    for country in offline_countries_relays.keys():
        if country is not None and country.isalpha():
            network_countries_relays.labels(status='offline', country=country).set(offline_countries_relays[country])

    network_flags_relays = Gauge("total_network_relays_flag", "Current number of relays per flag", ['status', 'flag'], registry=registry)
    for flag in total_flags_relays.keys():
        if flag is not None and flag.isalpha():
            network_flags_relays.labels(status='all', flag=flag).set(total_flags_relays[flag])
    for flag in online_flags_relays.keys():
        if flag is not None and flag.isalpha():
            network_flags_relays.labels(status='online', flag=flag).set(online_flags_relays[flag])
    for flag in offline_flags_relays.keys():
        if flag is not None and flag.isalpha():
            network_flags_relays.labels(status='offline', flag=flag).set(offline_flags_relays[flag])

    network_flags_bridges = Gauge("total_network_bridges_flag", "Current number of bridges per flag", ['status', 'flag'], registry=registry)
    for flag in total_flags_bridges.keys():
        if flag is not None and flag.isalpha():
            network_flags_bridges.labels(status='all', flag=flag).set(total_flags_bridges[flag])
    for flag in online_flags_bridges.keys():
        if flag is not None and flag.isalpha():
            network_flags_bridges.labels(status='online', flag=flag).set(online_flags_bridges[flag])
    for flag in offline_flags_bridges.keys():
        if flag is not None and flag.isalpha():
            network_flags_bridges.labels(status='offline', flag=flag).set(offline_flags_bridges[flag])

    network_tor_versions_relays = Gauge("total_network_tor_version_relays", "Current number of total relays per tor version", ['status', 'version'], registry=registry)
    for version in total_tor_versions_relays.keys():
        network_tor_versions_relays.labels(status='all', version=version).set(total_tor_versions_relays[version])
    for version in online_tor_versions_relays.keys():
        network_tor_versions_relays.labels(status='online', version=version).set(online_tor_versions_relays[version])
    for version in offline_tor_versions_relays.keys():
        network_tor_versions_relays.labels(status='offline', version=version).set(offline_tor_versions_relays[version])

    network_tor_versions_bridges = Gauge("total_network_tor_version_bridges", "Current number of total bridges per tor version", ['status', 'version'], registry=registry)
    for version in total_tor_versions_bridges.keys():
        network_tor_versions_bridges.labels(status='all', version=version).set(total_tor_versions_bridges[version])
    for version in online_tor_versions_bridges.keys():
        network_tor_versions_bridges.labels(status='online', version=version).set(online_tor_versions_bridges[version])
    for version in offline_tor_versions_bridges.keys():
        network_tor_versions_bridges.labels(status='offline', version=version).set(offline_tor_versions_bridges[version])

    network_tor_platforms_relays = Gauge("total_network_tor_platform_relays", "Current number of total relays per tor platform", ['status', 'platform'], registry=registry)
    for platform in total_tor_platforms_relays.keys():
        network_tor_platforms_relays.labels(status='all', platform=platform).set(total_tor_platforms_relays[platform])
    for platform in online_tor_platforms_relays.keys():
        network_tor_platforms_relays.labels(status='online', platform=platform).set(online_tor_platforms_relays[platform])
    for platform in offline_tor_platforms_relays.keys():
        network_tor_platforms_relays.labels(status='offline', platform=platform).set(offline_tor_platforms_relays[platform])

    network_tor_platforms_bridges = Gauge("total_network_tor_platform_bridges", "Current number of total bridges per tor platform", ['status', 'platform'], registry=registry)
    for platform in total_tor_platforms_bridges.keys():
        network_tor_platforms_bridges.labels(status='all', platform=platform).set(total_tor_platforms_bridges[platform])
    for platform in online_tor_platforms_bridges.keys():
        network_tor_platforms_bridges.labels(status='online', platform=platform).set(online_tor_platforms_bridges[platform])
    for platform in offline_tor_platforms_bridges.keys():
        network_tor_platforms_bridges.labels(status='offline', platform=platform).set(offline_tor_platforms_bridges[platform])

    network_tor_as_relays = Gauge("total_network_tor_as_relays", "Current number of relays per AS (Autonomous System)", ['status', 'autonomous_system'], registry=registry)
    for autonomous_system in total_as_relays.keys():
        network_tor_as_relays.labels(status='all', autonomous_system=autonomous_system).set(total_as_relays[autonomous_system])
    for autonomous_system in online_as_relays.keys():
        network_tor_as_relays.labels(status='online', autonomous_system=autonomous_system).set(online_as_relays[autonomous_system])
    for autonomous_system in offline_as_relays.keys():
        network_tor_as_relays.labels(status='offline', autonomous_system=autonomous_system).set(offline_as_relays[autonomous_system])

    network_tor_nickname_prefix_relays = Gauge("total_network_tor_nickname_prefix_relays", "Current number of relays sharing the same nickname prefix", ['status', 'nickname_prefix'], registry=registry)
    for nickname_prefix in total_nickname_prefix_relays.keys():
        network_tor_nickname_prefix_relays.labels(status='all', nickname_prefix=nickname_prefix).set(total_nickname_prefix_relays[nickname_prefix])
    for nickname_prefix in online_nickname_prefix_relays.keys():
        network_tor_nickname_prefix_relays.labels(status='online', nickname_prefix=nickname_prefix).set(online_nickname_prefix_relays[nickname_prefix])
    for nickname_prefix in offline_nickname_prefix_relays.keys():
        network_tor_nickname_prefix_relays.labels(status='offline', nickname_prefix=nickname_prefix).set(offline_nickname_prefix_relays[nickname_prefix])

    network_tor_nickname_prefix_bridges = Gauge("total_network_tor_nickname_prefix_bridges", "Current number of bridges sharing the same nickname prefix", ['status', 'nickname_prefix'], registry=registry)
    for nickname_prefix in total_nickname_prefix_bridges.keys():
        network_tor_nickname_prefix_bridges.labels(status='all', nickname_prefix=nickname_prefix).set(total_nickname_prefix_bridges[nickname_prefix])
    for nickname_prefix in online_nickname_prefix_bridges.keys():
        network_tor_nickname_prefix_bridges.labels(status='online', nickname_prefix=nickname_prefix).set(online_nickname_prefix_bridges[nickname_prefix])
    for nickname_prefix in offline_nickname_prefix_bridges.keys():
        network_tor_nickname_prefix_bridges.labels(status='offline', nickname_prefix=nickname_prefix).set(offline_nickname_prefix_bridges[nickname_prefix])

    network_tor_contact_string_relays = Gauge("total_network_tor_contact_string_relays", "Current number of relays sharing the same contact string", ['status', 'contact_string'], registry=registry)
    for contact_string in total_contact_string_relays.keys():
        network_tor_contact_string_relays.labels(status='all', contact_string=contact_string).set(total_contact_string_relays[contact_string])
    for contact_string in online_contact_string_relays.keys():
        network_tor_contact_string_relays.labels(status='online', contact_string=contact_string).set(online_contact_string_relays[contact_string])
    for contact_string in offline_contact_string_relays.keys():
        network_tor_contact_string_relays.labels(status='offline', contact_string=contact_string).set(offline_contact_string_relays[contact_string])

    network_tor_contact_string_bridges = Gauge("total_network_tor_contact_string_bridges", "Current number of bridges sharing the same contact string", ['status', 'contact_string'], registry=registry)
    for contact_string in total_contact_string_bridges.keys():
        network_tor_contact_string_bridges.labels(status='all', contact_string=contact_string).set(total_contact_string_bridges[contact_string])
    for contact_string in online_contact_string_bridges.keys():
        network_tor_contact_string_bridges.labels(status='online', contact_string=contact_string).set(online_contact_string_bridges[contact_string])
    for contact_string in offline_contact_string_bridges.keys():
        network_tor_contact_string_bridges.labels(status='offline', contact_string=contact_string).set(offline_contact_string_bridges[contact_string])

    network_observed_bandwidth = Gauge('total_observed_bandwidth', 'Current total observed bandwidth on the network', ['status'], registry=registry)
    network_observed_bandwidth.labels(status='all').set(total_observed_bandwidth)
    network_observed_bandwidth.labels(status='online').set(online_observed_bandwidth)
    network_observed_bandwidth.labels(status='offline').set(offline_observed_bandwidth)

    network_bandwidth_rate = Gauge('average_bandwidth_rate', 'Current average bandwidth rate on the network', ['status'], registry=registry)
    average_bandwidth_rate = total_relays == 0 and 0 or total_bandwidth_rate / len(total_relays)
    network_bandwidth_rate.labels(status='all').set(average_bandwidth_rate)
    average_bandwidth_rate_online = online_relays == 0 and 0 or online_bandwidth_rate / len(online_relays)
    network_bandwidth_rate.labels(status='online').set(average_bandwidth_rate_online)
    average_bandwidth_rate_offline = offline_relays == 0 and 0 or offline_bandwidth_rate / len(offline_relays)
    network_bandwidth_rate.labels(status='offline').set(average_bandwidth_rate_offline)

    file_path = os.getenv('METRICS_FILE_PATH', '/srv/onionoo/data/out/network/metrics')
    write_to_textfile(file_path, registry)

    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{current_time}] - Ok!")
