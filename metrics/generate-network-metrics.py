import json
import re
import requests
import time
import os
import csv
from datetime import datetime, timedelta

from prometheus_client import CollectorRegistry, Gauge, write_to_textfile

# OnionPerf metrics

def throughput_parse_csv(file_path):
    if not os.path.exists(file_path):
        print(f"File {file_path} does not exist.")
        return []
    
    data = []
    with open(file_path, mode='r') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            # Convert numeric values from strings to integers
            row['low'] = int(row['low'])
            row['q1'] = int(row['q1'])
            row['md'] = int(row['md'])
            row['q3'] = int(row['q3'])
            row['high'] = int(row['high'])
            # Add parsed row to the data list
            data.append(row)
    return data

def latency_parse_csv(file_path):
    if not os.path.exists(file_path):
        print(f"File {file_path} does not exist.")
        return []

    data = []
    with open(file_path, mode='r') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            # Convert numeric values from strings to integers
            row['low'] = int(row['low'])
            row['q1'] = int(row['q1'])
            row['md'] = int(row['md'])
            row['q3'] = int(row['q3'])
            row['high'] = int(row['high'])
            # Add parsed row to the data list
            data.append(row)
    return data

def failure_parse_csv(file_path):
    if not os.path.exists(file_path):
        print(f"File {file_path} does not exist.")
        return []

    data = []
    with open(file_path, mode='r') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            # Convert numeric values from strings to integers
            row['timeout'] = int(row['timeout'])
            row['failure'] = int(row['failure'])
            row['requests'] = int(row['requests'])
            # Add parsed row to the data list
            data.append(row)
    return data

def download_parse_csv(file_path):
    if not os.path.exists(file_path):
        print(f"File {file_path} does not exist.")
        return []

    data = []
    with open(file_path, mode='r') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            # Convert numeric values from strings to integers
            row['q1'] = int(row['q1'])
            row['md'] = int(row['md'])
            row['q3'] = int(row['q3'])
            # Add parsed row to the data list
            data.append(row)
    return data

def circuit_parse_csv(file_path):
    if not os.path.exists(file_path):
        print(f"File {file_path} does not exist.")
        return []

    data = []
    with open(file_path, mode='r') as file:
        csv_reader = csv.DictReader(file)
        for row in csv_reader:
            # Convert numeric values from strings to integers
            row['q1'] = int(row['q1'])
            row['md'] = int(row['md'])
            row['q3'] = int(row['q3'])
            # Add parsed row to the data list
            data.append(row)
    return data

def filter_latest_date(data):
    if not data:
        return []
    # Find the greatest date
    latest_date = max(row['date'] for row in data)
    # Filter rows to keep only those with the greatest date
    filtered_data = [row for row in data if row['date'] == latest_date]
    return filtered_data

def parse_estimated_users_count(json_file_path):
    # Check if the file exists
    if not os.path.exists(json_file_path):
        return None

    # Read and parse the JSON file
    with open(json_file_path, 'r') as file:
        data = json.load(file)

    # Extract the "estimated" section
    estimated_data = data.get("estimated", [])
    if not estimated_data:
        return None

    # Find the latest date across all records
    latest_date = None
    frac_value = None
    for record in estimated_data:
        current_date = datetime.strptime(record["date"], "%Y-%m-%d")
        if latest_date is None or current_date > latest_date:
            latest_date = current_date
            frac_value = record["frac"]

    # Filter records that match the latest date
    metrics = {}
    for record in estimated_data:
        current_date = datetime.strptime(record["date"], "%Y-%m-%d")
        if current_date == latest_date:
            country = record["country"]
            users = record["users"]
            metrics[country] = users

    return metrics, frac_value


def throughput_generate_prometheus_metrics(data, registry):
    op_throughput_mbps = Gauge("op_throughput_mbps", "OnionPerf throughput Mbps", ['source', 'server', 'percentile'], registry=registry)

    for row in data:
        source = row['source']
        server = row['server']
        for percentile in ['low', 'q1', 'md', 'q3', 'high']:
            value = row[percentile]
            op_throughput_mbps.labels(source=source, server=server, percentile=percentile).set(value / 1000)

def latency_generate_prometheus_metrics(data, registry):
    op_latency_millis = Gauge("op_latency_millis", "OnionPerf latency milliseconds", ['source', 'server', 'percentile'], registry=registry)

    for row in data:
        source = row['source']
        server = row['server']
        for percentile in ['low', 'q1', 'md', 'q3', 'high']:
            value = row[percentile]
            op_latency_millis.labels(source=source, server=server, percentile=percentile).set(value)

def failure_generate_prometheus_metrics(data, registry):
    op_requests_all_count = Gauge("op_requests_all_count", "OnionPerf requests all count", ['source', 'server'], registry=registry)
    op_requests_failure_count = Gauge("op_requests_failure_count", "OnionPerf requests failure count", ['source', 'server'], registry=registry)
    op_requests_timeout_count = Gauge("op_requests_timeout_count", "OnionPerf requests timeout count", ['source', 'server'], registry=registry)
    op_requests_failure_percentage = Gauge("op_requests_failure_percentage", "OnionPerf requests failure percentage", ['source', 'server'], registry=registry)
    op_requests_timeout_percentage = Gauge("op_requests_timeout_percentage", "OnionPerf requests timeout percentage", ['source', 'server'], registry=registry)

    for row in data:
        source = row['source']
        server = row['server']
        requests = row['requests'] if row['requests'] > 0 else 1  # Avoid division by zero
        op_requests_all_count.labels(source=source, server=server).set(requests)
        op_requests_failure_count.labels(source=source, server=server).set(row['failure'])
        op_requests_timeout_count.labels(source=source, server=server).set(row['timeout'])
        op_requests_failure_percentage.labels(source=source, server=server).set((row['failure'] / requests) * 100)
        op_requests_timeout_percentage.labels(source=source, server=server).set((row['timeout'] / requests) * 100)

def download_generate_prometheus_metrics(data, registry):
    op_download_sec = Gauge("op_download_sec", "OnionPerf download sec", ['source', 'server', 'filesize', 'percentile'], registry=registry)

    for row in data:
        source = row['source']
        server = row['server']
        filesize = row['filesize']
        for percentile in ['q1', 'md', 'q3']:
            value = row[percentile]
            op_download_sec.labels(source=source, server=server, filesize=filesize, percentile=percentile).set(value / 1000)

def circuit_generate_prometheus_metrics(data, registry):
    op_circuit_millis = Gauge("op_circuit_millis", "OnionPerf circuit millis", ['source', 'position', 'percentile'], registry=registry)

    for row in data:
        source = row['source']
        position = row['position']
        for percentile in ['q1', 'md', 'q3']:
            value = row[percentile]
            op_circuit_millis.labels(source=source, position=position, percentile=percentile).set(value)


def userstats_generate_prometheus_metrics(data, frac, registry):
    if data:
        op_userstats_count = Gauge("total_number_of_users", "Number of users", ['country'], registry=registry)
        op_userstats_fraction = Gauge("total_number_of_users_fraction", "Fraction for user metrics", registry=registry)

        for country, users in data.items():
            op_userstats_count.labels(country=country).set(users)

        op_userstats_fraction.set(frac)

        # main
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

    # OnionPerf
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

    network_observed_bandwidth = Gauge('total_observed_bandwidth', 'Current total observed bandwidth on the network', ['status'], registry=registry)
    network_observed_bandwidth.labels(status='all').set(total_observed_bandwidth)
    network_observed_bandwidth.labels(status='online').set(online_observed_bandwidth)
    network_observed_bandwidth.labels(status='offline').set(offline_observed_bandwidth)

    network_bandwidth_rate = Gauge('average_bandwidth_rate', 'Current average bandwidth rate on the network', ['status'], registry=registry)
    average_bandwidth_rate = 0 if len(total_relays) == 0 else total_bandwidth_rate / len(total_relays))
    network_bandwidth_rate.labels(status='all').set(average_bandwidth_rate)
    network_bandwidth_rate.labels(status='online').set(0 if len(online_relays) == 0 else online_bandwidth_rate / len(online_relays))
    network_bandwidth_rate.labels(status='online').set(average_bandwidth_rate_online)
    network_bandwidth_rate.labels(status='offline').set(0 if len(offline_relays) == 0 else offline_bandwidth_rate / len(offline_relays))
    network_bandwidth_rate.labels(status='offline').set(average_bandwidth_rate_offline)

    online_measured_count = Gauge('total_online_measured_count', 'Current total online measured relays', registry=registry)
    online_measured_count.set(total_online_measured)

    online_unmeasured_count = Gauge('total_online_unmeasured_count', 'Current total online unmeasured relays', registry=registry)
    online_unmeasured_count.set(total_online_unmeasured)

    online_measured_ratio = Gauge('total_online_measured_ratio', 'Current total online measured ratio', registry=registry)
    online_measured_ratio.set(0 if total_online_unmeasured == 0 else total_online_measured / total_online_unmeasured)
    
    online_measured_percentage = Gauge('total_online_measured_percentage', 'Current total online measured percentage', registry=registry)
    online_measured_percentage.set(0 if len(online_relays) == 0 else total_online_measured / len(online_relays) * 100)

    countries_average_observed_bandwidth = Gauge('countries_average_observed_bandwidth', 'Average observed bandwidth per country', ['status','country'], registry=registry)
    for country in total_countries_bandwidth.keys():
        if country and country.isalpha():
            count = total_countries_relays.get(country, 0)
            countries_average_observed_bandwidth.labels(status='all', country=country).set(0 if count == 0 else total_countries_bandwidth[country] / count)
    for country in online_countries_relays.keys():
        if country and country.isalpha():
            count = online_countries_relays.get(country, 0)
            countries_average_observed_bandwidth.labels(status='online', country=country).set(0 if count == 0 else online_countries_bandwidth[country] / count)
    for country in offline_countries_relays.keys():
        if country and country.isalpha():
            count = offline_countries_relays.get(country, 0)
            countries_average_observed_bandwidth.labels(status='offline', country=country).set(0 if count == 0 else offline_countries_bandwidth[country] / count)

    median_bandwidth_per_flag = Gauge('median_bandwidth_per_flag', 'Median bandwidth per flag', ['status', 'flag'], registry=registry)
    for flag in bandwidth_per_flag.keys():
        if flag and flag.isalpha() and len(bandwidth_per_flag[flag]) > 0:
            bandwidth_per_flag[flag].sort()
            middle_index = len(bandwidth_per_flag[flag]) // 2
            median_bandwidth_per_flag.labels(status='all', flag=flag).set(bandwidth_per_flag[flag][middle_index])
    for flag in online_bandwidth_per_flag.keys():
        if flag and flag.isalpha() and len(online_bandwidth_per_flag[flag]) > 0:
            online_bandwidth_per_flag[flag].sort()
            middle_index = len(online_bandwidth_per_flag[flag]) // 2
            median_bandwidth_per_flag.labels(status='online', flag=flag).set(online_bandwidth_per_flag[flag][middle_index])
    for flag in offline_bandwidth_per_flag.keys():
        if flag and flag.isalpha() and len(offline_bandwidth_per_flag[flag]) > 0:
            offline_bandwidth_per_flag[flag].sort()
            middle_index = len(offline_bandwidth_per_flag[flag]) // 2
            median_bandwidth_per_flag.labels(status='offline', flag=flag).set(offline_bandwidth_per_flag[flag][middle_index])

    network_consensus_is_fresh = Gauge('network_consensus_is_fresh', 'Current network consensus freshness', registry=registry)
    network_consensus_is_fresh.set(consensus_is_fresh)

    network_consensus_is_valid = Gauge('network_consensus_is_valid', 'Current network consensus validity', registry=registry)
    network_consensus_is_valid.set(consensus_is_valid)

    throughput_file_path = '/srv/onionoo/data/out/performance/throughput.csv'  # Replace with the actual path to your CSV file
    throughput_parsed_data = throughput_parse_csv(throughput_file_path)
    throughput_latest_data = filter_latest_date(throughput_parsed_data)
    throughput_generate_prometheus_metrics(throughput_latest_data, registry)

    latency_file_path = '/srv/onionoo/data/out/performance/latency.csv'  # Replace with the actual path to your CSV file
    latency_parsed_data = latency_parse_csv(latency_file_path)
    latency_latest_data = filter_latest_date(latency_parsed_data)
    latency_generate_prometheus_metrics(latency_latest_data, registry)

    failure_file_path = '/srv/onionoo/data/out/performance/failure.csv'  # Replace with the actual path to your CSV file
    failure_parsed_data = failure_parse_csv(failure_file_path)
    failure_latest_data = filter_latest_date(failure_parsed_data)
    failure_generate_prometheus_metrics(failure_latest_data, registry)

    download_file_path = '/srv/onionoo/data/out/performance/download.csv'  # Replace with the actual path to your CSV file
    download_parsed_data = download_parse_csv(download_file_path)
    download_latest_data = filter_latest_date(download_parsed_data)
    download_generate_prometheus_metrics(download_latest_data, registry)

    circuit_file_path = '/srv/onionoo/data/out/performance/circuit.csv'  # Replace with the actual path to your CSV file
    circuit_parsed_data = circuit_parse_csv(circuit_file_path)
    circuit_latest_data = filter_latest_date(circuit_parsed_data)
    circuit_generate_prometheus_metrics(circuit_latest_data, registry)

    estimated_users_path = '/srv/onionoo/data/status/userstats'
    estimated_parsed_data, frac_data = parse_estimated_users_count(estimated_users_path)
    userstats_generate_prometheus_metrics(estimated_parsed_data, frac_data, registry)

    file_path = os.getenv('METRICS_FILE_PATH', '/srv/onionoo/data/out/network/metrics')
    write_to_textfile(file_path, registry)

    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{current_time}] - Ok!")
