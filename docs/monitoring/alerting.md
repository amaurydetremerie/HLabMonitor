# Alerting

HLabMonitor integrates with Prometheus Alertmanager to provide flexible alerting on application health, monitoring check failures, and resource usage. Configure alerts to notify you via email, Slack, PagerDuty, or other channels when issues occur.

---

## Overview

Alerting allows you to:

- **Detect failures** - Be notified when monitoring checks fail
- **Track performance** - Alert on high latency or resource usage
- **Prevent incidents** - Early warning before problems escalate
- **Maintain SLAs** - Ensure uptime targets are met

---

## Architecture

``` text
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────┐
│  HLabMonitor │────▶│  Prometheus  │────▶│ Alertmanager │────▶│  Alerts  │
│  /actuator/  │     │   (scrape)   │     │   (route)    │     │ (notify) │
│  prometheus  │     │   (evaluate) │     │              │     │          │
└──────────────┘     └──────────────┘     └──────────────┘     └──────────┘
│
┌─────────────────────┼─────────────────────┐
│                     │                     │
▼                     ▼                     ▼
┌────────┐           ┌────────┐           ┌────────┐
│ Email  │           │ Slack  │           │Discord │
└────────┘           └────────┘           └────────┘
```

---

## Quick Start

### 1. Deploy Prometheus + Alertmanager

**`docker-compose.yml`:**

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - ./alerts.yml:/etc/prometheus/alerts.yml:ro
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'

  alertmanager:
    image: prom/alertmanager:latest
    ports:
      - "9093:9093"
    volumes:
      - ./alertmanager.yml:/etc/alertmanager/alertmanager.yml:ro
    command:
      - '--config.file=/etc/alertmanager/alertmanager.yml'
```

### 2. Configure Prometheus

**`prometheus.yml`:**

``` yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

# Load alert rules
rule_files:
  - 'alerts.yml'

# Alertmanager configuration
alerting:
  alertmanagers:
    - static_configs:
        - targets: ['alertmanager:9093']

# Scrape HLabMonitor
scrape_configs:
  - job_name: 'hlabmonitor'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['hlabmonitor:8080']
```

### 3. Define Alert Rules

**`alerts.yml`:**

``` yaml
groups:
  - name: hlabmonitor
    interval: 30s
    rules:
      # Application is down
      - alert: HLabMonitorDown
        expr: up{job="hlabmonitor"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "HLabMonitor is down"
          description: "HLabMonitor has been down for more than 1 minute"

      # Monitoring check failures
      - alert: MonitoringCheckFailed
        expr: rate(hlabmonitor_check_count_total{status="failure"}[5m]) > 0
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Monitoring check failures detected"
          description: "Target {{ $labels.target }} has failing checks"
```

### 4. Configure Alertmanager

**`alertmanager.yml`:**

``` yaml
global:
  resolve_timeout: 5m

route:
  group_by: ['alertname', 'severity']
  group_wait: 10s
  group_interval: 10s
  repeat_interval: 12h
  receiver: 'default'

receivers:
  - name: 'default'
    email_configs:
      - to: 'admin@example.com'
        from: 'alertmanager@example.com'
        smarthost: 'smtp.example.com:587'
        auth_username: 'alertmanager@example.com'
        auth_password: 'your-password'
```

### 5. Start Stack

``` bash
docker-compose up -d

# Verify
docker-compose ps
open http://localhost:9090  # Prometheus
open http://localhost:9093  # Alertmanager
```

---

## Alert Rules

### Application Health

#### HLabMonitor Down

``` yaml
- alert: HLabMonitorDown
  expr: up{job="hlabmonitor"} == 0
  for: 1m
  labels:
    severity: critical
    component: application
  annotations:
    summary: "HLabMonitor is down"
    description: "HLabMonitor instance {{ $labels.instance }} has been down for more than 1 minute"
    runbook_url: "https://docs.example.com/runbooks/hlabmonitor-down"
```

#### High Memory Usage

``` yaml
- alert: HighMemoryUsage
  expr: (jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) > 0.9
  for: 5m
  labels:
    severity: warning
    component: application
  annotations:
    summary: "High memory usage detected"
    description: "JVM heap usage is above 90% for more than 5 minutes"
```

#### High CPU Usage

``` yaml
- alert: HighCPUUsage
  expr: system_cpu_usage > 0.8
  for: 5m
  labels:
    severity: warning
    component: application
  annotations:
    summary: "High CPU usage"
    description: "CPU usage is above 80% for more than 5 minutes"
```

#### Application Restart

``` yaml
- alert: ApplicationRestarted
  expr: resets(process_uptime_seconds[5m]) > 0
  labels:
    severity: info
    component: application
  annotations:
    summary: "Application restarted"
    description: "HLabMonitor was restarted in the last 5 minutes"
```

### Monitoring Checks

#### Check Failures

``` yaml
- alert: MonitoringCheckFailed
  expr: rate(hlabmonitor_check_count_total{status="failure"}[5m]) > 0
  for: 5m
  labels:
    severity: warning
    component: monitoring
  annotations:
    summary: "Monitoring check failures"
    description: "Target {{ $labels.target }} ({{ $labels.type }}) has been failing for 5 minutes"
```

#### High Ping Latency

``` yaml
- alert: HighPingLatency
  expr: hlabmonitor_ping_latency_milliseconds > 100
  for: 5m
  labels:
    severity: warning
    component: monitoring
  annotations:
    summary: "High ping latency"
    description: "Ping latency for {{ $labels.target }} is {{ $value }}ms (threshold: 100ms)"
```

#### Target Unreachable

``` yaml
- alert: TargetUnreachable
  expr: hlabmonitor_check_count_total{status="success"} == 0
  for: 10m
  labels:
    severity: critical
    component: monitoring
  annotations:
    summary: "Target unreachable"
    description: "Target {{ $labels.target }} has been unreachable for 10 minutes"
```

#### HTTP Check Failure

``` yaml
- alert: HTTPCheckFailed
  expr: hlabmonitor_http_status{status_code!~"2.."} > 0
  for: 5m
  labels:
    severity: warning
    component: monitoring
  annotations:
    summary: "HTTP check failing"
    description: "HTTP check for {{ $labels.target }} returned {{ $labels.status_code }}"
```

#### Certificate Expiring Soon

``` yaml
- alert: CertificateExpiringSoon
  expr: hlabmonitor_certificate_expiry_days < 30
  labels:
    severity: warning
    component: monitoring
  annotations:
    summary: "SSL certificate expiring"
    description: "Certificate for {{ $labels.target }} expires in {{ $value }} days"
```

#### Certificate Expired

``` yaml
- alert: CertificateExpired
  expr: hlabmonitor_certificate_expiry_days < 0
  labels:
    severity: critical
    component: monitoring
  annotations:
    summary: "SSL certificate expired"
    description: "Certificate for {{ $labels.target }} has EXPIRED"
```

### Database

#### Database Connection Issues

``` yaml
- alert: DatabaseConnectionIssues
  expr: hikaricp_connections_timeout_total > 0
  for: 5m
  labels:
    severity: critical
    component: database
  annotations:
    summary: "Database connection timeouts"
    description: "Connection pool is experiencing timeouts"
```

#### High Database Connection Usage

``` yaml
- alert: HighDatabaseConnectionUsage
  expr: (hikaricp_connections_active / hikaricp_connections_max) > 0.8
  for: 5m
  labels:
    severity: warning
    component: database
  annotations:
    summary: "High database connection usage"
    description: "Connection pool usage is above 80%: {{ $value | humanizePercentage }}"
```

#### Slow Database Queries

``` yaml
- alert: SlowDatabaseQueries
  expr: histogram_quantile(0.95, rate(hikaricp_connections_usage_seconds_bucket[5m])) > 1
  for: 5m
  labels:
    severity: warning
    component: database
  annotations:
    summary: "Slow database queries"
    description: "95th percentile query time is above 1 second"
```

### HTTP API

#### High API Error Rate

``` yaml
- alert: HighAPIErrorRate
  expr: rate(http_server_requests_seconds_count{outcome="SERVER_ERROR"}[5m]) > 0.1
  for: 5m
  labels:
    severity: warning
    component: api
  annotations:
    summary: "High API error rate"
    description: "API error rate is above 10% for endpoint {{ $labels.uri }}"
```

#### Slow API Responses

``` yaml
- alert: SlowAPIResponses
  expr: histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m])) > 1
  for: 5m
  labels:
    severity: warning
    component: api
  annotations:
    summary: "Slow API responses"
    description: "95th percentile response time is {{ $value }}s for {{ $labels.uri }}"
```

---

## Alertmanager Configuration

### Email Notifications

``` yaml
global:
  smtp_smarthost: 'smtp.gmail.com:587'
  smtp_from: 'hlabmonitor@example.com'
  smtp_auth_username: 'hlabmonitor@example.com'
  smtp_auth_password: 'app-specific-password'

receivers:
  - name: 'email-team'
    email_configs:
      - to: 'team@example.com'
        headers:
          Subject: '[{{ .Status | toUpper }}] {{ .GroupLabels.alertname }}'
```

### Slack Notifications

``` yaml
receivers:
  - name: 'slack-alerts'
    slack_configs:
      - api_url: 'https://hooks.slack.com/services/YOUR/WEBHOOK/URL'
        channel: '#alerts'
        title: '{{ .GroupLabels.alertname }}'
        text: '{{ range .Alerts }}{{ .Annotations.description }}{{ end }}'
        color: '{{ if eq .Status "firing" }}danger{{ else }}good{{ end }}'
```

### Discord Notifications

``` yaml
receivers:
  - name: 'discord-alerts'
    webhook_configs:
      - url: 'https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN/slack'
        send_resolved: true
```

### PagerDuty Integration

``` yaml
receivers:
  - name: 'pagerduty-critical'
    pagerduty_configs:
      - service_key: 'YOUR_PAGERDUTY_SERVICE_KEY'
        severity: 'critical'
        description: '{{ .GroupLabels.alertname }}: {{ .CommonAnnotations.summary }}'
```

### Multiple Receivers

``` yaml
route:
  group_by: ['alertname', 'severity']
  receiver: 'default'
  routes:
    # Critical alerts to PagerDuty
    - match:
        severity: critical
      receiver: 'pagerduty-critical'
      continue: true

    # Warnings to Slack
    - match:
        severity: warning
      receiver: 'slack-alerts'
      continue: true

    # All alerts to email
    - receiver: 'email-team'

receivers:
  - name: 'default'
    email_configs:
      - to: 'admin@example.com'

  - name: 'pagerduty-critical'
    pagerduty_configs:
      - service_key: 'YOUR_KEY'

  - name: 'slack-alerts'
    slack_configs:
      - api_url: 'YOUR_WEBHOOK'
        channel: '#alerts'

  - name: 'email-team'
    email_configs:
      - to: 'team@example.com'
```

### Inhibition Rules

Prevent alert spam by inhibiting lower-severity alerts when critical ones fire:

``` yaml
inhibit_rules:
  # If target is down, inhibit all other alerts for that target
  - source_match:
      alertname: 'TargetUnreachable'
      severity: 'critical'
    target_match_re:
      target: '.*'
    equal: ['target']

  # If HLabMonitor is down, inhibit monitoring alerts
  - source_match:
      alertname: 'HLabMonitorDown'
    target_match:
      component: 'monitoring'
```

---

## Advanced Alert Rules

### SLO-Based Alerting

**99.9% availability SLO:**

``` yaml
- alert: SLOViolation
  expr: |
    (
      sum(rate(hlabmonitor_check_count_total{status="success"}[30d]))
      /
      sum(rate(hlabmonitor_check_count_total[30d]))
    ) < 0.999
  labels:
    severity: critical
    slo: "availability"
  annotations:
    summary: "SLO violation: Availability below 99.9%"
    description: "30-day availability is {{ $value | humanizePercentage }}"
```

### Alert Chaining

``` yaml
# Stage 1: Warning after 5 minutes
- alert: CheckFailureWarning
  expr: rate(hlabmonitor_check_count_total{status="failure"}[5m]) > 0
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Check failures detected (warning)"
    description: "Target {{ $labels.target }} failing for 5 minutes"

# Stage 2: Critical after 15 minutes
- alert: CheckFailureCritical
  expr: rate(hlabmonitor_check_count_total{status="failure"}[5m]) > 0
  for: 15m
  labels:
    severity: critical
  annotations:
    summary: "Check failures detected (CRITICAL)"
    description: "Target {{ $labels.target }} failing for 15 minutes"
```

### Rate of Change Alerts

``` yaml
- alert: SuddenLatencyIncrease
  expr: |
    (
      avg_over_time(hlabmonitor_ping_latency_milliseconds[5m])
      /
      avg_over_time(hlabmonitor_ping_latency_milliseconds[1h] offset 1h)
    ) > 2
  for: 5m
  labels:
    severity: warning
  annotations:
    summary: "Sudden latency increase"
    description: "Latency for {{ $labels.target }} increased by {{ $value }}x"
```

---

## Testing Alerts

### Manual Testing

#### Trigger Alert via API

``` bash
# Stop HLabMonitor to trigger HLabMonitorDown
docker-compose stop hlabmonitor

# Wait 1 minute, check Prometheus
open http://localhost:9090/alerts

# Check Alertmanager
open http://localhost:9093/#/alerts
```

#### Silence Alerts

``` bash
# Via Alertmanager UI
open http://localhost:9093/#/silences

# Via API
curl -X POST http://localhost:9093/api/v1/silences \
  -H "Content-Type: application/json" \
  -d '{
    "matchers": [{"name": "alertname", "value": "HLabMonitorDown"}],
    "startsAt": "2026-01-29T14:00:00Z",
    "endsAt": "2026-01-29T16:00:00Z",
    "createdBy": "admin",
    "comment": "Maintenance window"
  }'
```

### Load Testing

Generate failures to test alerts:

``` bash
# Add unreachable target
cat >> application.yaml <<EOF
monitoring:
  ping:
    test-unreachable:
      target: 192.0.2.1  # Reserved non-routable IP
      interval: 30s
EOF

# Restart and monitor
docker-compose restart hlabmonitor
docker-compose logs -f hlabmonitor
```

---

## Notification Templates

### Custom Email Template

``` yaml
receivers:
  - name: 'email-custom'
    email_configs:
      - to: 'team@example.com'
        html: |
          <h2>{{ .GroupLabels.alertname }}</h2>
          <p><strong>Status:</strong> {{ .Status }}</p>
          <p><strong>Severity:</strong> {{ .GroupLabels.severity }}</p>
          {{ range .Alerts }}
          <hr>
          <p>{{ .Annotations.description }}</p>
          <p><small>Started: {{ .StartsAt }}</small></p>
          {{ end }}
```

### Custom Slack Template

``` yaml
receivers:
  - name: 'slack-custom'
    slack_configs:
      - api_url: 'YOUR_WEBHOOK'
        channel: '#alerts'
        title: '{{ .GroupLabels.alertname }}'
        text: |
          {{ range .Alerts }}
          *{{ .Labels.severity | toUpper }}*: {{ .Annotations.summary }}
          {{ .Annotations.description }}
          {{ end }}
        color: '{{ if eq .Status "firing" }}danger{{ else }}good{{ end }}'
        actions:
          - type: button
            text: 'View in Prometheus'
            url: 'http://prometheus:9090/alerts'
          - type: button
            text: 'Silence'
            url: 'http://alertmanager:9093/#/silences'
```

---

## Troubleshooting

### Alerts Not Firing

1. **Check Prometheus rules:**

``` bash
# Verify rules loaded
open http://localhost:9090/rules

# Check evaluation
open http://localhost:9090/alerts
```

2. **Test PromQL expression:**

``` bash
# Execute query in Prometheus
open http://localhost:9090/graph
# Enter: up{job="hlabmonitor"} == 0
```

3. **Check Alertmanager connection:**

``` bash
# View Alertmanager status in Prometheus
open http://localhost:9090/status
```

### Alerts Not Sending

1. **Check Alertmanager logs:**

``` bash
docker-compose logs alertmanager
```

2. **Verify receiver configuration:**

``` yaml
# Test with simple webhook
receivers:
  - name: 'webhook-test'
    webhook_configs:
      - url: 'https://webhook.site/YOUR-UNIQUE-ID'
```

3. **Check routing:**

``` bash
# View Alertmanager routes
open http://localhost:9093/#/status
```

### Too Many Alerts

1. **Adjust thresholds:**

``` yaml
# Increase thresholds or duration
- alert: HighMemoryUsage
  expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) > 0.95  # Was 0.9
  for: 10m  # Was 5m
```

2. **Use inhibition rules**
3. **Group alerts properly**
4. **Silence known issues**

---

## Best Practices

### Alert Design

✅ **Do:**
- Alert on symptoms, not causes
- Use meaningful alert names
- Include actionable descriptions
- Set appropriate thresholds
- Test alerts regularly

❌ **Don't:**
- Alert on every metric
- Use vague descriptions
- Set thresholds too sensitive
- Ignore alert fatigue

### Alert Lifecycle

1. **Detection** - Alert fires when condition met
2. **Notification** - Alert routed to appropriate receiver
3. **Investigation** - Team investigates issue
4. **Resolution** - Issue resolved, alert clears
5. **Review** - Post-mortem, improve alerts

### On-Call Strategy

- **Tier 1**: Critical alerts → PagerDuty (24/7)
- **Tier 2**: Warnings → Slack (business hours)
- **Tier 3**: Info → Email (async)

---

## See Also

- [Metrics & Monitoring](metrics.md) - Available metrics
- [Logging](logs.md) - Log configuration
- [Production Checklist](../deployment/production-checklist.md)
- [Prometheus Alerting](https://prometheus.io/docs/alerting/latest/overview/)
- [Alertmanager Documentation](https://prometheus.io/docs/alerting/latest/alertmanager/)
