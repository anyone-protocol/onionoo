job "onionoo-dev" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "ator-network"

  group "onionoo-dev-group" {
    count = 1

    volume "onionoo-data" {
      type      = "host"
      read_only = false
      source    = "onionoo-dev"
    }

    network {
      mode = "bridge"
      port "http-port" {
        static = 9090
        to     = 8080
        host_network = "wireguard"
      }
    }

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "onionoo-jar-dev-task" {
      driver = "docker"
      template {
        data = <<EOH
            BASE_DIR="/srv/onionoo"
            LOGBASE="data/logs"
            TYPE="jar"
	{{- range nomadService "collector-dev" }}
  	    COLLECTOR_HOST="{{ .Address }}:{{ .Port }}"
	{{ end -}}                
            COLLECTOR_PROTOCOL="http://"
            UPDATER_PERIOD="5"
            UPDATER_OFFSET="3"
            EOH
        destination = "secrets/file.env"
        env         = true
      }

      volume_mount {
        volume      = "onionoo-data"
        destination = "/srv/onionoo/data"
        read_only   = false
      }

      config {
        image   = "svforte/onionoo:latest-dev"
        force_pull = true
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      vault {
      	policies = ["ator-network-read"]
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

      volume_mount {
        volume      = "onionoo-data"
        destination = "/srv/onionoo/data"
        read_only   = true
      }

      config {
        image   = "svforte/onionoo:latest-dev"
        force_pull = true
        ports   = ["http-port"]
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      vault {
      	policies = ["ator-network-read"]
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
        check {
          name     = "Onionoo web server check"
          type     = "http"
          port     = "http-port"
          path     = "/"
          interval = "10s"
          timeout  = "10s"
          check_restart {
            limit = 10
            grace = "30s"
          }
        }
      }
    }

    task "onionoo-cron-dev-task" {
      driver = "docker"

      env {
        ONIONOO_HOST      = "http://127.0.0.1:8080"
        INTERVAL_MINUTES  = "5"
        METRICS_FILE_PATH = "/srv/onionoo/data/out/network/metrics"
      }

      volume_mount {
        volume      = "onionoo-data"
        destination = "/srv/onionoo/data"
        read_only   = false
      }

      config {
        image   = "svforte/onionoo-cron:latest-dev"
        force_pull = true
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      vault {
      	policies = ["ator-network-read"]
      }

      resources {
        cpu    = 256
        memory = 256
      }
    }
  }
}
