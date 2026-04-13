# Notification System

HLabMonitor includes a built-in notification system. Notifications fire when a monitored target's health **family** changes, and resolve when it returns to a healthy state.

## Notification Logic

### Result Families

| Family | Contains |
|--------|----------|
| SUCCESS | `SUCCESS` |
| FAILURE | `FAILURE`, `WARNING`, `ERROR` |
| UNKNOWN | `UNKNOWN` |

A notification is triggered when the family transitions (e.g. `SUCCESS` → `FAILURE`). It resolves when the family transitions back (e.g. `FAILURE` → `SUCCESS`). No notification is sent for intermediate changes within the same family.

### Status Lifecycle

```
Result changes family
        │
        ▼
  [TO_SEND] ──────────► [SEND]      ← FIRING notification sent, notification is now "active"
                             │
                   result returns to SUCCESS family
                             │
                             ▼
                      [TO_TERMINATE] ──► [TERMINATED]  ← RESOLVED notification sent
                      
  Any error during dispatch ──► [FAILED]
```

**Active notifications** = those with status `SEND`. Their count is available via `GET /api/v1/notifications/count`.

---

## Notification Channels

### Discord

Sends rich embed messages to a Discord channel via webhook.

**Messages sent:**
- `[FIRING]` — target entered FAILURE family (orange embed, color `11550002`)
- `[RESOLVED]` — target returned to SUCCESS family (green embed, color `6729778`)
- `[FAILED]` — notification dispatch error (dark red embed, color `3319216`)

**Configuration (`monitoring.notification.discord`):**

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
      webhook-url: https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_TOKEN
      firing: true
      resolved: true
      failed: true
```

---

### Email

Sends HTML emails via SMTP using Spring's `JavaMailSender`.

**Subject lines:**
- `[FIRING] HLab Monitor — Target <id>`
- `[RESOLVED] HLab Monitor — Target <id>`
- `[FAILED] HLab Monitor — Target <id>`

**Configuration (`monitoring.notification.email`):**

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable email notifications |
| `from` | string | **required** | Sender address — startup will fail with `NullPointerException` if absent |
| `to` | string | same as `from` | Recipient address |
| `firing` | boolean | `true` | Send on FIRING |
| `resolved` | boolean | `true` | Send on RESOLVED |
| `failed` | boolean | `true` | Send on dispatch error |
| `smtp.host` | string | `localhost` | SMTP server hostname |
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

---

### Telegram

Sends HTML-formatted messages via the Telegram Bot API (`https://api.telegram.org/bot{token}/sendMessage`).

**Message format:**
- `🔴 [FIRING] — HLab Monitor`
- `🟢 [RESOLVED] — HLab Monitor`
- `⚠️ [FAILED] — HLab Monitor`

**Configuration (`monitoring.notification.telegram`):**

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable Telegram notifications |
| `token` | string | **required** | Telegram bot token |
| `chat-id` | string | **required** | Target chat ID (group or channel) |
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

To obtain a chat ID, send a message to your bot and call `https://api.telegram.org/bot{token}/getUpdates`.

---

### Log

Writes notification events to the application's SLF4J logger. Useful for debugging or integration with centralized logging.

**Log messages:**
- FIRING: `Notification for target {} has been fired`
- RESOLVED: `Notification for target {} has been cleared`
- FAILED: `An error occurred while sending notification for target {}`

**Configuration (`monitoring.notification.log`):**

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | boolean | `true` | Enable log notifications |
| `level` | LogLevel | **required** | `ERROR` \| `WARN` \| `INFO` \| `DEBUG` \| `TRACE` |
| `firing` | boolean | `true` | Log on FIRING |
| `resolved` | boolean | `true` | Log on RESOLVED |
| `failed` | boolean | `true` | Log on dispatch error |

> **Important:** The root logger level defaults to `ERROR`. If you configure `level: INFO` or lower, also set `logging.level.root: INFO` (or target the specific logger) to see the output.

```yaml
monitoring:
  notification:
    log:
      enabled: true
      level: INFO

logging:
  level:
    root: INFO
```

---

### Prometheus Metrics

HLabMonitor exposes notification metrics via `/actuator/prometheus`. These are the only notification-related metrics in the codebase:

| Metric | Type | Labels | Description |
|--------|------|--------|-------------|
| `notifications_total` | Counter | `status`, `target_id` | Total notifications sent, by status and target |
| `notifications_active` | Gauge | `id`, `status`, `target_id` | `1.0` if notification is active (SEND), `0.0` otherwise |
| `notifications_age_seconds` | Gauge | `id`, `target_id` | Seconds since the notification fired (FIRING only) |
| `notifications_resolution_seconds` | Gauge | `id`, `target_id` | Seconds from SEND to TERMINATED (after resolution) |

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus,health,metrics
```

Example PromQL queries:

```promql
# Currently active alerts
sum(notifications_active) by (target_id)

# Total FIRING notifications
notifications_total{status="SEND"}

# Average resolution time
avg(notifications_resolution_seconds) by (target_id)
```

---

### Server-Sent Events (SSE)

Subscribe to live notification count updates without polling.

**Endpoint:** `GET /api/v1/notifications/stream`  
**Content-Type:** `text/event-stream`  
**Event name:** `notifications-count-update`  
**Timeout:** infinite

The server pushes the current active notification count whenever it changes (on each new or resolved notification).

```javascript
const source = new EventSource('/api/v1/notifications/stream');

source.addEventListener('notifications-count-update', (event) => {
  document.getElementById('badge').textContent = event.data;
});

source.onerror = () => source.close();
```

---

## Disabling Notifications

To disable all notifications globally:

```yaml
monitoring:
  notification:
    enabled: false
```

To disable a specific channel while keeping others active:

```yaml
monitoring:
  notification:
    discord:
      enabled: false
    email:
      enabled: true
      from: monitor@example.com
      smtp:
        host: smtp.example.com
```
