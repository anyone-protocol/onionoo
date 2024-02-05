cd /srv/onionoo.torproject.org/onionoo

nohup java -jar onionoo-8.3-1.30.0-dev.war &

java -Dcollector.tor.hosts=${COLLECTOR_HOST} -Dcollector.host.protocol=${COLLECTOR_PROTOCOL} -Dupdater.period.minutes=${UPDATER_PERIOD} -Dupdater.offset.minutes=${UPDATER_OFFSET} -jar onionoo-8.3-1.30.0-dev.jar
