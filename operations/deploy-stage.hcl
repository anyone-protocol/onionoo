job "onionoo-stage" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "ator-network"

  group "onionoo-stage-group" {
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
        static = 9190
        to     = 8080
      }
    }

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "onionoo-jar-stage-task" {
      driver = "docker"

      env {
        COLLECTOR_HOST     = "88.99.219.105:9000"
        COLLECTOR_PROTOCOL = "http://"
        UPDATER_PERIOD     = "5"
        UPDATER_OFFSET     = "3"
        LOGBASE            = "data/logs"
        DIR_BASE           = "/srv/onionoo"
        TYPE               = "jar"
      }

      #      volume_mount {
      #        volume      = "onionoo-data"
      #        destination = "/srv/onionoo/data"
      #        read_only   = false
      #      }

      config {
        image   = "svforte/onionoo"
        volumes = [
          "local/logs/:/srv/onionoo/data/logs",
          # todo - remove when mount is done
          "local/data/:/srv/onionoo/data",
        ]
      }

      resources {
        cpu    = 256
        memory = 512
      }

      service {
        name = "onionoo-jar-stage"
      }
    }

    task "onionoo-war-stage-task" {
      driver = "docker"

      env {
        LOGBASE  = "data/logs"
        DIR_BASE = "/srv/onionoo"
        TYPE     = "war"
      }

      #      volume_mount {
      #        volume      = "onionoo-data"
      #        destination = "/srv/onionoo/data"
      #        read_only   = true
      #      }

      config {
        image   = "svforte/onionoo"
        ports   = ["http-port"]
        volumes = [
          "local/logs/:/srv/onionoo/data/logs",
          # todo - remove when mount is done
          "local/data/:/srv/onionoo/data",
        ]
      }

      resources {
        cpu    = 256
        memory = 256
      }

      service {
        name = "onionoo-war-stage"
        port = "http-port"
        #        tags = [
        #          "traefik.enable=true",
        #          "traefik.http.routers.deb-repo.entrypoints=https",
        #          "traefik.http.routers.deb-repo.rule=Host(`stage.onionoo.dmz.ator.dev`)",
        #          "traefik.http.routers.deb-repo.tls=true",
        #          "traefik.http.routers.deb-repo.tls.certresolver=atorresolver",
        #        ]
      }
    }
  }
}