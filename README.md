# HLabMonitor

> Comprehensive monitoring solution for your HomeLab

[![Docker Image](https://img.shields.io/docker/v/wiserisk/hlabmonitor?label=Docker)](https://hub.docker.com/r/wiserisk/hlabmonitor)
[![Latest Release](https://img.shields.io/github/v/release/amaurydetremerie/HLabMonitor)](https://github.com/amaurydetremerie/HLabMonitor/releases)
[![License](https://img.shields.io/github/license/amaurydetremerie/HLabMonitor)](LICENSE)

HLabMonitor is a Spring Boot application designed to monitor and manage your HomeLab infrastructure, providing real-time metrics, database monitoring, and extensive integration capabilities.

## Features

- 📊 Real-time monitoring and metrics collection
- 🗄️ Multi-database support (PostgreSQL, H2, SQLite, SQL Server)
- 🔌 REST API with OpenAPI/Swagger documentation
- 📈 Spring Boot Actuator integration
- 🐳 Multiple deployment options (Docker, Kubernetes, systemd)
- 🔄 Database migrations with Liquibase
- 🔒 Security-focused design

## Quick Start

```bash
# Docker (easiest way to get started)
docker run -d -p 8080:8080 wiserisk/hlabmonitor:latest

# Access the application
open http://localhost:8080
```

**That's it!** HLabMonitor is now running with an embedded H2 database.

Access the Swagger UI at http://localhost:8080/swagger-ui.html

## Installation Options

Choose the installation method that best fits your environment:

| Method | Best For | Documentation |
|--------|----------|---------------|
| 🐳 **Docker** | Quick testing, containerized environments | [Docker Guide](docs/installation/docker.md) |
| ☸️ **Kubernetes** | Production clusters, orchestration | [Kubernetes Guide](docs/installation/kubernetes.md) |
| 🔄 **ArgoCD** | GitOps workflows | [ArgoCD Guide](docs/installation/argocd.md) |
| 📦 **DEB Package** | Debian/Ubuntu servers | [Debian/Ubuntu Guide](docs/installation/debian-ubuntu.md) |
| 📦 **RPM Package** | RHEL/Fedora/Rocky servers | [RHEL/Fedora Guide](docs/installation/rhel-fedora.md) |
| ☕ **Standalone JAR** | Any platform with Java 21+ | [JAR Guide](docs/installation/standalone-jar.md) |

## Documentation

### Configuration
- 📝 [Application YAML Reference](docs/configuration/application-yaml.md) - Complete configuration options
- 🔧 [Environment Variables](docs/configuration/environment-variables.md) - Override configuration
- 🗄️ [Database Setup](docs/configuration/database.md) - PostgreSQL, SQL Server, SQLite

### Deployment
- 🐳 [Docker Compose Examples](docs/deployment/docker-compose.md)
- ☸️ [Kubernetes Manifests](docs/deployment/kubernetes-manifests.md)
- ✅ [Production Checklist](docs/deployment/production-checklist.md)

### Monitoring & Operations
- 📊 [Metrics & Monitoring](docs/monitoring/metrics.md)
- 📋 [Logging](docs/monitoring/logs.md)
- 🚨 [Alerting](docs/monitoring/alerting.md)

### Development
- 🏗️ [Building from Source](docs/development/building.md)
- 🤝 [Contributing Guide](docs/development/contributing.md)
- 🏛️ [Architecture Overview](docs/development/architecture.md)

### Help
- 🔍 [Troubleshooting](docs/troubleshooting/common-issues.md)
- ❓ [FAQ](docs/troubleshooting/faq.md)

## Requirements

- **Java**: OpenJDK/Temurin/Corretto 21+
- **Databases** (optional): PostgreSQL, SQL Server, SQLite (H2 embedded by default)
- **Container Runtime** (optional): Docker, Podman, containerd

## API & Endpoints

Once running, access:

- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics

## Docker Images

Available variants on [Docker Hub](https://hub.docker.com/r/wiserisk/hlabmonitor):

```bash
# Ubuntu/Temurin (default)
docker pull wiserisk/hlabmonitor:latest

# Alpine (lightweight)
docker pull wiserisk/hlabmonitor:latest-alpine

# Amazon Corretto
docker pull wiserisk/hlabmonitor:latest-corretto
```

## Upgrading

See the [upgrade guide](docs/installation/upgrading.md) for version-specific migration notes.

## Support

- 📖 [Documentation](docs/)
- 🐛 [Issue Tracker](https://github.com/amaurydetremerie/HLabMonitor/issues)
- 💬 [Discussions](https://github.com/amaurydetremerie/HLabMonitor/discussions)

## License

[Your License] - See [LICENSE](LICENSE) file for details

## Contributing

Contributions are welcome! See [CONTRIBUTING.md](docs/development/contributing.md) for guidelines.

---

**Developed by WiseRisk** | [GitHub](https://github.com/amaurydetremerie/HLabMonitor) | [Docker Hub](https://hub.docker.com/r/wiserisk/hlabmonitor)