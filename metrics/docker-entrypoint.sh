mkdir -p data/out/network && touch data/out/network/metrics

mkdir -p data/logs && touch data/logs/cron.log

# Handle case where no crontab exists for root user
(crontab -l 2>/dev/null || echo "") | { cat; echo "$CRON_EXPRESSION export INTERVAL_MINUTES=$INTERVAL_MINUTES ONIONOO_HOST=$ONIONOO_HOST METRICS_FILE_PATH=$METRICS_FILE_PATH; /usr/local/bin/python3 /srv/onionoo/generate-network-metrics.py >> /srv/onionoo/data/logs/cron.log 2>&1"; } | crontab -

cron -f
