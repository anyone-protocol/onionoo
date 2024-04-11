job "onionoo-live" {
  datacenters = ["ator-fin"]
  type        = "service"
  namespace   = "ator-network"

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
        static = 9290
        to     = 8080
        host_network = "wireguard"
      }
    }

    ephemeral_disk {
      migrate = true
      sticky  = true
    }

    task "onionoo-jar-live-task" {
      driver = "docker"

      template {
        data = <<EOH
            BASE_DIR="/srv/onionoo"
            LOGBASE="data/logs"
            TYPE="jar"
	      {{- range nomadService "collector-live" }}
  	        COLLECTOR_HOST="{{ .Address }}:{{ .Port }}"
	      {{ end -}}
            COLLECTOR_PROTOCOL="http://"
            UPDATER_PERIOD="60"
            UPDATER_OFFSET="6"
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
        image   = "svforte/onionoo:latest"
        force_pull = true
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      resources {
        cpu    = 256
        memory = 512
      }

      service {
        name = "onionoo-jar-live"
      }
    }

    task "onionoo-war-live-task" {
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
        image   = "svforte/onionoo:latest"
        force_pull = true
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
        name = "onionoo-war-live"
        port = "http-port"
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

    task "onionoo-cron-live-task" {
      driver = "docker"

      env {
        ONIONOO_HOST      = "http://127.0.0.1:8080"
        INTERVAL_MINUTES  = "60"
        METRICS_FILE_PATH = "/srv/onionoo/data/out/network/metrics"
        CRON_EXPRESSION = "5 * * * *"
      }

      volume_mount {
        volume      = "onionoo-data"
        destination = "/srv/onionoo/data"
        read_only   = false
      }

      config {
        image   = "svforte/onionoo-cron:latest"
        force_pull = true
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
        ]
      }

      resources {
        cpu    = 256
        memory = 256
      }
    }
  }
}
