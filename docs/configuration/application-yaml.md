# Application YAML Reference

This document describes all custom configuration properties available in HLabMonitor's `application.yaml` file. Standard Spring Boot properties (like `server.port`, `spring.application.name`, etc.) are not covered here.

## Table of Contents

- [Database Configuration](#database-configuration)
- [Monitoring Configuration](#monitoring-configuration)
  - [Ping Monitoring](#ping-monitoring)
  - [HTTP Monitoring](#http-monitoring)
  - [SSL Certificate Monitoring](#ssl-certificate-monitoring)
- [Debug Configuration](#debug-configuration)

---

## Database Configuration

The `database` section configures the persistence layer for HLabMonitor. By default, an **H2 in-memory database** is used, but SQLite, PostgreSQL, and SQL Server are also supported.

### Properties

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `type` | enum | No | `h2` | Database type: `h2`, `sqlite`, `postgresql`, or `sqlserver` |
| `path` | string | No | Platform-specific | File path for SQLite database (see below for defaults) |
| `host` | string | No | `localhost` | Database server hostname (PostgreSQL, SQL Server) |
| `port` | integer | No | Type-specific | Database server port (5432 for PostgreSQL, 1433 for SQL Server) |
| `name` | string | No | `monitor` | Database name |
| `username` | string | No | - | Database username (PostgreSQL, SQL Server) |
| `password` | string | No | - | Database password (PostgreSQL, SQL Server) |

### Default SQLite Paths

- **Windows**: `C:\ProgramData\hlabmonitor\monitor.db`
- **Linux/Unix**: `/var/lib/hlabmonitor/monitor.db`

### Examples

#### H2 (Default)

No configuration needed. H2 is used automatically if no `database` section is provided.

``` yaml
# No database configuration = H2 in-memory
```

#### SQLite - Minimal

``` yaml
database:
  type: sqlite
```

#### SQLite - Custom Path

``` yaml
database:
  type: sqlite
  path: /opt/monitoring/data/monitor.db
```

#### PostgreSQL - Minimal

``` yaml
database:
  type: postgresql
  username: monitor
  password: secure_password
```

#### PostgreSQL - Full

``` yaml
database:
  type: postgresql
  host: db.example.com
  port: 5433
  name: production_db
  username: produser
  password: prodpass
```

#### SQL Server - Minimal

``` yaml
database:
  type: sqlserver
  username: monitor
  password: secure_password
```

#### SQL Server - Full

``` yaml
database:
  type: sqlserver
  host: sqlserver.example.com
  port: 14331
  name: MonitorDB
  username: dbadmin
  password: AdminPassword456
```

---

## Monitoring Configuration

The `monitoring` section defines all targets to monitor. Each target is identified by a unique key and can be either a **ping** check or an **HTTP** check.

### Ping Monitoring

Ping checks use ICMP to verify network connectivity to a target.

#### Properties

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `target` | string | Yes | - | IP address or hostname to ping |
| `interval` | duration | No | `5m` | Check interval (supports `s`, `m`, `h`, `d`, `w` suffixes) |

#### Example

``` yaml
monitoring:
  ping:
    gateway:
      target: 192.168.1.1
      interval: 1m

    dns-server:
      target: 8.8.8.8
      interval: 30s
    
    remote-host:
      target: example.com
      # Uses default interval of 5m
```

### HTTP Monitoring

HTTP checks verify web service availability and response.

#### Properties

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `target` | string | Yes | - | URL or hostname to check (protocol auto-detected based on `ssl`) |
| `interval` | duration | No | `10m` | Check interval (supports `s`, `m`, `h`, `d`, `w` suffixes) |
| `ssl` | boolean | No | `true` | Enable HTTPS (true) or HTTP (false) |
| `certificate` | object | No | - | SSL certificate monitoring configuration (see below) |

#### Example

``` yaml
monitoring:
  http:
    website:
      target: www.example.com
      interval: 5m
      ssl: true

    api:
      target: api.internal.local
      interval: 1m
      ssl: false
    
    service:
      target: service.example.com
      # Uses default interval of 10m
      # Uses default ssl: true
```

### SSL Certificate Monitoring

When `ssl: true` (default), you can optionally configure certificate expiration monitoring. This creates an additional **certificate check** for the target.

#### Certificate Properties

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `verify` | boolean | No | `true` | Enable certificate expiration monitoring |
| `interval` | duration | No | Inherits from parent HTTP check | Certificate check interval |

> **Note**: When `verify: true`, a separate certificate target is automatically created with the suffix `:certificate` (e.g., `website:certificate`).

#### Examples

##### HTTPS with Certificate Monitoring (Full Configuration)

``` yaml
monitoring:
  http:
    secure-site:
      target: www.example.com
      interval: 10m
      ssl: true
      certificate:
        verify: true
        interval: 1d
```

This creates:
- `secure-site` - HTTP check every 10 minutes
- `secure-site:certificate` - Certificate expiration check every 1 day

##### HTTPS with Certificate Monitoring (Minimal)

``` yaml
monitoring:
  http:
    secure-site:
      target: www.example.com
      certificate:
        verify: true
```

This creates:
- `secure-site` - HTTP check every 10 minutes (default)
- `secure-site:certificate` - Certificate check every 10 minutes (inherits from HTTP check)

##### HTTPS without Certificate Monitoring

``` yaml
monitoring:
  http:
    no-cert-check:
      target: www.example.com
      ssl: true
      certificate:
        verify: false
```

This creates only the HTTP check, no certificate monitoring.

##### HTTP (No SSL)

``` yaml
monitoring:
  http:
    plain-http:
      target: api.internal.local
      ssl: false
      certificate:
        verify: true  # Ignored - no certificate on HTTP
        interval: 1d
```

Certificate configuration is ignored when `ssl: false`.

### Complete Monitoring Example

``` yaml
monitoring:
  ping:
    router:
      target: 192.168.1.1
      interval: 30s

    public-dns:
      target: 1.1.1.1
      interval: 1m

  http:
    production-web:
      target: www.production.com
      interval: 5m
      ssl: true
      certificate:
        verify: true
        interval: 1d

    internal-api:
      target: api.internal.lan
      interval: 2m
      ssl: false
    
    monitoring-dashboard:
      target: grafana.local
      ssl: true
      # Certificate monitoring enabled with default interval
      certificate:
        verify: true
```

---

## Debug Configuration

### Debug Controller

The `debug.controller.enabled` property enables a REST controller for manually triggering monitoring checks (ping or HTTP).

#### Properties

| Property | Type | Required | Default | Description |
|----------|------|----------|---------|-------------|
| `debug.controller.enabled` | boolean | No | `false` | Enable debug REST endpoints for manual check execution |

#### Example

``` yaml
debug:
  controller:
    enabled: true
```

> **Warning**: This should only be enabled in development or testing environments, not in production.

---

## Interval Format

All `interval` properties support duration strings with the following suffixes:

| Suffix | Unit | Example | Duration |
|--------|------|---------|----------|
| `s` | Seconds | `30s` | 30 seconds |
| `m` | Minutes | `5m` | 5 minutes |
| `h` | Hours | `2h` | 2 hours |
| `d` | Days | `1d` | 1 day |
| `w` | Weeks | `2w` | 2 weeks |

If an invalid or empty interval is provided, the default for that check type is used (5m for ping, 10m for HTTP).

---

## Complete Example

``` yaml
database:
  type: postgresql
  host: postgres.local
  port: 5432
  name: hlabmonitor
  username: monitor_user
  password: secure_password

monitoring:
  ping:
    gateway:
      target: 192.168.1.1
      interval: 30s

    internet:
      target: 8.8.8.8
      interval: 1m

  http:
    website:
      target: www.example.com
      interval: 5m
      ssl: true
      certificate:
        verify: true
        interval: 1d

    api:
      target: api.internal.local
      interval: 2m
      ssl: false

debug:
  controller:
    enabled: false
```
