job "onionoo-stage" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "stage-network"

  constraint {
    attribute = "${meta.pool}"
    value = "stage"
  }

  update {
    max_parallel     = 1
    canary           = 1
    min_healthy_time = "30s"
    healthy_deadline = "5m"
    auto_revert      = true
    auto_promote     = true
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
        to     = 8080
        host_network = "wireguard"
      }
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
        address_mode = "alloc"
        check_restart {
          limit = 10
          grace = "30s"
        }
      }
    }

    task "onionoo-jar-stage-task" {
      driver = "docker"

      template {
        data = <<-EOH
        BASE_DIR="/srv/onionoo"
        LOGBASE="{{ env `NOMAD_ALLOC_DIR` }}/logs"
        TYPE="jar"
        {{- range service "collector-stage" }}
        COLLECTOR_HOST="{{ .Address }}:{{ .Port }}"
        {{- end }}
        COLLECTOR_PROTOCOL="http://"
        UPDATER_PERIOD="5"
        UPDATER_OFFSET="0"
        {{- range service "api-service-stage" }}
        API_SERVICE_URL="http://{{ .Address }}:{{ .Port }}"
        {{- end }}
        EOH
        destination = "local/config.env"
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
        LOGBASE  = "${NOMAD_ALLOC_DIR}/logs"
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
      }

      resources {
        cpu    = 256
        memory = 512
      }
    }

    task "onionoo-cron-stage-task" {
      driver = "docker"

      env {
        CRON_EXPRESSION = "*/5 * * * *"
        ONIONOO_HOST = "http://127.0.0.1:8080"
        INTERVAL_MINUTES = "60"
        METRICS_FILE_PATH = "/srv/onionoo/data/out/network/metrics"
      }

      config {
        image   = "ghcr.io/anyone-protocol/onionoo-cron:DEPLOY_TAG"
        image_pull_timeout = "15m"
      }

      volume_mount {
        volume      = "onionoo-data"
        destination = "/srv/onionoo/data"
        read_only   = false
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
