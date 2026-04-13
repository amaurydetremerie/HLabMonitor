# Application YAML Reference

This document covers all custom configuration properties for HLabMonitor. Standard Spring Boot properties (`server.port`, `spring.datasource`, etc.) are not repeated here unless they interact with HLabMonitor-specific behaviour.

## Configuration Locations and Priority

HLabMonitor applies properties in the following order (highest priority first):

1. Environment variables (e.g. `HLABMONITOR_DATABASE_TYPE=postgresql`)
2. File at `HLABMONITOR_CONFIG_LOCATION` environment variable
3. `/etc/hlabmonitor/application.yaml`
4. Defaults embedded in the JAR

---

## `database` Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `database.type` | enum | `h2` | `h2` \| `sqlite` \| `postgresql` \| `sqlserver` |
| `database.path` | string | platform-specific | SQLite file path. Linux: `/var/lib/hlabmonitor/monitor.db`; Windows: `%ProgramData%\hlabmonitor\monitor.db` |
| `database.host` | string | `localhost` | Server host (PostgreSQL, SQL Server) |
| `database.port` | integer | `5432` / `1433` | Auto-set per type if omitted |
| `database.name` | string | `monitor` | Database name |
| `database.username` | string | — | DB username |
| `database.password` | string | — | DB password |
| `database.debug` | boolean | `false` | Enable JDBC debug logging |
| `database.jdbc` | string | — | Custom JDBC URL — overrides all other `database.*` properties |

**H2 (default):** No configuration needed. Data is not persisted between restarts.

```yaml
database:
  type: sqlite
  path: /opt/hlabmonitor/monitor.db
```

```yaml
database:
  type: postgresql
  host: postgres.local
  port: 5432
  name: hlabmonitor
  username: monitor_user
  password: secret
```

---

## `monitoring` Configuration

Defines all targets to check. Each entry uses a user-chosen key.

**Generated target IDs:** `<key>:ping`, `<key>:http`, `<key>:certificate`
Example: key `my-server` → targets `my-server:ping`, `my-server:http`, `my-server:certificate`.

### `monitoring.ping`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `<key>.target` | string | required | IPv4, IPv6 address or hostname to ping |
| `<key>.interval` | duration | `5m` | Check interval. Suffixes: `s`, `m`, `h`, `d`, `w` |

```yaml
monitoring:
  ping:
    router:
      target: 192.168.1.1
      interval: 30s
    google-dns:
      target: 8.8.8.8
      # interval defaults to 5m
```

### `monitoring.http`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `<key>.target` | string | required | Hostname or URL. Protocol (`http://`/`https://`) is auto-prepended based on `ssl` |
| `<key>.interval` | duration | `10m` | Check interval |
| `<key>.ssl` | boolean | `true` | `true` = HTTPS, `false` = HTTP |
| `<key>.status-code` | integer | — | Acceptable HTTP status in addition to 2xx (e.g. `401`) |
| `<key>.internal` | boolean | `false` | Use internal RestClient; sets type to `HTTP_INTERNAL` |
| `<key>.certificate.verify` | boolean | `true` | Enable SSL certificate expiration monitoring |
| `<key>.certificate.interval` | duration | `1d` | Certificate check interval |

> **Default certificate behaviour:** When `ssl: true` (the default) and no `certificate` block is specified, certificate monitoring is **automatically created** with a 1-day interval. To disable it, set `certificate.verify: false` explicitly.

```yaml
monitoring:
  http:
    my-website:
      target: example.com        # → https://example.com
      interval: 5m
      ssl: true
      certificate:
        verify: true
        interval: 7d

    internal-api:
      target: my.internal.service
      interval: 1m
      ssl: false                 # → http://
      internal: true             # type = HTTP_INTERNAL

    protected-endpoint:
      target: api.example.com
      status-code: 401           # 401 is considered SUCCESS

    no-cert-monitoring:
      target: other.example.com
      ssl: true
      certificate:
        verify: false            # disable cert check despite ssl: true
```

### `monitoring.notification`

Global notification settings. Each channel can be toggled independently.

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Master switch for all notification channels |

#### `monitoring.notification.email`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable email notifications |
| `from` | string | **required** | Sender address — `NullPointerException` if absent when enabled |
| `to` | string | same as `from` | Recipient address |
| `firing` | boolean | `true` | Send on FIRING (result family → FAILURE) |
| `resolved` | boolean | `true` | Send on RESOLVED (result family → SUCCESS) |
| `failed` | boolean | `true` | Send if notification dispatch fails |
| `smtp.host` | string | `localhost` | SMTP server |
| `smtp.port` | integer | `587` | SMTP port |
| `smtp.username` | string | `""` | SMTP username |
| `smtp.password` | string | `""` | SMTP password |
| `smtp.auth` | boolean | `true` | Enable SMTP authentication |
| `smtp.tls` | boolean | `true` | Enable STARTTLS |

```yaml
monitoring:
  notification:
    email:
      enabled: true
      from: hlabmonitor@example.com
      to: alerts@example.com
      smtp:
        host: smtp.example.com
        port: 587
        username: hlabmonitor
        password: secret
        auth: true
        tls: true
```

#### `monitoring.notification.discord`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable Discord notifications |
| `webhook-url` | string | **required** | Discord webhook URL |
| `firing` | boolean | `true` | Send on FIRING |
| `resolved` | boolean | `true` | Send on RESOLVED |
| `failed` | boolean | `true` | Send on dispatch error |

```yaml
monitoring:
  notification:
    discord:
      enabled: true
      webhook-url: https://discord.com/api/webhooks/YOUR_ID/YOUR_TOKEN
```

#### `monitoring.notification.telegram`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable Telegram notifications |
| `token` | string | **required** | Bot API token |
| `chat-id` | string | **required** | Target chat ID (channel or group) |
| `firing` | boolean | `true` | Send on FIRING |
| `resolved` | boolean | `true` | Send on RESOLVED |
| `failed` | boolean | `true` | Send on dispatch error |

```yaml
monitoring:
  notification:
    telegram:
      enabled: true
      token: YOUR_BOT_TOKEN
      chat-id: "-123456789"
```

#### `monitoring.notification.log`

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable log notifications |
| `level` | LogLevel | **required** | `ERROR` \| `WARN` \| `INFO` \| `DEBUG` \| `TRACE` — must be set explicitly |
| `firing` | boolean | `true` | Log on FIRING |
| `resolved` | boolean | `true` | Log on RESOLVED |
| `failed` | boolean | `true` | Log on dispatch error |

> **Note:** Root logger level is `ERROR` by default. Set `logging.level.root: INFO` if log notifications at lower levels should appear in output.

```yaml
monitoring:
  notification:
    log:
      enabled: true
      level: INFO
```

---

## `debug` Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `debug.controller.enabled` | boolean | `false` | Expose debug REST endpoints at `/api/debug/execute/*` |

> **Warning:** Only enable in development or testing. The debug controller has no authentication.

```yaml
debug:
  controller:
    enabled: true
```

---

## Interval Format

All `interval` fields accept:

| Suffix | Unit | Example |
|--------|------|---------|
| `s` | seconds | `30s` |
| `m` | minutes | `5m` |
| `h` | hours | `2h` |
| `d` | days | `1d` |
| `w` | weeks | `2w` |

Invalid or empty values fall back to the type default (`5m` for ping, `10m` for HTTP, `1d` for certificate).

---

## Full Example

```yaml
database:
  type: postgresql
  host: postgres.local
  name: hlabmonitor
  username: monitor_user
  password: secret

monitoring:
  ping:
    router:
      target: 192.168.1.1
      interval: 30s

  http:
    my-site:
      target: example.com
      interval: 5m
      ssl: true
      certificate:
        verify: true
        interval: 1d
    internal:
      target: api.local
      ssl: false
      interval: 1m

  notification:
    email:
      enabled: true
      from: monitor@example.com
      smtp:
        host: smtp.example.com
        port: 587
        username: monitor
        password: secret
    discord:
      enabled: false
    telegram:
      enabled: false
    log:
      enabled: true
      level: INFO

debug:
  controller:
    enabled: false
```
