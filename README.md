# HLabMonitor

> Monitoring solution for your HomeLab

[![Docker Image](https://img.shields.io/docker/v/wiserisk/hlabmonitor?label=Docker)](https://hub.docker.com/r/wiserisk/hlabmonitor)
[![Latest Release](https://img.shields.io/github/v/release/amaurydetremerie/HLabMonitor)](https://github.com/amaurydetremerie/HLabMonitor/releases)
[![License](https://img.shields.io/github/license/amaurydetremerie/HLabMonitor)](LICENSE)

HLabMonitor is a Spring Boot application (v4.0.1, Java 21) designed to monitor your HomeLab infrastructure. It performs periodic checks on network targets, tracks results, and sends notifications when service health changes.

## Features

- **PING (ICMP)** — IPv4 and IPv6, 5s timeout
- **HTTP/HTTPS GET** — status code verification (2xx or custom), internal RestClient mode
- **SSL Certificate monitoring** — X.509 expiration checks, auto-created for every HTTPS target
- **Built-in notifications** — Discord, Email (SMTP), Telegram, Log, Prometheus metrics, SSE stream
- **REST API** — full CRUD for targets, paginated results, live notification stream
- **Multi-database** — H2 (default, in-memory), SQLite, PostgreSQL, SQL Server via Liquibase
- **Multiple deployment options** — Docker, DEB/RPM packages, standalone JAR

> **SPEEDTEST** is declared in the codebase but not yet implemented (`UnsupportedOperationException`).

## Quick Start

```bash
docker run -d \
  --name hlabmonitor \
  -p 8080:8080 \
  -v hlabmonitor-data:/etc/hlabmonitor \
  wiserisk/hlabmonitor:latest
```

HLabMonitor starts with an H2 in-memory database (data lost on restart). Place an `application.yaml` in the mounted volume to configure monitoring targets and notifications.

> **Note:** Swagger UI and API docs are **disabled by default**. Enable them with `springdoc.swagger-ui.enabled: true` and `springdoc.api-docs.enabled: true`.

> **Security:** The REST API has **no authentication** by default. Restrict network access appropriately.

## Installation Options

| Method | Best For | Documentation |
|--------|----------|---------------|
| Docker | Containers, quick start | [Docker Guide](docs/installation/docker.md) |
| DEB Package | Debian/Ubuntu | [Debian/Ubuntu Guide](docs/installation/deb.md) |
| RPM Package | RHEL/Fedora/Rocky | [RHEL/Fedora Guide](docs/installation/rpm.md) |
| Standalone JAR | Any platform (Java 21+) | [JAR Guide](docs/installation/standalone.md) |

## Documentation

### Configuration
- [Application YAML Reference](docs/configuration/application-yaml.md) — all config properties
- [Database Setup](docs/configuration/database.md) — PostgreSQL, SQL Server, SQLite

### Monitoring & Notifications
- [Notification System](docs/monitoring/notifications.md) — Discord, Email, Telegram, Log, SSE, Prometheus

### Deployment
- [Docker Compose Examples](docs/deployment/docker-compose.md)
- [Kubernetes Manifests](docs/deployment/kubernetes-manifests.md)
- [Production Checklist](docs/deployment/production-checklist.md)

### Development
- [Architecture Overview](docs/development/architecture.md)
- [Building from Source](docs/development/building.md)
- [Contributing Guide](docs/development/contributing.md)

### Help
- [Troubleshooting](docs/troubleshooting/common-issues.md)
- [FAQ](docs/troubleshooting/faq.md)

## API Endpoints

**Base URL:** `http://localhost:8080/api/v1`

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/targets` | All target IDs |
| `GET` | `/targets/{monitoringType}` | Target IDs by type (`PING`, `HTTP`, `CERTIFICATE`, `HTTP_INTERNAL`) |
| `GET` | `/results` | All results |
| `GET` | `/results/{targetId}` | Results for one target (404 if unknown) |
| `GET` | `/results/search` | Paginated filtered results (`from`, `to`, `targetIdList`, `monitoringResultList`, `monitoringTypeList`, `size`=20, `page`=0) |
| `GET` | `/notifications` | Active notifications (status `SEND`) |
| `GET` | `/notifications/count` | Count of active notifications |
| `GET` | `/notifications/stream` | SSE stream — event: `notifications-count-update` |
| `GET` | `/management/stats` | Statistics (optional `statisticTypes[]`). Note: `NOTIFICATION_SEND` and `NOTIFICATION_TRIGGER` return `-1L` (not yet implemented) |
| `POST` | `/management` | Add a target (body: `Target` JSON) → 202 |
| `PUT` | `/management/` | Update a target (body: `Target` JSON) → 202 |
| `POST` | `/management/{targetId}/stop` | Stop monitoring for a target → 202 |
| `POST` | `/management/{targetId}/resume` | Resume monitoring for a target → 202 |
| `POST` | `/management/reload` | Reload all targets from config → 202 |

**Debug endpoints** (requires `debug.controller.enabled: true`, base path `/api/debug`):

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/debug/execute/{targetId}` | Manually trigger a check |
| `POST` | `/api/debug/execute/{notificationId}` | Delete a notification by ID (Long) |

**Actuator** (always available):

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Liveness + readiness probes |
| `/actuator/prometheus` | Prometheus metrics scrape |
| `/actuator/metrics` | Spring metrics |

## Docker Images

```bash
docker pull wiserisk/hlabmonitor:latest           # Ubuntu/Temurin (default)
docker pull wiserisk/hlabmonitor:latest-alpine    # Alpine (smaller)
docker pull wiserisk/hlabmonitor:latest-corretto  # Amazon Corretto
```

## Configuration Priority

1. Environment variables (highest)
2. File at `HLABMONITOR_CONFIG_LOCATION`
3. `/etc/hlabmonitor/application.yaml`
4. Embedded JAR defaults (lowest)

## Requirements

- **Java 21+** (for JAR deployments — Docker images include the JVM)
- **Database** (optional): PostgreSQL, SQL Server, or SQLite — H2 in-memory is the default

## License

GNU Affero General Public License v3.0 (AGPL-3.0). See [LICENSE](LICENSE).

## Contributing

See [CONTRIBUTING](docs/development/contributing.md).

---

**Developed by WiseRisk** | [GitHub](https://github.com/amaurydetremerie/HLabMonitor) | [Docker Hub](https://hub.docker.com/r/wiserisk/hlabmonitor)
