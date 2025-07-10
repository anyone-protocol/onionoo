job "onionoo-live" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "live-network"

  constraint {
    attribute = "${meta.pool}"
    value = "live-network"
  }

  update {
    max_parallel      = 1
    healthy_deadline  = "15m"
    progress_deadline = "20m"
  }

  group "onionoo-live-group" {
    count = 1

    volume "onionoo-data" {
      type      = "host"
      read_only = false
      source    = "onionoo-live"
    }

    network {
      mode = "bridge"
      port "http-port" {
        to     = 8080
        host_network = "wireguard"
      }
    }

    service {
      name = "onionoo-war-live"
      port = "http-port"
      [
        "logging",
        "traefik-ops.enable=true",
        "traefik-ops.http.routers.onionoo-live.rule=Host(`onionoo.ops.anyone.tech`)",
        "traefik-ops.http.routers.onionoo-live.entrypoints=https",
        "traefik-ops.http.routers.onionoo-live.tls=true",
        "traefik-ops.http.routers.onionoo-live.tls.certresolver=anyoneresolver",
        "traefik-ops.http.routers.onionoo-live.middlewares=oauth2-errors@consulcatalog,oauth2-proxy@consulcatalog",
      ]
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

    task "onionoo-jar-live-task" {
      driver = "docker"

      template {
        data = <<-EOH
        BASE_DIR="/srv/onionoo"
        LOGBASE="{{ env `NOMAD_ALLOC_DIR` }}/logs"
        TYPE="jar"
	      {{- range service "collector-live" }}
        COLLECTOR_HOST="{{ .Address }}:{{ .Port }}"
	      {{- end }}
        COLLECTOR_PROTOCOL="http://"
        UPDATER_PERIOD="5"
        UPDATER_OFFSET="0"
        {{- range service "api-service-live" }}
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
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      resources {
        cpu    = 4096
        memory = 16384
      }

      service {
        name = "onionoo-jar-live"
        tags = ["logging"]
      }
    }

    task "onionoo-war-live-task" {
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
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      resources {
        cpu    = 2048
        memory = 4096
      }
    }

    task "onionoo-cron-live-task" {
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
        name = "onionoo-cron-live"
        tags = ["logging"]
      }

      resources {
        cpu    = 256
        memory = 256
      }
    }
  }
}
