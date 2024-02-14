mkdir -p data/out/network && touch data/out/network/metrics

mkdir -p data/logs && touch data/logs/cron.log

crontab -l | { cat; echo "*/$INTERVAL_MINUTES * * * * export ONIONOO_HOST=$ONIONOO_HOST METRICS_FILE_PATH=$METRICS_FILE_PATH; /usr/local/bin/python3 /srv/onionoo/generate-network-metrics.py >> /srv/onionoo/data/logs/cron.log 2>&1"; } | crontab -

cron -f
