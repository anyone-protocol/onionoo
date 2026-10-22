FROM debian:bookworm AS build

RUN apt-get update && \
    apt-get install -y default-jdk ant ivy git curl

WORKDIR /src

COPY . .

RUN ant fetch-metrics-lib && \
    ant -lib /usr/share/java resolve && \
    ant tar && \
    mkdir /out && \
    cp -r generated/dist/*dev.jar generated/dist/*dev.war docker/*.sh geoip /out/

FROM debian:bookworm

RUN apt-get update && \
    apt-get install -y --no-install-recommends default-jre-headless && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /srv/onionoo

COPY --from=build /out/ ./

EXPOSE 8080

ENTRYPOINT ["sh", "docker-entrypoint.sh"]
