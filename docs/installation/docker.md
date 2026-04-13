# Docker Installation

## Available Images

| Tag | Base | Use Case |
|-----|------|----------|
| `wiserisk/hlabmonitor:latest` | Ubuntu/Temurin | Default, recommended |
| `wiserisk/hlabmonitor:latest-alpine` | Alpine Linux | Smaller image size |
| `wiserisk/hlabmonitor:latest-corretto` | Ubuntu/Amazon Corretto | AWS environments |

## Quick Start

```bash
docker run -d \
  --name hlabmonitor \
  -p 8080:8080 \
  -v hlabmonitor-data:/etc/hlabmonitor \
  wiserisk/hlabmonitor:latest
```

HLabMonitor is now running at `http://localhost:8080` with an H2 in-memory database.

> **H2 is non-persistent.** Data is lost on container restart. Use SQLite or PostgreSQL for persistent storage.

## Configuration

Place an `application.yaml` at `/etc/hlabmonitor/application.yaml` inside the container (i.e. in the mounted volume).

### Mount a host directory

```bash
# Create config directory on host
mkdir -p /opt/hlabmonitor/config

# Create your application.yaml
cat > /opt/hlabmonitor/config/application.yaml << 'EOF'
database:
  type: sqlite

monitoring:
  ping:
    router:
      target: 192.168.1.1
      interval: 1m
  notification:
    discord:
      enabled: false
    telegram:
      enabled: false
    email:
      enabled: false
    log:
      enabled: true
      level: INFO
EOF

# Run with host directory mounted
docker run -d \
  --name hlabmonitor \
  -p 8080:8080 \
  -v /opt/hlabmonitor/config:/etc/hlabmonitor \
  wiserisk/hlabmonitor:latest
```

### Override via environment variables

Spring Boot properties map to env vars: dots and hyphens become underscores, all uppercase.

```bash
docker run -d \
  --name hlabmonitor \
  -p 8080:8080 \
  -e HLABMONITOR_DATABASE_TYPE=postgresql \
  -e HLABMONITOR_DATABASE_HOST=my-postgres \
  -e HLABMONITOR_DATABASE_NAME=hlabmonitor \
  -e HLABMONITOR_DATABASE_USERNAME=monitor_user \
  -e HLABMONITOR_DATABASE_PASSWORD=secret \
  wiserisk/hlabmonitor:latest
```

### Custom config file path

```bash
docker run -d \
  --name hlabmonitor \
  -p 8080:8080 \
  -v /path/to/my-config.yaml:/config/app.yaml \
  -e HLABMONITOR_CONFIG_LOCATION=/config/app.yaml \
  wiserisk/hlabmonitor:latest
```

## Docker Compose with PostgreSQL

```yaml
services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    ports:
      - "8080:8080"
    volumes:
      - hlabmonitor_config:/etc/hlabmonitor
    environment:
      HLABMONITOR_DATABASE_TYPE: postgresql
      HLABMONITOR_DATABASE_HOST: postgres_db
      HLABMONITOR_DATABASE_NAME: hlabmonitor_db
      HLABMONITOR_DATABASE_USERNAME: hlabuser
      HLABMONITOR_DATABASE_PASSWORD: ${POSTGRES_PASSWORD}
    depends_on:
      - postgres_db
    restart: unless-stopped

  postgres_db:
    image: postgres:16-alpine
    container_name: hlabmonitor_postgres
    environment:
      POSTGRES_DB: hlabmonitor_db
      POSTGRES_USER: hlabuser
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

volumes:
  hlabmonitor_config:
  postgres_data:
```

Create a `.env` file alongside the compose file:

```
POSTGRES_PASSWORD=your_secure_password
```

Then start:

```bash
docker compose up -d
docker compose logs -f hlabmonitor
```

## Alpine Variant

```bash
docker run -d \
  --name hlabmonitor \
  -p 8080:8080 \
  -v hlabmonitor-data:/etc/hlabmonitor \
  wiserisk/hlabmonitor:latest-alpine
```

The Alpine image is functionally identical but smaller. Use it if image size matters.

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response: `{"status":"UP",...}`

Docker health check integration:

```yaml
healthcheck:
  test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 30s
```
