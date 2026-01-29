# Logging

HLabMonitor uses Spring Boot's default logging framework (Logback) to provide comprehensive application logging. Logs help you understand application behavior, troubleshoot issues, and audit monitoring activities.

---

## Overview

HLabMonitor logs provide insights into:

- **Application Lifecycle** - Startup, shutdown, configuration loading
- **Monitoring Activity** - Check executions, results, errors
- **Database Operations** - Connections, queries, migrations
- **HTTP Requests** - API calls, response times, errors
- **Security Events** - Authentication, authorization (if configured)

---

## Log Levels

HLabMonitor supports standard log levels:

| Level | Description | Use Case |
|-------|-------------|----------|
| `TRACE` | Very detailed information | Deep debugging only |
| `DEBUG` | Detailed debugging information | Development, troubleshooting |
| `INFO` | General informational messages | Default production level |
| `WARN` | Warning messages (non-critical issues) | Potential problems |
| `ERROR` | Error messages (failures) | Critical issues requiring attention |

**Default Level:** `INFO`

---

## Configuration

### Log Level Configuration

#### Via application.yaml

**`application.yaml`:**

``` yaml
logging:
  level:
    root: INFO
    be.wiserisk.hlabmonitor: DEBUG
    org.springframework.web: INFO
    org.springframework.jdbc: WARN
    org.hibernate: WARN
```

#### Via Environment Variables

``` bash
# Root level
LOGGING_LEVEL_ROOT=INFO

# HLabMonitor package level
LOGGING_LEVEL_BE_WISERISK_HLABMONITOR=DEBUG

# Spring Framework levels
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=INFO
LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_JDBC=WARN
```

#### Via JVM Arguments

``` bash
java -jar hlabmonitor.jar \
  --logging.level.root=INFO \
  --logging.level.be.wiserisk.hlabmonitor=DEBUG
```

### Common Configurations

#### Development (Verbose Logging)

``` yaml
logging:
  level:
    root: INFO
    be.wiserisk.hlabmonitor: DEBUG
    org.springframework.web: DEBUG
    org.springframework.jdbc: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### Production (Minimal Logging)

``` yaml
logging:
  level:
    root: INFO
    be.wiserisk.hlabmonitor: INFO
    org.springframework.web: WARN
    org.springframework.jdbc: WARN
    org.hibernate: WARN
```

#### Troubleshooting (Maximum Detail)

``` yaml
logging:
  level:
    root: DEBUG
    be.wiserisk.hlabmonitor: TRACE
    org.springframework: DEBUG
```

---

## Log Output

### Console Output (Default)

By default, logs are written to the console (stdout):

``` text
2026-01-29 14:30:15.123  INFO 1 --- [main] b.w.h.HLabMonitorApplication : Starting HLabMonitorApplication v1.0.0
2026-01-29 14:30:15.456  INFO 1 --- [main] b.w.h.config.DatabaseConfig  : Configuring SQLite database at /var/lib/hlabmonitor/monitor.db
2026-01-29 14:30:16.789  INFO 1 --- [main] b.w.h.monitoring.PingMonitor : Scheduled ping check for target 'gateway' every 60 seconds
2026-01-29 14:30:17.012  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port 8080
2026-01-29 14:30:17.123  INFO 1 --- [main] b.w.h.HLabMonitorApplication : Started HLabMonitorApplication in 2.456 seconds
```

### File Output

#### Configure File Logging

**`application.yaml`:**

``` yaml
logging:
  file:
    name: /var/log/hlabmonitor/application.log
    max-size: 10MB
    max-history: 30
    total-size-cap: 1GB
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**Environment Variables:**

``` bash
LOGGING_FILE_NAME=/var/log/hlabmonitor/application.log
LOGGING_FILE_MAX_SIZE=10MB
LOGGING_FILE_MAX_HISTORY=30
```

#### Log Rotation

Logback automatically rotates logs based on:

- **Size**: When file reaches `max-size` (default: 10MB)
- **Daily**: Creates new file each day
- **History**: Keeps `max-history` files (default: 7 days)
- **Total Cap**: Removes oldest when `total-size-cap` is reached

**Rotated files:**

``` text
/var/log/hlabmonitor/
├── application.log              # Current log
├── application.log.2026-01-28   # Yesterday
├── application.log.2026-01-27   # 2 days ago
└── application.log.2026-01-26   # 3 days ago
```

---

## Docker Logging

### View Container Logs

``` bash
# Follow logs
docker logs -f hlabmonitor

# Last 100 lines
docker logs --tail 100 hlabmonitor

# Logs with timestamps
docker logs -t hlabmonitor

# Logs since 1 hour ago
docker logs --since 1h hlabmonitor
```

### Docker Compose Logs

``` bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f hlabmonitor

# With timestamps and tail
docker-compose logs -f --tail=100 --timestamps hlabmonitor
```

### Log to File (Docker Volume)

**`docker-compose.yml`:**

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    environment:
      LOGGING_FILE_NAME: /var/log/hlabmonitor/application.log
    volumes:
      - hlabmonitor-logs:/var/log/hlabmonitor
    restart: unless-stopped

volumes:
  hlabmonitor-logs:
```

**Access logs from host:**

``` bash
# Copy logs from volume
docker cp hlabmonitor:/var/log/hlabmonitor/application.log ./

# Or use a bind mount
docker run -v ./logs:/var/log/hlabmonitor wiserisk/hlabmonitor:latest
```

### Docker Logging Driver

Configure logging driver in `docker-compose.yml`:

``` yaml
services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
        labels: "production,hlabmonitor"
```

**Available drivers:**
- `json-file` - Default, JSON format
- `syslog` - Send to syslog
- `journald` - systemd journal
- `gelf` - Graylog Extended Log Format
- `fluentd` - Fluentd aggregator

---

## Kubernetes Logging

### View Pod Logs

``` bash
# Current logs
kubectl -n hlabmonitor logs hlabmonitor-pod-name

# Follow logs
kubectl -n hlabmonitor logs -f hlabmonitor-pod-name

# Previous container logs (after crash)
kubectl -n hlabmonitor logs --previous hlabmonitor-pod-name

# Multiple pods (deployment)
kubectl -n hlabmonitor logs -l app=hlabmonitor -f

# Logs from specific container (multi-container pod)
kubectl -n hlabmonitor logs hlabmonitor-pod-name -c hlabmonitor
```

### Persistent Logs in Kubernetes

**Deployment with log volume:**

``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
  namespace: hlabmonitor
spec:
  template:
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:latest
        env:
        - name: LOGGING_FILE_NAME
          value: /var/log/hlabmonitor/application.log
        volumeMounts:
        - name: logs
          mountPath: /var/log/hlabmonitor
      volumes:
      - name: logs
        persistentVolumeClaim:
          claimName: hlabmonitor-logs-pvc
```

### Centralized Logging

Use a log aggregation solution:

#### Option 1: Loki + Promtail

**Promtail DaemonSet** (collects logs from all pods):

``` yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: promtail-config
  namespace: logging
data:
  promtail.yaml: |
    server:
      http_listen_port: 9080
    
    clients:
      - url: http://loki:3100/loki/api/v1/push
    
    scrape_configs:
      - job_name: kubernetes-pods
        kubernetes_sd_configs:
          - role: pod
        relabel_configs:
          - source_labels: [__meta_kubernetes_namespace]
            target_label: namespace
          - source_labels: [__meta_kubernetes_pod_name]
            target_label: pod
          - source_labels: [__meta_kubernetes_pod_label_app]
            target_label: app
```

#### Option 2: ELK Stack (Elasticsearch, Logstash, Kibana)

**Filebeat DaemonSet** (ships logs to Elasticsearch):

``` yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: filebeat-config
  namespace: logging
data:
  filebeat.yml: |
    filebeat.inputs:
    - type: container
      paths:
        - /var/log/containers/*hlabmonitor*.log
    
    output.elasticsearch:
      hosts: ["elasticsearch:9200"]
      index: "hlabmonitor-%{+yyyy.MM.dd}"
```

---

## Log Format

### Default Format (Console)

``` text
[Date] [Level] [Thread] [Logger] - [Message]

2026-01-29 14:30:15.123  INFO 1 --- [scheduling-1] b.w.h.m.PingCheckExecutor : Executing ping check for target 'gateway'
```

### Custom Format

**`application.yaml`:**

``` yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**Pattern elements:**
- `%d{...}` - Date/time format
- `%thread` - Thread name
- `%-5level` - Log level (left-aligned, 5 chars)
- `%logger{36}` - Logger name (max 36 chars)
- `%msg` - Log message
- `%n` - Line separator

### Structured Logging (JSON)

For log aggregation, use JSON format:

**`logback-spring.xml`:**

``` xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeMdcKeyName>traceId</includeMdcKeyName>
            <includeMdcKeyName>spanId</includeMdcKeyName>
            <fieldNames>
                <timestamp>timestamp</timestamp>
                <message>message</message>
                <logger>logger</logger>
                <thread>thread</thread>
                <level>level</level>
            </fieldNames>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="JSON_CONSOLE"/>
    </root>
</configuration>
```

**JSON output:**

``` json
{
  "timestamp": "2026-01-29T14:30:15.123Z",
  "level": "INFO",
  "thread": "scheduling-1",
  "logger": "be.wiserisk.hlabmonitor.monitoring.PingCheckExecutor",
  "message": "Executing ping check for target 'gateway'",
  "application": "hlabmonitor",
  "environment": "production"
}
```

---

## Important Log Messages

### Startup Logs

``` text
INFO  b.w.h.HLabMonitorApplication : Starting HLabMonitorApplication v1.0.0
INFO  b.w.h.config.DatabaseConfig  : Configuring PostgreSQL database
INFO  liquibase.changelog           : Reading from monitor.databasechangelog
INFO  liquibase.changelog           : ChangeSet changelog.xml::1 ran successfully
INFO  b.w.h.monitoring.MonitoringScheduler : Scheduling 5 monitoring targets
INFO  o.s.b.w.embedded.tomcat       : Tomcat started on port 8080
INFO  b.w.h.HLabMonitorApplication : Started HLabMonitorApplication in 3.456 seconds
```

### Monitoring Activity

``` text
# Successful check
INFO  b.w.h.m.PingCheckExecutor : Ping check succeeded for 'gateway': 2.5ms

# Failed check
WARN  b.w.h.m.PingCheckExecutor : Ping check failed for 'gateway': Host unreachable

# HTTP check
INFO  b.w.h.m.HttpCheckExecutor : HTTP check for 'website' returned 200 OK (234ms)

# Certificate check
WARN  b.w.h.m.CertCheckExecutor : Certificate for 'website' expires in 25 days
```

### Database Events

``` text
INFO  com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Starting...
INFO  com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Start completed
DEBUG org.hibernate.SQL : select * from monitoring_targets
WARN  o.h.engine.jdbc.spi.SqlExceptionHelper : Connection timeout
```

### Errors

``` text
ERROR b.w.h.m.PingCheckExecutor : Failed to execute ping check
java.net.UnknownHostException: invalid-host.local
    at java.base/java.net.InetAddress.getAllByName0(InetAddress.java:...)
    at be.wiserisk.hlabmonitor.monitoring.PingCheckExecutor.execute(...)
```

---

## Log Analysis

### Search Logs

``` bash
# Search for errors
docker logs hlabmonitor 2>&1 | grep ERROR

# Search for specific target
docker logs hlabmonitor 2>&1 | grep "gateway"

# Count error occurrences
docker logs hlabmonitor 2>&1 | grep -c ERROR

# Find database connection issues
kubectl logs -l app=hlabmonitor | grep -i "connection"
```

### Parse JSON Logs

``` bash
# Extract error messages (assuming JSON logging)
docker logs hlabmonitor 2>&1 | jq 'select(.level=="ERROR") | .message'

# Count errors by logger
docker logs hlabmonitor 2>&1 | jq -r 'select(.level=="ERROR") | .logger' | sort | uniq -c

# Find slow requests (>1s)
docker logs hlabmonitor 2>&1 | jq 'select(.duration > 1000)'
```

### Export Logs

``` bash
# Export to file
docker logs hlabmonitor > hlabmonitor.log 2>&1

# Export with timestamps
docker logs -t hlabmonitor > hlabmonitor-$(date +%Y%m%d).log 2>&1

# Export from Kubernetes
kubectl -n hlabmonitor logs hlabmonitor-pod-name > pod-logs.txt
```

---

## Integration with Log Aggregation

### Grafana Loki

**Docker Compose with Loki:**

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    logging:
      driver: loki
      options:
        loki-url: "http://loki:3100/loki/api/v1/push"
        loki-retries: "5"
        loki-batch-size: "400"
        labels: "app=hlabmonitor,environment=production"

  loki:
    image: grafana/loki:latest
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
```

**Query logs in Grafana:**

``` logql
# All HLabMonitor logs
{app="hlabmonitor"}

# Only errors
{app="hlabmonitor"} |= "ERROR"

# Specific target
{app="hlabmonitor"} |= "gateway"

# Failed checks
{app="hlabmonitor"} |= "check failed"
```

### Elasticsearch (ELK)

**Logstash configuration:**

``` ruby
input {
  tcp {
    port => 5000
    codec => json
  }
}

filter {
  if [application] == "hlabmonitor" {
    mutate {
      add_field => { "[@metadata][index]" => "hlabmonitor" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "%{[@metadata][index]}-%{+YYYY.MM.dd}"
  }
}
```

**Kibana queries:**

``` text
# All errors
level: ERROR

# Failed ping checks
logger: *PingCheckExecutor AND message: *failed*

# Slow database queries
duration: >1000 AND logger: *hibernate*
```

---

## Troubleshooting

### No Logs Appearing

1. **Check log level:**

``` bash
# Too restrictive level might hide logs
curl http://localhost:8080/actuator/loggers/be.wiserisk.hlabmonitor
```

2. **Verify logging configuration:**

``` bash
# Check effective configuration
curl http://localhost:8080/actuator/configprops | jq '.logging'
```

3. **Enable debug logging:**

``` bash
# Temporarily enable debug
curl -X POST http://localhost:8080/actuator/loggers/be.wiserisk.hlabmonitor \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

### Logs Too Verbose

Reduce log levels for noisy packages:

``` yaml
logging:
  level:
    org.springframework.web: WARN
    org.hibernate: WARN
    com.zaxxer.hikari: WARN
```

### Log Files Growing Too Large

1. **Check rotation settings:**

``` yaml
logging:
  file:
    max-size: 10MB      # Smaller files
    max-history: 7      # Keep fewer days
    total-size-cap: 1GB # Lower total cap
```

2. **Reduce log level:**

``` yaml
logging:
  level:
    root: WARN  # Only warnings and errors
```

3. **Clean old logs manually:**

``` bash
find /var/log/hlabmonitor -name "*.log.*" -mtime +7 -delete
```

---

## Best Practices

### Production Logging

✅ **Do:**
- Use `INFO` level by default
- Enable `DEBUG` only for troubleshooting
- Configure log rotation
- Use structured logging (JSON) for aggregation
- Include context (target, check type) in log messages
- Monitor log volume

❌ **Don't:**
- Use `TRACE` or `DEBUG` in production
- Log sensitive data (passwords, tokens)
- Disable logging entirely
- Ignore disk space for log files

### Performance

- Log aggregation has minimal impact (<2% CPU)
- Async appenders improve performance
- Avoid logging in tight loops
- Use appropriate log levels

### Security

Never log:
- Passwords or credentials
- API keys or tokens
- Personal identifiable information (PII)
- Full stack traces with sensitive data

---

## See Also

- [Metrics & Monitoring](metrics.md) - Application metrics
- [Alerting](alerting.md) - Alert configuration
- [Troubleshooting](../troubleshooting/common-issues.md)
- [Logback Documentation](http://logback.qos.ch/documentation.html)
- [Spring Boot Logging](https://docs.spring.io/spring-boot/reference/features/logging.html)
