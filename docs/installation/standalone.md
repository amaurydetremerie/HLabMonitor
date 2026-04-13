# Standalone JAR Installation

## Prerequisites

- **Java 21+** — OpenJDK, Temurin, Amazon Corretto, or any compatible JVM

```bash
java -version  # must show 21+
```

## Installation

### 1. Download the JAR

Get the latest JAR from the [GitHub Releases](https://github.com/amaurydetremerie/HLabMonitor/releases):

```bash
# Replace <version> with the actual release, e.g. 0.0.23
wget https://github.com/amaurydetremerie/HLabMonitor/releases/download/v<version>/hlabmonitor-<version>.jar
sudo mkdir -p /opt/hlabmonitor
sudo mv hlabmonitor-*.jar /opt/hlabmonitor/hlabmonitor.jar
```

### 2. Create configuration

```bash
sudo mkdir -p /etc/hlabmonitor
sudo nano /etc/hlabmonitor/application.yaml
```

Minimal configuration (SQLite, log notifications only):

```yaml
database:
  type: sqlite
  # path defaults to /var/lib/hlabmonitor/monitor.db on Linux

monitoring:
  ping:
    my-server:
      target: 192.168.1.1
      interval: 1m
  notification:
    email:
      enabled: false
    discord:
      enabled: false
    telegram:
      enabled: false
    log:
      enabled: true
      level: INFO
```

Ensure the SQLite data directory exists:

```bash
sudo mkdir -p /var/lib/hlabmonitor
```

### 3. Run manually

```bash
java -jar /opt/hlabmonitor/hlabmonitor.jar
```

Custom config file:

```bash
HLABMONITOR_CONFIG_LOCATION=/path/to/my-config.yaml java -jar /opt/hlabmonitor/hlabmonitor.jar
# or via Spring Boot flag:
java -jar /opt/hlabmonitor/hlabmonitor.jar --spring.config.location=file:/path/to/my-config.yaml
```

## Run as a systemd Service

### 1. Create a dedicated user

```bash
sudo useradd -r -M -s /bin/false hlabmonitor
sudo chown -R hlabmonitor:hlabmonitor /opt/hlabmonitor /etc/hlabmonitor /var/lib/hlabmonitor
```

### 2. Create the service unit

```bash
sudo nano /etc/systemd/system/hlabmonitor.service
```

```init
[Unit]
Description=HLabMonitor
After=network.target

[Service]
User=hlabmonitor
Group=hlabmonitor
WorkingDirectory=/opt/hlabmonitor
ExecStart=/usr/bin/java -jar /opt/hlabmonitor/hlabmonitor.jar
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

> `SuccessExitStatus=143` treats `SIGTERM` (graceful shutdown) as a clean exit.

### 3. Enable and start

```bash
sudo systemctl daemon-reload
sudo systemctl enable hlabmonitor
sudo systemctl start hlabmonitor
```

### 4. Check status and logs

```bash
sudo systemctl status hlabmonitor
sudo journalctl -u hlabmonitor -f
```

## Verify

```bash
curl http://localhost:8080/actuator/health
```

Expected: `{"status":"UP",...}`

## Configuration Priority

1. Environment variables
2. `HLABMONITOR_CONFIG_LOCATION` file
3. `/etc/hlabmonitor/application.yaml`
4. JAR defaults

See [Application YAML Reference](../configuration/application-yaml.md) for all options.
