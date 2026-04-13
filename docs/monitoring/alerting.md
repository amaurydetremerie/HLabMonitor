# Alerting with Prometheus Alertmanager

HLabMonitor exposes metrics via `/actuator/prometheus`. You can use Prometheus + Alertmanager to route alerts to any channel (Slack, PagerDuty, etc.).

For HLabMonitor's **built-in notification system** (Discord, Email, Telegram, Log), see [Notifications](notifications.md).

---

## Available Metrics from HLabMonitor

The only notification-specific metrics exposed by HLabMonitor are:

| Metric | Type | Labels | Description |
|--------|------|--------|-------------|
| `notifications_total` | Counter | `status`, `target_id` | Notifications sent, by final status |
| `notifications_active` | Gauge | `id`, `status`, `target_id` | `1.0` = active (SEND), `0.0` = resolved |
| `notifications_age_seconds` | Gauge | `id`, `target_id` | Age of active notification in seconds |
| `notifications_resolution_seconds` | Gauge | `id`, `target_id` | Time from SEND to TERMINATED |

Standard Spring Boot / JVM / HikariCP metrics are also available (e.g. `jvm_memory_used_bytes`, `hikaricp_connections_active`, `http_server_requests_seconds`).

---

## Quick Setup (Docker Compose)

```yaml
services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    ports:
      - "8080:8080"

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./alerts.yml:/etc/prometheus/alerts.yml:ro

  alertmanager:
    image: prom/alertmanager:latest
    ports:
      - "9093:9093"
    volumes:
      - ./alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro
```

**`prometheus.yml`:**

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - alerts.yml

alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

scrape_configs:
  - job_name: hlabmonitor
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ['hlabmonitor:8080']
```

---

## Example Alert Rules

### Application Down

```yaml
groups:
  - name: hlabmonitor
    rules:
      - alert: HLabMonitorDown
        expr: up{job="hlabmonitor"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "HLabMonitor is down"
```

### Active Notifications (targets in failure)

```yaml
      - alert: ActiveNotifications
        expr: sum(notifications_active{status="SEND"}) > 0
        for: 0m
        labels:
          severity: warning
        annotations:
          summary: "{{ $value }} target(s) are currently in failure state"
```

### High JVM Memory

```yaml
      - alert: HighMemoryUsage
        expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "JVM heap above 90%"
```

### Slow API Responses

```yaml
      - alert: SlowAPIResponses
        expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "95th percentile API response time above 1s"
```

---

## Alertmanager Configuration Examples

### Email

```yaml
global:
  smtp_smarthost: 'smtp.example.com:587'
  smtp_from: 'alertmanager@example.com'
  smtp_auth_username: 'alertmanager@example.com'
  smtp_auth_password: 'password'

receivers:
  - name: email
    email_configs:
      - to: 'team@example.com'
```

### Slack

```yaml
receivers:
  - name: slack
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/WEBHOOK'
        channel: '#alerts'
        text: '{{ range .Alerts }}{{ .Annotations.summary }}{{ end }}'
```

### Discord (via webhook bridge)

```yaml
receivers:
  - name: discord
    webhook_configs:
      - url: 'https://discord.com/api/webhooks/YOUR_ID/YOUR_TOKEN/slack'
        send_resolved: true
```

---

## See Also

- [Notifications](notifications.md) — built-in Discord/Email/Telegram/Log
- [Metrics](metrics.md) — all available metrics
- [Prometheus Docs](https://prometheus.io/docs/alerting/latest/overview/)
