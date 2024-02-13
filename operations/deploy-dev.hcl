job "onionoo-dev" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "ator-network"

  group "onionoo-dev-group" {
    count = 1

    constraint {
      attribute = "${node.unique.id}"
      value     = "c8e55509-a756-0aa7-563b-9665aa4915ab"
    }

    #    volume "onionoo-data" {
    #      type      = "host"
    #      read_only = false
    #      source    = "onionoo-data"
    #    }

    network {
      port "http-port" {
        static = 9090
        to     = 8080
      }
    }

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "onionoo-jar-dev-task" {
      driver = "docker"

      env {
        BASE_DIR           = "/srv/onionoo"
        LOGBASE            = "data/logs"
        TYPE               = "jar"
        COLLECTOR_HOST     = "88.99.219.105:9000"
        COLLECTOR_PROTOCOL = "http://"
        UPDATER_PERIOD     = "5"
        UPDATER_OFFSET     = "3"
      }

      #      volume_mount {
      #        volume      = "onionoo-data"
      #        destination = "/srv/onionoo/data"
      #        read_only   = false
      #      }

      config {
        image   = "svforte/onionoo:latest-dev"
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      resources {
        cpu    = 256
        memory = 512
      }

      service {
        name = "onionoo-jar-dev"
      }
    }

    task "onionoo-war-dev-task" {
      driver = "docker"

      env {
        BASE_DIR = "/srv/onionoo"
        LOGBASE  = "data/logs"
        TYPE     = "war"
      }

      #      volume_mount {
      #        volume      = "onionoo-data"
      #        destination = "/srv/onionoo/data"
      #        read_only   = true
      #      }

      config {
        image   = "svforte/onionoo:latest-dev"
        ports   = ["http-port"]
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      resources {
        cpu    = 256
        memory = 256
      }

      service {
        name = "onionoo-war-dev"
        port = "http-port"
        #        tags = [
        #          "traefik.enable=true",
        #          "traefik.http.routers.deb-repo.entrypoints=https",
        #          "traefik.http.routers.deb-repo.rule=Host(`dev.onionoo.dmz.ator.dev`)",
        #          "traefik.http.routers.deb-repo.tls=true",
        #          "traefik.http.routers.deb-repo.tls.certresolver=atorresolver",
        #        ]
      }
    }

    task "onionoo-cron-dev-task" {
      driver = "docker"

      env {
        ONIONOO_HOST      = "http://88.99.219.105:9090"
        METRICS_FILE_PATH = "/srv/onionoo/data/out/network/metrics"
      }

      #      volume_mount {
      #        volume      = "onionoo-data"
      #        destination = "/srv/onionoo/data"
      #        read_only   = true
      #      }

      config {
        image   = "svforte/onionoo-cron:latest-dev"
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      resources {
        cpu    = 256
        memory = 256
      }

      service {
        name = "onionoo-cron-dev"
      }
    }
  }
}