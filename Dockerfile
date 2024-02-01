FROM debian:bookworm

RUN apt update && apt install -y wget apt-transport-https gnupg && mkdir -p /etc/apt/keyrings

RUN wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | tee /etc/apt/keyrings/adoptium.asc

RUN echo "deb [signed-by=/etc/apt/keyrings/adoptium.asc] https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list

RUN apt update && apt install -y temurin-17-jdk ant ivy git curl

ADD . /srv/onionoo.torproject.org/onionoo/temp

WORKDIR /srv/onionoo.torproject.org/onionoo/temp

RUN git submodule init && git submodule update

RUN ant fetch-metrics-lib && ant -lib /usr/share/java resolve

RUN ant tar

RUN cp generated/dist/**dev.jar generated/dist/**dev.war docker-entrypoint.sh ..

RUN cd .. && rm -rf temp && mkdir out

EXPOSE 8080

ENTRYPOINT ["sh", "/srv/onionoo.torproject.org/onionoo/docker-entrypoint.sh"]
