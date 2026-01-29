# Docker Compose Examples

This guide provides ready-to-use Docker Compose configurations for deploying HLabMonitor in various scenarios, from simple testing to production-ready setups.

---

## Docker Image Variants

HLabMonitor is available in three Docker image variants to suit different environments:

| Image Tag | Base Image | Size | Best For |
|-----------|------------|------|----------|
| `latest` or `ubuntu` | Eclipse Temurin 21 JRE (Ubuntu Jammy) | ~250MB | General use, maximum compatibility |
| `alpine` | Eclipse Temurin 21 JRE (Alpine) | ~180MB | Lightweight deployments, minimal footprint |
| `corretto` | Amazon Corretto 21 (AL2023) | ~240MB | AWS environments, Amazon Linux preference |

### Available Tags

**Latest stable releases:**
- `wiserisk/hlabmonitor:latest` - Latest Ubuntu-based image (default)
- `wiserisk/hlabmonitor:ubuntu` - Latest Ubuntu-based image
- `wiserisk/hlabmonitor:alpine` - Latest Alpine-based image
- `wiserisk/hlabmonitor:corretto` - Latest Corretto-based image

**Version-specific tags** (example with v1.0.0):
- `wiserisk/hlabmonitor:1.0.0`, `1.0`, `1` - Ubuntu-based versioned releases
- `wiserisk/hlabmonitor:1.0.0-alpine`, `1.0-alpine`, `1-alpine` - Alpine-based versioned releases
- `wiserisk/hlabmonitor:1.0.0-corretto`, `1.0-corretto`, `1-corretto` - Corretto-based versioned releases

**Recommendation**: Use `latest` (default) unless you have specific size or compatibility requirements. For production, prefer version-pinned tags like `1.0.0` to avoid unexpected updates.

### Version Pinning (Recommended for Production)

For production deployments, it's recommended to pin specific versions instead of using `latest`:

``` yaml
services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:1.0.0  # Ubuntu variant
    # or
    # image: wiserisk/hlabmonitor:1.0.0-alpine  # Alpine variant
    # or
    # image: wiserisk/hlabmonitor:1.0.0-corretto  # Corretto variant
```

This prevents automatic updates that could introduce breaking changes. Update explicitly when you're ready:

``` bash
# Update to new version
docker-compose pull
docker-compose up -d
```

Check available versions on [Docker Hub](https://hub.docker.com/r/wiserisk/hlabmonitor/tags).

---

## Table of Contents

- [Quick Start (H2)](#quick-start-h2)
- [SQLite Deployment](#sqlite-deployment)
- [PostgreSQL Stack](#postgresql-stack)
- [SQL Server Stack](#sql-server-stack)
- [Production Examples](#production-examples)
- [Advanced Configurations](#advanced-configurations)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Quick Start (H2)

**Minimal setup with embedded H2 database.** Perfect for testing and evaluation.

### Basic Setup

**`docker-compose.yml`:**

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    restart: unless-stopped
```

**`application.yaml`:**

``` yaml
monitoring:
  ping:
    gateway:
      target: 192.168.1.1
      interval: 1m
  http:
    google:
      target: www.google.com
      interval: 5m
```

**Start:**

``` bash
docker-compose up -d
```

**Access:** http://localhost:8080

> ⚠️ **Note**: H2 is in-memory. Data is lost on container restart. Use SQLite, PostgreSQL, or SQL Server for persistence.

---

## SQLite Deployment

**Persistent file-based database.** Ideal for homelab single-instance deployments.

### With Persistent Volume

**`docker-compose.yml`:**

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    environment:
      DATABASE_TYPE: sqlite
      # Path uses default: /var/lib/hlabmonitor/monitor.db
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
      - hlabmonitor-data:/var/lib/hlabmonitor
    restart: unless-stopped

volumes:
  hlabmonitor-data:
```

**`application.yaml`:**

``` yaml
monitoring:
  ping:
    router:
      target: 192.168.1.1
      interval: 30s
    
    pihole:
      target: 192.168.1.10
      interval: 1m
  
  http:
    homeassistant:
      target: homeassistant.local
      interval: 2m
      ssl: true
      certificate:
        verify: true
        interval: 1d
    
    proxmox:
      target: proxmox.local
      interval: 5m
      ssl: true
```

### With Host Bind Mount

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    environment:
      DATABASE_TYPE: sqlite
      DATABASE_PATH: /data/monitor.db
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
      - ./data:/data
    restart: unless-stopped
```

**Create data directory:**

``` bash
mkdir -p ./data
chmod 755 ./data
```

---

## PostgreSQL Stack

**Production-ready setup with PostgreSQL.** Recommended for high availability and scalability.

### Complete Stack with Healthchecks

**`docker-compose.yml`:**

``` yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: hlabmonitor-postgres
    environment:
      POSTGRES_DB: hlabmonitor
      POSTGRES_USER: hlabmonitor_user
      POSTGRES_PASSWORD: ${DB_PASSWORD:-change_me_in_production}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - hlabmonitor-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hlabmonitor_user -d hlabmonitor"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    restart: unless-stopped

  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_PORT: 5432
      DATABASE_NAME: hlabmonitor
      DATABASE_USERNAME: hlabmonitor_user
      DATABASE_PASSWORD: ${DB_PASSWORD:-change_me_in_production}
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    networks:
      - hlabmonitor-net
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped

volumes:
  postgres-data:

networks:
  hlabmonitor-net:
    driver: bridge
```

**`.env` file:**

``` bash
DB_PASSWORD=secure_password_here
```

**`application.yaml`:**

``` yaml
monitoring:
  ping:
    gateway:
      target: 192.168.1.1
      interval: 30s
    
    dns-primary:
      target: 192.168.1.1
      interval: 1m
    
    dns-secondary:
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
    
    grafana:
      target: grafana.local
      interval: 3m
      ssl: true
      certificate:
        verify: true
        interval: 12h
```

### With External PostgreSQL

If you have an existing PostgreSQL instance:

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres.example.com
      DATABASE_PORT: 5432
      DATABASE_NAME: hlabmonitor
      DATABASE_USERNAME: hlabmonitor_user
      DATABASE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    restart: unless-stopped
```

---

## SQL Server Stack

**For Windows-centric environments or existing SQL Server infrastructure.**

### Complete Stack

**`docker-compose.yml`:**

``` yaml
version: '3.8'

services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: hlabmonitor-sqlserver
    environment:
      ACCEPT_EULA: "Y"
      SA_PASSWORD: ${SQL_SA_PASSWORD:-YourStrong@Password123}
      MSSQL_PID: "Express"
    volumes:
      - sqlserver-data:/var/opt/mssql
    networks:
      - hlabmonitor-net
    ports:
      - "1433:1433"
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P $${SA_PASSWORD} -Q 'SELECT 1' || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s
    restart: unless-stopped

  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    depends_on:
      sqlserver:
        condition: service_healthy
    environment:
      DATABASE_TYPE: sqlserver
      DATABASE_HOST: sqlserver
      DATABASE_PORT: 1433
      DATABASE_NAME: HLabMonitor
      DATABASE_USERNAME: sa
      DATABASE_PASSWORD: ${SQL_SA_PASSWORD:-YourStrong@Password123}
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    networks:
      - hlabmonitor-net
    restart: unless-stopped

volumes:
  sqlserver-data:

networks:
  hlabmonitor-net:
    driver: bridge
```

> 💡 **Production Tip**: Create a dedicated SQL Server user instead of using `sa`. See [Database Setup](../configuration/database.md#sql-server) for details.

---

## Production Examples

### High Availability PostgreSQL with Backups

``` yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: hlabmonitor-postgres
    environment:
      POSTGRES_DB: hlabmonitor
      POSTGRES_USER: hlabmonitor_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      PGDATA: /var/lib/postgresql/data/pgdata
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./backups:/backups
    networks:
      - hlabmonitor-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hlabmonitor_user -d hlabmonitor"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 512M
        reservations:
          memory: 256M

  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_NAME: hlabmonitor
      DATABASE_USERNAME: hlabmonitor_user
      DATABASE_PASSWORD: ${DB_PASSWORD}
      LOGGING_LEVEL_ROOT: INFO
      LOGGING_LEVEL_BE_WISERISK_HLABMONITOR: DEBUG
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
      - hlabmonitor-logs:/var/log/hlabmonitor
    networks:
      - hlabmonitor-net
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
    restart: unless-stopped
    deploy:
      resources:
        limits:
          memory: 1024M
        reservations:
          memory: 512M

  # Optional: Automated backups
  postgres-backup:
    image: prodrigestivill/postgres-backup-local:16-alpine
    container_name: hlabmonitor-backup
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      POSTGRES_HOST: postgres
      POSTGRES_DB: hlabmonitor
      POSTGRES_USER: hlabmonitor_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
      SCHEDULE: "@daily"
      BACKUP_KEEP_DAYS: 7
      BACKUP_KEEP_WEEKS: 4
      BACKUP_KEEP_MONTHS: 6
    volumes:
      - ./backups:/backups
    networks:
      - hlabmonitor-net
    restart: unless-stopped

volumes:
  postgres-data:
  hlabmonitor-logs:

networks:
  hlabmonitor-net:
    driver: bridge
```

### Alpine Variant (Lightweight)

``` yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: hlabmonitor-postgres
    environment:
      POSTGRES_DB: hlabmonitor
      POSTGRES_USER: hlabmonitor_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - hlabmonitor-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hlabmonitor_user"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  hlabmonitor:
    image: wiserisk/hlabmonitor:alpine  # Alpine variant
    container_name: hlabmonitor
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_NAME: hlabmonitor
      DATABASE_USERNAME: hlabmonitor_user
      DATABASE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    networks:
      - hlabmonitor-net
    restart: unless-stopped

volumes:
  postgres-data:

networks:
  hlabmonitor-net:
```

---

## Advanced Configurations

### Multiple HLabMonitor Instances

Run multiple instances for different environments (dev/staging/prod):

``` yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: hlabmonitor-postgres
    environment:
      POSTGRES_MULTIPLE_DATABASES: hlabmonitor_dev,hlabmonitor_prod
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - hlabmonitor-net
    restart: unless-stopped

  hlabmonitor-dev:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor-dev
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_NAME: hlabmonitor_dev
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8081:8080"
    volumes:
      - ./config/dev/application.yaml:/etc/hlabmonitor/application.yaml:ro
    networks:
      - hlabmonitor-net
    restart: unless-stopped

  hlabmonitor-prod:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor-prod
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_NAME: hlabmonitor_prod
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8080:8080"
    volumes:
      - ./config/prod/application.yaml:/etc/hlabmonitor/application.yaml:ro
    networks:
      - hlabmonitor-net
    restart: unless-stopped

volumes:
  postgres-data:

networks:
  hlabmonitor-net:
```

### Behind Reverse Proxy (Traefik)

``` yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: hlabmonitor-postgres
    environment:
      POSTGRES_DB: hlabmonitor
      POSTGRES_USER: hlabmonitor_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - hlabmonitor-net
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hlabmonitor_user"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_NAME: hlabmonitor
      DATABASE_USERNAME: hlabmonitor_user
      DATABASE_PASSWORD: ${DB_PASSWORD}
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    networks:
      - hlabmonitor-net
      - traefik-net
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.hlabmonitor.rule=Host(`monitor.example.com`)"
      - "traefik.http.routers.hlabmonitor.entrypoints=websecure"
      - "traefik.http.routers.hlabmonitor.tls.certresolver=letsencrypt"
      - "traefik.http.services.hlabmonitor.loadbalancer.server.port=8080"
    restart: unless-stopped

volumes:
  postgres-data:

networks:
  hlabmonitor-net:
    driver: bridge
  traefik-net:
    external: true
```

### With Monitoring Stack (Prometheus + Grafana)

``` yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: hlabmonitor-postgres
    environment:
      POSTGRES_DB: hlabmonitor
      POSTGRES_USER: hlabmonitor_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - monitoring-net
    restart: unless-stopped

  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    container_name: hlabmonitor
    depends_on:
      - postgres
    environment:
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_NAME: hlabmonitor
      DATABASE_USERNAME: hlabmonitor_user
      DATABASE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8080:8080"
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    networks:
      - monitoring-net
    restart: unless-stopped

  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - monitoring-net
    restart: unless-stopped

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD:-admin}
    volumes:
      - grafana-data:/var/lib/grafana
    ports:
      - "3000:3000"
    networks:
      - monitoring-net
    restart: unless-stopped

volumes:
  postgres-data:
  prometheus-data:
  grafana-data:

networks:
  monitoring-net:
    driver: bridge
```

**`prometheus.yml`:**

``` yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'hlabmonitor'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['hlabmonitor:8080']
```

---

## Best Practices

### Security

1. **Use `.env` files for secrets** (never commit to version control):

``` bash
# .env
DB_PASSWORD=your_secure_password_here
SQL_SA_PASSWORD=your_sqlserver_password
GRAFANA_PASSWORD=your_grafana_password
```

``` bash
# .gitignore
.env
*.env
!.env.example
```

2. **Run containers as non-root** (already configured in HLabMonitor images as UID 1001)

3. **Use read-only mounts for config files**:

``` yaml
volumes:
    - ./application.yaml:/etc/hlabmonitor/application.yaml:ro  # :ro = read-only
```

4. **Limit container resources**:

``` yaml
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 1024M
    reservations:
      memory: 512M
```

### Persistence

1. **Use named volumes** for database data (not bind mounts):

``` yaml
volumes:
    - postgres-data:/var/lib/postgresql/data  # Named volume

volumes:
    postgres-data:  # Define named volume
```

2. **Back up your volumes regularly**:

``` bash
# Backup named volume
docker run --rm \
  -v hlabmonitor_postgres-data:/data \
  -v $(pwd)/backups:/backup \
  alpine tar czf /backup/postgres-backup-$(date +%Y%m%d).tar.gz -C /data .

# Restore named volume
docker run --rm \
  -v hlabmonitor_postgres-data:/data \
  -v $(pwd)/backups:/backup \
  alpine tar xzf /backup/postgres-backup-20260129.tar.gz -C /data
```

### Networking

1. **Use dedicated networks** to isolate services:

``` yaml
networks:
  hlabmonitor-net:
    driver: bridge
    ipam:
      config:
        - subnet: 172.28.0.0/16
```

2. **Only expose necessary ports**:

``` yaml
# Don't expose database ports unless needed
postgres:
  ports: []  # No ports exposed to host
```

### Health Checks

Always configure health checks for proper startup ordering:

``` yaml
healthcheck:
  test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8080/actuator/health"]
  interval: 30s
  timeout: 10s
  retries: 3
  start_period: 40s
```

### Logging

Configure log rotation and persistence:

``` yaml
services:
  hlabmonitor:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
    volumes:
      - hlabmonitor-logs:/var/log/hlabmonitor
```

---

## Troubleshooting

### Container Won't Start

**Check logs:**

``` bash
docker-compose logs hlabmonitor
docker-compose logs postgres
```

**Common issues:**
- Database not ready (wait for healthcheck)
- Configuration file not mounted
- Port already in use

### Database Connection Errors

**Verify database is running:**

``` bash
docker-compose ps
```

**Test database connection:**

``` bash
# PostgreSQL
docker-compose exec postgres psql -U hlabmonitor_user -d hlabmonitor -c "SELECT 1;"

# SQL Server
docker-compose exec sqlserver /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P $SQL_SA_PASSWORD -Q "SELECT 1"
```

**Check network connectivity:**

``` bash
docker-compose exec hlabmonitor ping postgres
```

### Configuration Not Applied

**Verify mount:**

``` bash
docker-compose exec hlabmonitor ls -la /etc/hlabmonitor/
docker-compose exec hlabmonitor cat /etc/hlabmonitor/application.yaml
```

**Check file permissions:**

``` bash
ls -la application.yaml
# Should be readable (at least 644)
```

### High Memory Usage

**Check container stats:**

``` bash
docker stats
```

**Adjust Java memory:**

``` yaml
services:
  hlabmonitor:
    environment:
      JAVA_OPTS: "-Xmx512m -Xms256m"
```

### Cannot Access Web UI

**Verify port binding:**

``` bash
docker-compose ps
netstat -tulpn | grep 8080
```

**Check firewall:**

``` bash
# Linux
sudo ufw allow 8080/tcp

# Verify HLabMonitor is listening
curl http://localhost:8080/actuator/health
```

---

## Useful Commands

### Start Stack

``` bash
docker-compose up -d
```

### View Logs

``` bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f hlabmonitor

# Last 100 lines
docker-compose logs --tail=100 hlabmonitor
```

### Restart Service

``` bash
docker-compose restart hlabmonitor
```

### Stop Stack

``` bash
docker-compose down
```

### Stop and Remove Volumes

``` bash
# ⚠️ This deletes all data!
docker-compose down -v
```

### Update Images

``` bash
docker-compose pull
docker-compose up -d
```

### Execute Commands in Container

``` bash
docker-compose exec hlabmonitor sh
docker-compose exec postgres psql -U hlabmonitor_user -d hlabmonitor
```

---

## See Also

- [Application YAML Reference](../configuration/application-yaml.md) - Configuration options
- [Database Setup](../configuration/database.md) - Database-specific guides
- [Environment Variables](../configuration/environment-variables.md) - Environment variable reference
- [Kubernetes Manifests](kubernetes-manifests.md) - Kubernetes deployment
- [Production Checklist](production-checklist.md) - Production readiness guide
