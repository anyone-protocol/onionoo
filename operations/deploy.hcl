job "anon-onionoo-deploy" {
  datacenters = ["ator-fin"]
  type = "service"
  namespace = "ator-network"

  group "anon-onionoo-group" {
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

    network  {
#      port "reprepro-ssh" {
#        static = 22
#      }
      port "onionoo-http" {
        static = 8080
      }
    }

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "anon-onionoo-task" {
      driver = "docker"

      env {
        COLLECTOR_HOST = "51.68.169.208:9000"
        COLLECTOR_PROTOCOL: "http://"
        UPDATER_PERIOD: 5
        UPDATER_OFFSET: 2
      }

      #      volume_mount {
      #        volume      = "onionoo-data"
      #        destination = "/srv/onionoo"
      #        read_only   = false
      #      }

      config {
        image = "svforte/onionoo"
        ports = ["onionoo-http"]
        #        volumes = [
        #          "local/default.conf:/etc/nginx/conf.d/default.conf:ro",
        #        ]
      }

      resources {
        cpu    = 256
        memory = 256
      }

      service {
        name = "anon-onionoo"
        port = "onionoo-http"
        #        tags = [
        #          "traefik.enable=true",
        #          "traefik.http.routers.deb-repo.entrypoints=https",
        #          "traefik.http.routers.deb-repo.rule=Host(`deb.dmz.ator.dev`)",
        #          "traefik.http.routers.deb-repo.tls=true",
        #          "traefik.http.routers.deb-repo.tls.certresolver=atorresolver",
        #        ]
        check {
          name     = "onionoo web http server alive"
          type     = "tcp"
          interval = "10s"
          timeout  = "10s"
          check_restart {
            limit = 10
            grace = "30s"
          }
        }
      }
    }
  }
}