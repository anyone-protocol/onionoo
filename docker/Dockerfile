FROM debian:bookworm

ADD .. /srv/onionoo/temp

WORKDIR /srv/onionoo

RUN apt-get update && \
    apt-get install -y default-jdk ant ivy git curl && \
    cd temp && \
    git submodule init && \
    git submodule update && \
    ant fetch-metrics-lib && \
    ant -lib /usr/share/java resolve && \
    ant tar && \
    cp generated/dist/**dev.jar generated/dist/**dev.war docker/**.sh .. && \
    cd .. && \
    rm -rf temp

EXPOSE 8080

ENTRYPOINT ["sh", "docker-entrypoint.sh"]
