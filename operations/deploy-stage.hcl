job "onionoo-stage" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "ator-network"

  update {
    max_parallel      = 1
    healthy_deadline  = "15m"
    progress_deadline = "20m"
  }

  group "onionoo-stage-group" {
    count = 1

    volume "onionoo-data" {
      type      = "host"
      read_only = false
      source    = "onionoo-stage"
    }

    network {
      mode = "bridge"
      port "http-port" {
        static = 9190
        to     = 8080
        host_network = "wireguard"
      }
    }

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "onionoo-jar-stage-task" {
      driver = "docker"

      template {
        data = <<EOH
            BASE_DIR="/srv/onionoo"
            LOGBASE="data/logs"
            TYPE="jar"
	      {{- range service "collector-stage" }}
  	        COLLECTOR_HOST="{{ .Address }}:{{ .Port }}"
	      {{ end -}}
            COLLECTOR_PROTOCOL="http://"
            UPDATER_PERIOD="5"
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

      resources {
        cpu    = 512
        memory = 2048
      }

      service {
        name = "onionoo-jar-stage"
        tags = ["logging"]
      }
    }

    task "onionoo-war-stage-task" {
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

      resources {
        cpu    = 256
        memory = 512
      }

      service {
        name = "onionoo-war-stage"
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

    task "onionoo-cron-stage-task" {
      driver = "docker"

      env {
        ONIONOO_HOST      = "http://127.0.0.1:8080"
        INTERVAL_MINUTES  = "60"
        METRICS_FILE_PATH = "/srv/onionoo/data/out/network/metrics"
        CRON_EXPRESSION = "*/5 * * * *"
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

      service {
        name = "onionoo-cron-stage"
        tags = ["logging"]
      }

      resources {
        cpu    = 128
        memory = 128
      }
    }
  }
}
