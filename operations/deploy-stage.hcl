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
	      {{- range nomadService "collector-dev" }}
  	        COLLECTOR_HOST=http://{{ .Address }}:{{ .Port }}/api/v1/write
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
        image   = "svforte/onionoo"
        volumes = [
          "local/logs/:/srv/onionoo/data/logs"
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
        image   = "svforte/onionoo"
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
