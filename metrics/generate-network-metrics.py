import json
import re
import requests
import time
import os
import pandas as pd
from datetime import datetime, timedelta

from prometheus_client import CollectorRegistry, Gauge, write_to_textfile

if __name__ == '__main__':

    onionoo_host = os.getenv('ONIONOO_HOST')
    interval_minutes = int(os.getenv('INTERVAL_MINUTES'))

    details = json.loads(requests.get(f'{onionoo_host}/details').text)
    bandwidth = json.loads(requests.get(f'{onionoo_host}/bandwidth').text)

    time_string = details["relays_published"]
    time_object = datetime.strptime(time_string, '%Y-%m-%d %H:%M:%S')
    fresh_until = time_object + timedelta(minutes=(interval_minutes*1.5))
    valid_until = time_object + timedelta(minutes=(interval_minutes*3))

    consensus_is_fresh = 0
    if datetime.now() < fresh_until:
        consensus_is_fresh = 1

    consensus_is_valid = 0
    if datetime.now() < valid_until:
        consensus_is_valid = 1

    total_relays = []
    online_relays = []
    offline_relays = []

    total_eol_relays = []
    online_eol_relays = []
    offline_eol_relays = []

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

    total_countries_relays = {}
    online_countries_relays = {}
    offline_countries_relays = {}

    total_countries_bandwidth = {}
    online_countries_bandwidth = {}
    offline_countries_bandwidth = {}

    bandwidth_per_flag = {}
    online_bandwidth_per_flag = {}
    offline_bandwidth_per_flag = {}

    total_flags_relays = {}
    online_flags_relays = {}
    offline_flags_relays = {}

    total_observed_bandwidth = 0
    online_observed_bandwidth = 0
    offline_observed_bandwidth = 0

    total_bandwidth_rate = 0
    online_bandwidth_rate = 0
    offline_bandwidth_rate = 0

    total_online_measured = 0
    total_online_unmeasured = 0

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

        for flag in flags:
            if bandwidth_per_flag.get(flag):
                bandwidth_per_flag[flag].append(observed_bandwidth)
            else:
                bandwidth_per_flag[flag] = [observed_bandwidth]

        if total_countries_bandwidth.get(country):
            total_countries_bandwidth[country] += observed_bandwidth
        else:
            total_countries_bandwidth[country] = observed_bandwidth

        bandwidth_rate = relay.get('bandwidth_rate')
        total_bandwidth_rate += bandwidth_rate

        is_measured = relay.get('measured')

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

            for flag in flags:
                if online_bandwidth_per_flag.get(flag):
                    online_bandwidth_per_flag[flag].append(observed_bandwidth)
                else:
                    online_bandwidth_per_flag[flag] = [observed_bandwidth]

            if online_countries_bandwidth.get(country):
                online_countries_bandwidth[country] += observed_bandwidth
            else:
                online_countries_bandwidth[country] = observed_bandwidth

            online_bandwidth_rate += bandwidth_rate

            if is_measured:
                total_online_measured += 1
            else:
                total_online_unmeasured += 1

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

            for flag in flags:
                if offline_bandwidth_per_flag.get(flag):
                    offline_bandwidth_per_flag[flag].append(observed_bandwidth)
                else:
                    offline_bandwidth_per_flag[flag] = [observed_bandwidth]

            if offline_countries_bandwidth.get(country):
                offline_countries_bandwidth[country] += observed_bandwidth
            else:
                offline_countries_bandwidth[country] = observed_bandwidth

            offline_bandwidth_rate += bandwidth_rate

    registry = CollectorRegistry()

    network_relays = Gauge('total_relays', 'Current number of relays on the network', ['status'], registry=registry)
    network_relays.labels(status='all').set(len(total_relays))
    network_relays.labels(status='online').set(len(online_relays))
    network_relays.labels(status='offline').set(len(offline_relays))

    network_eol_version_relays = Gauge('total_eol_version_relays', 'Current number of eol relays', ['status'], registry=registry)
    network_eol_version_relays.labels(status='all').set(len(total_eol_relays))
    network_eol_version_relays.labels(status='online').set(len(online_eol_relays))
    network_eol_version_relays.labels(status='offline').set(len(offline_eol_relays))

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

    network_tor_versions_relays = Gauge("total_network_tor_version_relays", "Current number of total relays per tor version", ['status', 'version'], registry=registry)
    for version in total_tor_versions_relays.keys():
        network_tor_versions_relays.labels(status='all', version=version).set(total_tor_versions_relays[version])
    for version in online_tor_versions_relays.keys():
        network_tor_versions_relays.labels(status='online', version=version).set(online_tor_versions_relays[version])
    for version in offline_tor_versions_relays.keys():
        network_tor_versions_relays.labels(status='offline', version=version).set(offline_tor_versions_relays[version])

    network_tor_platforms_relays = Gauge("total_network_tor_platform_relays", "Current number of total relays per tor platform", ['status', 'platform'], registry=registry)
    for platform in total_tor_platforms_relays.keys():
        network_tor_platforms_relays.labels(status='all', platform=platform).set(total_tor_platforms_relays[platform])
    for platform in online_tor_platforms_relays.keys():
        network_tor_platforms_relays.labels(status='online', platform=platform).set(online_tor_platforms_relays[platform])
    for platform in offline_tor_platforms_relays.keys():
        network_tor_platforms_relays.labels(status='offline', platform=platform).set(offline_tor_platforms_relays[platform])

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

    network_tor_contact_string_relays = Gauge("total_network_tor_contact_string_relays", "Current number of relays sharing the same contact string", ['status', 'contact_string'], registry=registry)
    for contact_string in total_contact_string_relays.keys():
        network_tor_contact_string_relays.labels(status='all', contact_string=contact_string).set(total_contact_string_relays[contact_string])
    for contact_string in online_contact_string_relays.keys():
        network_tor_contact_string_relays.labels(status='online', contact_string=contact_string).set(online_contact_string_relays[contact_string])
    for contact_string in offline_contact_string_relays.keys():
        network_tor_contact_string_relays.labels(status='offline', contact_string=contact_string).set(offline_contact_string_relays[contact_string])

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

    online_measured_count = Gauge('total_online_measured_count', 'Current total online measured relays', registry=registry)
    online_measured_count.set(total_online_measured)

    online_unmeasured_count = Gauge('total_online_unmeasured_count', 'Current total online unmeasured relays', registry=registry)
    online_unmeasured_count.set(total_online_unmeasured)

    online_measured_ratio = Gauge('total_online_measured_ratio', 'Current total online measured ratio', registry=registry)
    online_measured_ratio.set(total_online_measured / total_online_unmeasured)

    online_measured_percentage = Gauge('total_online_measured_percentage', 'Current total online measured percentage', registry=registry)
    online_measured_percentage.set(total_online_measured / len(online_relays) * 100)

    countries_average_observed_bandwidth = Gauge('countries_average_observed_bandwidth', 'Average observed bandwidth per country', ['status','country'], registry=registry)
    for country in total_countries_bandwidth.keys():
        if country is not None and country.isalpha():
            average_countries_bandwidth = total_countries_bandwidth[country] / total_countries_relays[country]
            countries_average_observed_bandwidth.labels(status='all', country=country).set(average_countries_bandwidth)
    for country in online_countries_relays.keys():
        if country is not None and country.isalpha():
            average_online_countries_bandwidth = online_countries_bandwidth[country] / online_countries_relays[country]
            countries_average_observed_bandwidth.labels(status='online', country=country).set(average_online_countries_bandwidth)
    for country in offline_countries_relays.keys():
        if country is not None and country.isalpha():
            average_offline_countries_bandwidth = offline_countries_bandwidth[country] / offline_countries_relays[country]
            countries_average_observed_bandwidth.labels(status='offline', country=country).set(average_offline_countries_bandwidth)

    median_bandwidth_per_flag = Gauge('median_bandwidth_per_flag', 'Median bandwidth per flag', ['status', 'flag'], registry=registry)
    for flag in bandwidth_per_flag.keys():
        if flag is not None and flag.isalpha():
            bandwidth_per_flag[flag].sort()
            middle_index = int(len(bandwidth_per_flag[flag])/2)
            median_bandwidth_per_flag.labels(status='all', flag=flag).set(bandwidth_per_flag[flag][middle_index])
    for flag in online_bandwidth_per_flag.keys():
        if flag is not None and flag.isalpha():
            online_bandwidth_per_flag[flag].sort()
            middle_index = int(len(online_bandwidth_per_flag[flag])/2)
            median_bandwidth_per_flag.labels(status='online', flag=flag).set(online_bandwidth_per_flag[flag][middle_index])
    for flag in offline_bandwidth_per_flag.keys():
        if flag is not None and flag.isalpha():
            offline_bandwidth_per_flag[flag].sort()
            middle_index = int(len(offline_bandwidth_per_flag[flag])/2)
            median_bandwidth_per_flag.labels(status='offline', flag=flag).set(offline_bandwidth_per_flag[flag][middle_index])

    network_consensus_is_fresh = Gauge('network_consensus_is_fresh', 'Current network consensus freshness', registry=registry)
    network_consensus_is_fresh.set(consensus_is_fresh)

    network_consensus_is_valid = Gauge('network_consensus_is_valid', 'Current network consensus validity', registry=registry)
    network_consensus_is_valid.set(consensus_is_valid)

    file_path = os.getenv('METRICS_FILE_PATH', '/srv/onionoo/data/out/network/metrics')
    write_to_textfile(file_path, registry)

    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{current_time}] - Ok!")
