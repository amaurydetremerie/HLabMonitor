# Metrics & Monitoring

HLabMonitor exposes comprehensive metrics about the application's performance, health, and monitoring activities through Spring Boot Actuator and Micrometer. These metrics can be consumed by monitoring systems like Prometheus and visualized with Grafana.

---

## Overview

HLabMonitor provides three categories of metrics:

1. **Application Metrics** - JVM, memory, CPU, threads
2. **Monitoring Metrics** - Check execution, success rates, latency
3. **Database Metrics** - Connection pools, query performance

All metrics are exposed via the `/actuator/metrics` endpoint and can be scraped by Prometheus at `/actuator/prometheus`.

---

## Enabling Metrics

### Default Configuration

Metrics are enabled by default. No configuration needed for basic usage.

### Expose Prometheus Endpoint

**`application.yaml`:**

``` yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Environment Variables:**

``` bash
MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
```

---

## Available Metrics

### Application Metrics

#### JVM Memory

| Metric | Description | Type |
|--------|-------------|------|
| `jvm.memory.used` | Memory currently used | Gauge |
| `jvm.memory.max` | Maximum memory available | Gauge |
| `jvm.memory.committed` | Memory committed to JVM | Gauge |
| `jvm.buffer.memory.used` | Buffer pool memory used | Gauge |

**Query Example:**

``` bash
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

**Response:**

``` json
{
  "name": "jvm.memory.used",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 268435456
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    },
    {
      "tag": "id",
      "values": ["G1 Eden Space", "G1 Old Gen", "G1 Survivor Space"]
    }
  ]
}
```

#### Garbage Collection

| Metric | Description | Type |
|--------|-------------|------|
| `jvm.gc.pause` | GC pause duration | Timer |
| `jvm.gc.memory.allocated` | Memory allocated after GC | Counter |
| `jvm.gc.memory.promoted` | Memory promoted to old gen | Counter |
| `jvm.gc.max.data.size` | Max old generation size | Gauge |

#### Threads

| Metric | Description | Type |
|--------|-------------|------|
| `jvm.threads.live` | Current live threads | Gauge |
| `jvm.threads.daemon` | Current daemon threads | Gauge |
| `jvm.threads.peak` | Peak thread count | Gauge |
| `jvm.threads.states` | Thread states breakdown | Gauge |

#### CPU & System

| Metric | Description | Type |
|--------|-------------|------|
| `system.cpu.usage` | System CPU usage (0.0-1.0) | Gauge |
| `process.cpu.usage` | Process CPU usage (0.0-1.0) | Gauge |
| `process.uptime` | Application uptime (seconds) | Gauge |
| `process.start.time` | Application start time (epoch) | Gauge |

### HTTP Metrics

| Metric | Description | Tags | Type |
|--------|-------------|------|------|
| `http.server.requests` | HTTP request count & latency | uri, method, status, outcome | Timer |
| `http.server.requests.active` | Active HTTP requests | - | Gauge |

**Tags:**
- `uri`: Request path (e.g., `/actuator/health`)
- `method`: HTTP method (GET, POST, etc.)
- `status`: HTTP status code (200, 404, 500, etc.)
- `outcome`: Request outcome (SUCCESS, CLIENT_ERROR, SERVER_ERROR)

**Query with tags:**

``` bash
curl "http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/actuator/health&tag=method:GET"
```

### Database Metrics

#### HikariCP Connection Pool (PostgreSQL/SQL Server)

| Metric | Description | Type |
|--------|-------------|------|
| `hikaricp.connections.active` | Active database connections | Gauge |
| `hikaricp.connections.idle` | Idle database connections | Gauge |
| `hikaricp.connections.pending` | Pending connection requests | Gauge |
| `hikaricp.connections.max` | Maximum pool size | Gauge |
| `hikaricp.connections.min` | Minimum pool size | Gauge |
| `hikaricp.connections.timeout` | Connection timeout count | Counter |
| `hikaricp.connections.creation` | Connection creation time | Timer |
| `hikaricp.connections.acquire` | Connection acquire time | Timer |
| `hikaricp.connections.usage` | Connection usage time | Timer |

> **Note**: HikariCP metrics are only available when using PostgreSQL or SQL Server. SQLite and H2 do not use connection pooling.

### Monitoring Check Metrics

HLabMonitor exposes custom metrics for monitoring activities:

| Metric | Description | Tags | Type |
|--------|-------------|------|------|
| `hlabmonitor.check.execution` | Check execution time | type, target, status | Timer |
| `hlabmonitor.check.count` | Check execution count | type, target, status | Counter |
| `hlabmonitor.ping.latency` | Ping response time | target | Gauge |
| `hlabmonitor.http.status` | HTTP response status | target, status_code | Gauge |
| `hlabmonitor.certificate.expiry` | Days until certificate expiry | target | Gauge |

**Tags:**
- `type`: Check type (`ping`, `http`, `certificate`)
- `target`: Target identifier (e.g., `gateway`, `website`)
- `status`: Check result (`success`, `failure`)
- `status_code`: HTTP status code (for HTTP checks)

**Example Queries:**

``` bash
# Get all ping check executions
curl http://localhost:8080/actuator/metrics/hlabmonitor.check.execution?tag=type:ping

# Get failed HTTP checks
curl http://localhost:8080/actuator/metrics/hlabmonitor.check.count?tag=type:http&tag=status:failure

# Certificate expiry for specific target
curl http://localhost:8080/actuator/metrics/hlabmonitor.certificate.expiry?tag=target:website
```

---

## Accessing Metrics

### List All Available Metrics

**Request:**

``` bash
curl http://localhost:8080/actuator/metrics
```

**Response:**

``` json
{
  "names": [
    "jvm.memory.used",
    "jvm.memory.max",
    "jvm.threads.live",
    "system.cpu.usage",
    "process.uptime",
    "http.server.requests",
    "hikaricp.connections.active",
    "hlabmonitor.check.execution",
    "hlabmonitor.ping.latency",
    ...
  ]
}
```

### Query Specific Metric

**Request:**

``` bash
curl http://localhost:8080/actuator/metrics/system.cpu.usage
```

**Response:**

``` json
{
  "name": "system.cpu.usage",
  "description": "The recent CPU usage of the system the application is running in",
  "baseUnit": "percent",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 0.25
    }
  ]
}
```

### Query Metric with Tags

**Request:**

``` bash
curl "http://localhost:8080/actuator/metrics/hlabmonitor.check.count?tag=type:ping&tag=status:success"
```

---

## Prometheus Integration

### Prometheus Endpoint

HLabMonitor exposes a Prometheus-compatible metrics endpoint at `/actuator/prometheus`.

**Request:**

``` bash
curl http://localhost:8080/actuator/prometheus
```

**Response (excerpt):**

``` prometheus
# HELP jvm_memory_used_bytes The amount of used memory
# TYPE jvm_memory_used_bytes gauge
jvm_memory_used_bytes{area="heap",id="G1 Eden Space",} 2.68435456E8
jvm_memory_used_bytes{area="nonheap",id="Metaspace",} 5.4321152E7

# HELP system_cpu_usage The recent CPU usage of the system
# TYPE system_cpu_usage gauge
system_cpu_usage 0.25

# HELP hlabmonitor_check_execution_seconds Check execution time
# TYPE hlabmonitor_check_execution_seconds summary
hlabmonitor_check_execution_seconds_count{target="gateway",type="ping",status="success",} 120.0
hlabmonitor_check_execution_seconds_sum{target="gateway",type="ping",status="success",} 0.456

# HELP hlabmonitor_ping_latency_milliseconds Ping response time
# TYPE hlabmonitor_ping_latency_milliseconds gauge
hlabmonitor_ping_latency_milliseconds{target="gateway",} 2.5
hlabmonitor_ping_latency_milliseconds{target="dns",} 8.3
```

### Prometheus Configuration

**`prometheus.yml`:**

``` yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'hlabmonitor'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['hlabmonitor:8080']
        labels:
          application: 'hlabmonitor'
          environment: 'production'
```

**Docker Compose Example:**

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    ports:
      - "8080:8080"
    environment:
      MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE: health,info,metrics,prometheus

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
```

**Verify scraping:**

``` bash
# Check Prometheus targets
open http://localhost:9090/targets

# Query metrics in Prometheus
http://localhost:9090/graph?g0.expr=hlabmonitor_ping_latency_milliseconds
```

---

## Grafana Dashboards

### Import HLabMonitor Dashboard

1. **Add Prometheus Data Source** in Grafana:
    - URL: `http://prometheus:9090`
    - Access: Server (default)

2. **Create Dashboard** with panels:

#### Panel 1: Application Health

**Query:**
``` promql
up{job="hlabmonitor"}
```

**Type:** Stat
**Thresholds:** 0 (red), 1 (green)

#### Panel 2: Memory Usage

**Query:**
``` promql
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}
```

**Type:** Gauge
**Unit:** Percent (0-1)

#### Panel 3: Check Success Rate

**Query:**
``` promql
rate(hlabmonitor_check_count_total{status="success"}[5m]) /
rate(hlabmonitor_check_count_total[5m])
```

**Type:** Graph
**Unit:** Percent (0-1)

#### Panel 4: Ping Latency

**Query:**
``` promql
hlabmonitor_ping_latency_milliseconds
```

**Type:** Graph
**Legend:** `{{target}}`
**Unit:** milliseconds

#### Panel 5: HTTP Check Status

**Query:**
``` promql
hlabmonitor_http_status{status_code=~"2.."}
```

**Type:** Table
**Transformation:** Group by target

#### Panel 6: Certificate Expiry

**Query:**
``` promql
hlabmonitor_certificate_expiry_days
```

**Type:** Stat
**Thresholds:** 30 (red), 60 (orange), 90 (green)

### Example Grafana Dashboard JSON

Save as `grafana-dashboard.json`:

``` json
{
  "dashboard": {
    "title": "HLabMonitor Overview",
    "panels": [
      {
        "id": 1,
        "title": "Ping Latency",
        "targets": [
          {
            "expr": "hlabmonitor_ping_latency_milliseconds",
            "legendFormat": "{{target}}"
          }
        ],
        "type": "graph"
      },
      {
        "id": 2,
        "title": "Memory Usage",
        "targets": [
          {
            "expr": "jvm_memory_used_bytes{area='heap'} / jvm_memory_max_bytes{area='heap'} * 100"
          }
        ],
        "type": "gauge"
      }
    ]
  }
}
```

Import via: **Dashboards → Import → Upload JSON file**

---

## Useful Prometheus Queries

### Application Performance

``` promql
# CPU usage
system_cpu_usage * 100

# Memory usage percentage
(jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}) * 100

# Thread count
jvm_threads_live

# Uptime in hours
process_uptime_seconds / 3600
```

### Monitoring Activity

``` promql
# Total checks per minute
rate(hlabmonitor_check_count_total[1m]) * 60

# Check success rate
sum(rate(hlabmonitor_check_count_total{status="success"}[5m])) /
sum(rate(hlabmonitor_check_count_total[5m]))

# Average ping latency
avg(hlabmonitor_ping_latency_milliseconds)

# Targets with high latency (>100ms)
hlabmonitor_ping_latency_milliseconds > 100

# Certificates expiring in 30 days
hlabmonitor_certificate_expiry_days < 30
```

### Database Performance

``` promql
# Active database connections
hikaricp_connections_active

# Connection pool usage
hikaricp_connections_active / hikaricp_connections_max

# Connection acquisition time
histogram_quantile(0.95, rate(hikaricp_connections_acquire_seconds_bucket[5m]))
```

### HTTP API Performance

``` promql
# Request rate
rate(http_server_requests_seconds_count[1m])

# Average response time
rate(http_server_requests_seconds_sum[5m]) /
rate(http_server_requests_seconds_count[5m])

# 95th percentile response time
histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))

# Error rate
rate(http_server_requests_seconds_count{outcome="SERVER_ERROR"}[5m])
```

---

## Custom Metrics (Development)

You can add custom business metrics in your code:

### Counter Example

``` java
@Component
public class MonitoringService {
    private final Counter checkCounter;

    public MonitoringService(MeterRegistry registry) {
        this.checkCounter = Counter.builder("hlabmonitor.custom.events")
                .description("Custom event counter")
                .tag("type", "business")
                .register(registry);
    }

    public void recordEvent() {
        checkCounter.increment();
    }
}
```

### Gauge Example

``` java
@Component
public class MonitoringMetrics {
    public MonitoringMetrics(MeterRegistry registry) {
        Gauge.builder("hlabmonitor.targets.count", this, MonitoringMetrics::getTargetCount)
                .description("Number of configured targets")
                .register(registry);
    }

    private int getTargetCount() {
        // Return actual count
        return 42;
    }
}
```

### Timer Example

``` java
@Component
public class CheckExecutor {
    private final Timer checkTimer;

    public CheckExecutor(MeterRegistry registry) {
        this.checkTimer = Timer.builder("hlabmonitor.check.custom")
                .description("Custom check execution time")
                .register(registry);
    }

    public void executeCheck() {
        checkTimer.record(() -> {
            // Your check logic
        });
    }
}
```

---

## Performance Considerations

### Metric Collection Overhead

- Metrics collection has minimal overhead (<1% CPU)
- Prometheus scraping is pull-based (no push overhead)
- Default scrape interval: 15 seconds

### Cardinality

Avoid high-cardinality tags (many unique values):

❌ **Bad** - User ID as tag (unbounded):
``` java
Counter.builder("requests")
.tag("user_id", userId)  // Thousands of unique values
.register(registry);
```

✅ **Good** - User type as tag (bounded):
``` java
Counter.builder("requests")
.tag("user_type", "admin")  // Few unique values
.register(registry);
```

### Retention

Configure Prometheus retention based on your needs:

``` yaml
# prometheus.yml
global:
  scrape_interval: 15s

storage:
  tsdb:
    retention.time: 30d    # Keep data for 30 days
    retention.size: 10GB   # Or limit by size
```

---

## Troubleshooting

### Metrics Not Appearing

1. **Verify endpoint is exposed:**

``` bash
curl http://localhost:8080/actuator | jq '.endpoints'
```

2. **Check Prometheus endpoint:**

``` bash
curl http://localhost:8080/actuator/prometheus | grep hlabmonitor
```

3. **Enable metrics export:**

``` yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
```

### High Memory Usage

Monitor JVM metrics:

``` bash
# Check heap usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used?tag=area:heap

# Check GC activity
curl http://localhost:8080/actuator/metrics/jvm.gc.pause
```

### Prometheus Not Scraping

1. **Check Prometheus logs:**

``` bash
docker logs prometheus
```

2. **Verify target in Prometheus UI:**

``` text
http://localhost:9090/targets
```

3. **Test endpoint manually:**

``` bash
curl http://hlabmonitor:8080/actuator/prometheus
```

---

## See Also

- [Logging](logs.md) - Application logging configuration
- [Alerting](alerting.md) - Alert rules and notifications
- [Production Checklist](../deployment/production-checklist.md)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Documentation](https://prometheus.io/docs/)
- [Grafana Dashboards](https://grafana.com/docs/grafana/latest/dashboards/)
