mkdir -p data/out/network && touch data/out/network/metrics

mkdir -p data/logs && touch data/logs/cron.log

crontab /etc/cron.d/network-metrics-cron

cron -f
