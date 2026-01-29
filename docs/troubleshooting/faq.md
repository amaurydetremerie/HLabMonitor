# Frequently Asked Questions

Common questions about HLabMonitor. Updated as questions arise.

---

## General

### What databases are supported?

HLabMonitor supports:
- **H2** - In-memory (testing/demo)
- **SQLite** - File-based (single instance)
- **PostgreSQL** - Recommended for production
- **SQL Server** - Enterprise environments

See [Database Configuration](../configuration/database.md) for details.

### Can I run multiple instances?

Yes, but with considerations:
- Use PostgreSQL or SQL Server (not SQLite)
- Ensure proper connection pooling
- Each instance will execute checks independently

See [High Availability](../deployment/kubernetes.md#high-availability).

### What's the difference between H2 and SQLite?

- **H2**: In-memory, data lost on restart, fast, for testing only
- **SQLite**: File-based, data persists, good for single-instance homelabs

---

## Configuration

### How do I add a new monitoring target?

Add to `application.yaml`:

``` yaml
monitoring:
  ping:
  my-server:
    target: 192.168.1.10
    interval: 1m
```

Restart the application.

### Can I reload configuration without restart?

Not currently. Restart required for configuration changes.

### How do I change the database type?

Set in `application.yaml` or environment variable:

``` yaml
database:
  type: postgresql
  host: localhost
  name: monitor
```

See [Database Configuration](../configuration/database.md).

---

## Monitoring

### How often are checks executed?

Configurable per target via `interval` parameter:

``` yaml
monitoring:
  ping:
    gateway:
      target: 192.168.1.1
      interval: 30s  # Every 30 seconds
```

### Can I disable a specific check?

Remove or comment it from `application.yaml`, then restart.

### How long is history retained?

By default, indefinitely. Configure retention in your database or implement custom cleanup.

---

## Docker

### Which Docker image should I use?

- **`latest`** - Ubuntu-based, best compatibility
- **`alpine`** - Smaller size, Alpine Linux
- **`corretto`** - Amazon Corretto runtime

All are functionally equivalent. Choose based on your preference.

### How do I persist data in Docker?

Mount a volume for database and configuration:

``` yaml
volumes:
  - ./application.yaml:/etc/hlabmonitor/application.yaml:ro 
  - hlabmonitor-data:/var/lib/hlabmonitor
  ```

---

## Kubernetes

### How do I deploy to Kubernetes?

See [Kubernetes Deployment Guide](../deployment/kubernetes.md).

### Can I use PostgreSQL operator?

Yes, HLabMonitor works with any PostgreSQL instance. Configure connection details in your deployment.

---

## Security

### Does HLabMonitor have authentication?

Not by default. It's designed for trusted homelab environments.

For production, secure with:
- Network policies
- Ingress authentication
- VPN access

### Are credentials stored securely?

Database credentials should be provided via:
- Kubernetes Secrets
- Docker Secrets
- Environment variables

Never commit credentials to Git.

---

## Troubleshooting

### Where are the logs?

- **Docker**: `docker logs hlabmonitor`
- **Kubernetes**: `kubectl logs <pod-name>`
- **Bare metal**: stdout or configured log file

See [Logging Guide](../monitoring/logs.md).

### How do I enable debug logging?

``` bash
# Environment variable
LOGGING_LEVEL_BE_WISERISK_HLABMONITOR=DEBUG

# Or in application.yaml
logging:
  level:
    be.wiserisk.hlabmonitor: DEBUG
```

### Application won't start, what should I check?

1. Java version (21+ required)
2. Database connectivity
3. Port availability (default 8080)
4. Configuration syntax
5. Check logs for errors

---

## Performance

### What are the resource requirements?

Minimal requirements:
- CPU: 0.5 core
- Memory: 512 MB
- Disk: 1 GB (for database)

Scales with number of targets and check frequency.

### How many targets can I monitor?

Tested with:
- 50+ targets with no issues
- 100+ targets should work fine

Practical limit depends on check intervals and system resources.

---

## Contributing

### How can I contribute?

See [Contributing Guide](../development/contributing.md).

### I found a bug, where do I report it?

[Create an issue](https://github.com/amaurydetremerie/HLabMonitor/issues) with:
- Version
- Environment
- Steps to reproduce
- Expected vs actual behavior

---

## Have a Question?

Not answered here? Try:

1. [Common Issues](common-issues.md)
2. [GitHub Discussions](https://github.com/amaurydetremerie/HLabMonitor/discussions)
3. [Create an Issue](https://github.com/amaurydetremerie/HLabMonitor/issues)

---

## See Also

- [Configuration Reference](../configuration/application-yaml.md)
- [Deployment Guides](../deployment/)
- [Monitoring Documentation](../monitoring/)
