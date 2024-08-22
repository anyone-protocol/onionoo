FROM python:3.9-slim

RUN mkdir -p /srv/onionoo

WORKDIR /srv/onionoo

RUN apt-get update && apt-get install -y cron pip && pip3 install requests prometheus_client pandas

COPY generate-network-metrics.py .

COPY docker-entrypoint.sh .

ENTRYPOINT ["sh", "docker-entrypoint.sh"]
