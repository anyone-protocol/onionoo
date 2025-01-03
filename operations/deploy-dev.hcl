job "onionoo-dev" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "ator-network"

  update {
    max_parallel      = 1
    healthy_deadline  = "15m"
    progress_deadline = "20m"
  }

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
	{{- range service "collector-dev" }}
  	    COLLECTOR_HOST="{{ .Address }}:{{ .Port }}"
	{{ end -}}
            COLLECTOR_PROTOCOL="http://"
            UPDATER_PERIOD="1"
            UPDATER_OFFSET="0"
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
        image   = "ghcr.io/anyone-protocol/onionoo:DEPLOY_TAG"
        image_pull_timeout = "15m"
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      vault {
      	policies = ["ator-network-read"]
      }

      resources {
        cpu    = 512
        memory = 768
      }

      service {
        name = "onionoo-jar-dev"
        tags = ["logging"]
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
        read_only   = false
      }

      config {
        image   = "ghcr.io/anyone-protocol/onionoo:DEPLOY_TAG"
        image_pull_timeout = "15m"
        ports   = ["http-port"]
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      vault {
      	policies = ["ator-network-read"]
      }

      resources {
        cpu    = 250
        memory = 500
      }

      service {
        name = "onionoo-war-dev"
        port = "http-port"
        tags = ["logging"]
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
        CRON_EXPRESSION = "* * * * *"
      }

      volume_mount {
        volume      = "onionoo-data"
        destination = "/srv/onionoo/data"
        read_only   = false
      }

      config {
        image   = "ghcr.io/anyone-protocol/onionoo-cron:DEPLOY_TAG"
        image_pull_timeout = "15m"
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      vault {
      	policies = ["ator-network-read"]
      }

      service {
        name = "onionoo-cron-dev"
        tags = ["logging"]
      }

      resources {
        cpu    = 128
        memory = 128
      }
    }
  }
}
