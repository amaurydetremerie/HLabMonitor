# Environment Variables

HLabMonitor supports configuration via environment variables, which is particularly useful for containerized deployments (Docker, Kubernetes) and CI/CD pipelines.

> ⚠️ **Recommendation**: For most deployments, using [`application.yaml`](application-yaml.md) is preferred for better readability, maintainability, and version control. Use environment variables primarily for:
> - Docker/container deployments
> - Kubernetes ConfigMaps/Secrets
> - Overriding specific values without modifying configuration files
> - CI/CD pipelines and testing

---

## Configuration File Locations

HLabMonitor automatically searches for `application.yaml` in the following locations (in order of priority):

1. **`/etc/hlabmonitor/`** (default in Docker images)
2. **Custom location** specified via `HLABMONITOR_CONFIG_LOCATION` environment variable
3. Classpath locations (built-in defaults)

### Using the Default Location (Docker)

Mount your configuration file to `/etc/hlabmonitor/`:

``` bash
docker run -d \
-v /path/to/application.yaml:/etc/hlabmonitor/application.yaml:ro \
-p 8080:8080 \
wiserisk/hlabmonitor:latest
```

### Using a Custom Location

Set `HLABMONITOR_CONFIG_LOCATION` to specify an alternative configuration directory:

``` bash
docker run -d \
-e HLABMONITOR_CONFIG_LOCATION=file:/config/ \
-v /path/to/config:/config:ro \
-p 8080:8080 \
wiserisk/hlabmonitor:latest
```

> 💡 **Note**: The `file:` prefix is required, and the path must end with a trailing slash `/`.

---

## How It Works

Spring Boot automatically maps environment variables to configuration properties using the following convention:

- Lowercase property names become **UPPERCASE**
- Dots (`.`) become **underscores** (`_`)
- Dashes (`-`) become **underscores** (`_`)

**Examples:**
- `database.type` → `DATABASE_TYPE`
- `database.host` → `DATABASE_HOST`
- `monitoring.ping.gateway.target` → `MONITORING_PING_GATEWAY_TARGET`

---

## Configuration Location

| Environment Variable | Type | Description |
|---------------------|------|-------------|
| `HLABMONITOR_CONFIG_LOCATION` | string | Custom directory path for configuration files (must include `file:` prefix and trailing `/`) |

**Example:**

``` bash
HLABMONITOR_CONFIG_LOCATION=file:/custom/config/
```

---

## Database Configuration

### Basic Properties

| Environment Variable | Type | YAML Equivalent | Description |
|---------------------|------|-----------------|-------------|
| `DATABASE_TYPE` | string | `database.type` | Database type: `h2`, `sqlite`, `postgresql`, `sqlserver` |
| `DATABASE_PATH` | string | `database.path` | SQLite database file path |
| `DATABASE_HOST` | string | `database.host` | Database server hostname |
| `DATABASE_PORT` | integer | `database.port` | Database server port |
| `DATABASE_NAME` | string | `database.name` | Database name |
| `DATABASE_USERNAME` | string | `database.username` | Database username |
| `DATABASE_PASSWORD` | string | `database.password` | Database password |

### Examples

#### PostgreSQL via Environment Variables

``` bash
DATABASE_TYPE=postgresql
DATABASE_HOST=postgres.example.com
DATABASE_PORT=5432
DATABASE_NAME=hlabmonitor
DATABASE_USERNAME=monitor_user
DATABASE_PASSWORD=secure_password
```

#### SQLite via Environment Variables

``` bash
DATABASE_TYPE=sqlite
DATABASE_PATH=/var/lib/hlabmonitor/monitor.db
```

#### Docker Example

``` bash
docker run -d \
-e DATABASE_TYPE=postgresql \
-e DATABASE_HOST=postgres.local \
-e DATABASE_NAME=hlabmonitor \
-e DATABASE_USERNAME=monitor \
-e DATABASE_PASSWORD=secret \
-p 8080:8080 \
wiserisk/hlabmonitor:latest
```

---

## Monitoring Configuration

> ⚠️ **Limitation**: Configuring monitoring targets via environment variables is **verbose and not recommended**. Use [`application.yaml`](application-yaml.md#monitoring-configuration) instead.

Environment variables work for monitoring but require repeating the full property path for each target and property.

### Ping Monitoring

**Format**: `MONITORING_PING_<TARGET_KEY>_<PROPERTY>`

**Example:**

``` bash
# Define a ping target named "gateway"
MONITORING_PING_GATEWAY_TARGET=192.168.1.1
MONITORING_PING_GATEWAY_INTERVAL=30s

# Define a ping target named "dns"
MONITORING_PING_DNS_TARGET=8.8.8.8
MONITORING_PING_DNS_INTERVAL=1m
```

**YAML Equivalent** (much cleaner):

``` yaml
monitoring:
 ping:
  gateway:
   target: 192.168.1.1
   interval: 30s
  dns:
   target: 8.8.8.8
   interval: 1m
```

### HTTP Monitoring

**Format**: `MONITORING_HTTP_<TARGET_KEY>_<PROPERTY>`

**Example:**

``` bash
# Define an HTTP target named "website"
MONITORING_HTTP_WEBSITE_TARGET=www.example.com
MONITORING_HTTP_WEBSITE_INTERVAL=5m
MONITORING_HTTP_WEBSITE_SSL=true
MONITORING_HTTP_WEBSITE_CERTIFICATE_VERIFY=true
MONITORING_HTTP_WEBSITE_CERTIFICATE_INTERVAL=1d
```

**YAML Equivalent**:

``` yaml
monitoring:
 http:
  website:
   target: www.example.com
   interval: 5m
   ssl: true
   certificate:
    verify: true
    interval: 1d
```

---

## Debug Configuration

| Environment Variable | Type | YAML Equivalent | Default | Description |
|---------------------|------|-----------------|---------|-------------|
| `DEBUG_CONTROLLER_ENABLED` | boolean | `debug.controller.enabled` | `false` | Enable debug REST endpoints |

**Example:**

``` bash
DEBUG_CONTROLLER_ENABLED=true
```

---

## Hybrid Approach (Recommended)

The most practical approach is to use `application.yaml` for monitoring configuration and environment variables for **sensitive data and deployment-specific values**.

### Example: Docker Compose

``` yaml
version: '3.8'

services:
 hlabmonitor:
  image: wiserisk/hlabmonitor:latest
  environment:
   # Database config via environment variables
   DATABASE_TYPE: postgresql
   DATABASE_HOST: postgres
   DATABASE_NAME: hlabmonitor
   DATABASE_USERNAME: monitor_user
   DATABASE_PASSWORD: ${DB_PASSWORD}  # from .env file
  volumes:
   # Monitoring config via YAML file (default location)
   - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
  ports:
   - "8080:8080"
```

**`application.yaml`:**

``` yaml
monitoring:
 ping:
  gateway:
   target: 192.168.1.1
   interval: 30s
 http:
  website:
   target: www.example.com
   interval: 5m
   ssl: true
   certificate:
    verify: true
    interval: 1d
```

This approach provides:
- ✅ Clean, readable monitoring configuration in YAML
- ✅ Secure credential management via environment variables
- ✅ Easy secrets management (Kubernetes Secrets, Docker Secrets, .env files)

---

## Kubernetes ConfigMaps and Secrets

### Database Credentials (Secret)

``` yaml
apiVersion: v1
kind: Secret
metadata:
 name: hlabmonitor-db-secret
 type: Opaque
 stringData:
  database-username: monitor_user
  database-password: secure_password
```

### Database Configuration (ConfigMap)

``` yaml
apiVersion: v1
kind: ConfigMap
metadata:
 name: hlabmonitor-db-config
data:
 DATABASE_TYPE: "postgresql"
 DATABASE_HOST: "postgres.default.svc.cluster.local"
 DATABASE_PORT: "5432"
 DATABASE_NAME: "hlabmonitor"
```

### Monitoring Configuration (ConfigMap with YAML)

``` yaml
apiVersion: v1
kind: ConfigMap
metadata:
 name: hlabmonitor-monitoring-config
data:
 application.yaml: |
  monitoring:
   ping:
    gateway:
     target: 192.168.1.1
     interval: 30s
   http:
    website:
     target: www.example.com
     interval: 5m
```

### Deployment

``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hlabmonitor
spec:
  template:
    spec:
      containers:
      - name: hlabmonitor
        image: wiserisk/hlabmonitor:latest
        envFrom:
        - configMapRef:
            name: hlabmonitor-db-config
        env:
        - name: DATABASE_USERNAME
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-username
        - name: DATABASE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: hlabmonitor-db-secret
              key: database-password
        volumeMounts:
        - name: monitoring-config
          mountPath: /etc/hlabmonitor
          readOnly: true
      volumes:
      - name: monitoring-config
        configMap:
          name: hlabmonitor-monitoring-config
```

---

## Priority Order

When the same property is defined in multiple places, Spring Boot applies them in this order (highest priority first):

1. **Environment variables** (highest priority)
2. **Command-line arguments** (`--database.host=...`)
3. **`application.yaml`** in custom location (via `HLABMONITOR_CONFIG_LOCATION`)
4. **`application.yaml`** in `/etc/hlabmonitor/` (Docker default)
5. **`application.yaml`** in classpath
6. **Default values** in code (lowest priority)

### Example

If you have:

**`/etc/hlabmonitor/application.yaml`:**
``` yaml
database:
  host: yaml-configured.example.com
```

**Environment variable:**
``` bash
DATABASE_HOST=env-override.example.com
```

**Result**: HLabMonitor will connect to `env-override.example.com` (environment variable wins).

---

## Standard Spring Boot Properties

You can also override standard Spring Boot properties via environment variables:

| Environment Variable | YAML Equivalent | Description |
|---------------------|-----------------|-------------|
| `SERVER_PORT` | `server.port` | HTTP server port (default: 8080) |
| `LOGGING_LEVEL_ROOT` | `logging.level.root` | Root logging level (INFO, DEBUG, etc.) |
| `SPRING_PROFILES_ACTIVE` | `spring.profiles.active` | Active Spring profiles |
| `SPRING_LIQUIBASE_ENABLED` | `spring.liquibase.enabled` | Enable/disable Liquibase migrations |

**Example:**

``` bash
SERVER_PORT=9090
LOGGING_LEVEL_ROOT=DEBUG
SPRING_PROFILES_ACTIVE=production
```

---

## Best Practices

### ✅ DO

- Use environment variables for **secrets** (passwords, API keys)
- Use environment variables for **deployment-specific values** (hostnames, ports)
- Use environment variables in **containerized environments** (Docker, Kubernetes)
- Store secrets in secure vaults (Kubernetes Secrets, HashiCorp Vault, AWS Secrets Manager)
- Mount configuration files to `/etc/hlabmonitor/` in Docker for simplicity

### ❌ DON'T

- Don't configure complex monitoring targets via environment variables (use YAML)
- Don't hardcode secrets in Dockerfiles or Kubernetes manifests
- Don't commit `.env` files with real credentials to version control
- Don't forget the `file:` prefix and trailing `/` when using `HLABMONITOR_CONFIG_LOCATION`

### 📝 Recommendation Summary

| Configuration Type | Recommended Method | Reason |
|-------------------|-------------------|--------|
| Database credentials | Environment variables | Security (secrets management) |
| Database connection details | Environment variables or YAML | Deployment flexibility |
| Monitoring targets | YAML file | Readability and maintainability |
| Debug flags | YAML file | Version controlled |
| Container overrides | Environment variables | Deployment-specific |

---

## Docker Configuration Examples

### Default Location (Recommended)

``` bash
docker run -d \
--name hlabmonitor \
-e DATABASE_TYPE=postgresql \
-e DATABASE_HOST=postgres.local \
-e DATABASE_USERNAME=monitor_user \
-e DATABASE_PASSWORD=secure_password \
-v /path/to/application.yaml:/etc/hlabmonitor/application.yaml:ro \
-p 8080:8080 \
wiserisk/hlabmonitor:latest
```

### Custom Location

``` bash
docker run -d \
--name hlabmonitor \
-e HLABMONITOR_CONFIG_LOCATION=file:/custom/config/ \
-e DATABASE_TYPE=postgresql \
-e DATABASE_HOST=postgres.local \
-e DATABASE_USERNAME=monitor_user \
-e DATABASE_PASSWORD=secure_password \
-v /path/to/config:/custom/config:ro \
-p 8080:8080 \
wiserisk/hlabmonitor:latest
```

### Multiple Configuration Files

You can have multiple YAML files in the configuration directory:

``` bash
# Structure:
# /etc/hlabmonitor/
#   ├── application.yaml    (main config)
#   ├── application-production.yaml  (profile-specific)
#   └── application-monitoring.yaml  (split config)

docker run -d \
-e SPRING_PROFILES_ACTIVE=production \
-v /config:/etc/hlabmonitor:ro \
-p 8080:8080 \
wiserisk/hlabmonitor:latest
```

---

## Troubleshooting

### Configuration File Not Loaded

**Symptom**: HLabMonitor doesn't apply settings from `application.yaml`.

**Possible Causes:**
1. File not mounted to `/etc/hlabmonitor/`
2. Incorrect `HLABMONITOR_CONFIG_LOCATION` format
3. File permissions issue (container user is 1001:1001)

**Solution:**
``` bash
# Verify mount is correct
docker inspect <container> | grep -A 10 Mounts

# Check file permissions (should be readable by UID 1001)
ls -la /path/to/application.yaml

# View logs to see which config locations are being checked
docker logs <container> | grep "config"
```

### Custom Location Not Working

**Symptom**: `HLABMONITOR_CONFIG_LOCATION` not loading configuration.

**Common Mistakes:**
- Missing `file:` prefix
- Missing trailing `/`
- Incorrect path

**Correct format:**
``` bash
✅ HLABMONITOR_CONFIG_LOCATION=file:/custom/config/
❌ HLABMONITOR_CONFIG_LOCATION=/custom/config
❌ HLABMONITOR_CONFIG_LOCATION=file:/custom/config
```

### Environment Variable Not Applied

**Symptom**: Setting `DATABASE_HOST=postgres` but HLabMonitor still uses `localhost`.

**Possible Causes:**
1. Variable name incorrect (remember: UPPERCASE and underscores)
2. YAML property has higher priority (check configuration files)
3. Variable not exported in shell session

**Solution:**
``` bash
# Verify the variable is set
echo $DATABASE_HOST

# Export it if using shell
export DATABASE_HOST=postgres

# Check HLabMonitor startup logs for active configuration
docker logs <container> | grep -i database
```

### Special Characters in Passwords

If your password contains special characters (`$`, `!`, `&`, etc.), quote them properly:

**Docker:**
``` bash
docker run -e "DATABASE_PASSWORD=Pa\$\$w0rd!" wiserisk/hlabmonitor:latest
```

**Shell:**
``` bash
export DATABASE_PASSWORD='Pa$$w0rd!'
```

**Kubernetes Secret:**
``` yaml
stringData:
  database-password: "Pa$$w0rd!"
```

---

## Complete `.env` File for Docker Compose

``` bash
# Configuration Location (optional)
# HLABMONITOR_CONFIG_LOCATION=file:/custom/config/

# Database Configuration
DATABASE_TYPE=postgresql
DATABASE_HOST=postgres.local
DATABASE_PORT=5432
DATABASE_NAME=hlabmonitor
DATABASE_USERNAME=monitor_user
DATABASE_PASSWORD=secure_password

# Server Configuration
SERVER_PORT=8080

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_BE_WISERISK_HLABMONITOR=DEBUG

# Debug
DEBUG_CONTROLLER_ENABLED=false
```

**`docker-compose.yml`:**

``` yaml
version: '3.8'

services:
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    env_file: .env
    volumes:
      - ./application.yaml:/etc/hlabmonitor/application.yaml:ro
    ports:
      - "8080:8080"
```

---

## See Also

- [Application YAML Reference](application-yaml.md) - Complete configuration property reference
- [Database Setup](database.md) - Database-specific configuration details
- [Docker Compose Examples](../deployment/docker-compose.md) - Ready-to-use deployment examples
- [Kubernetes Manifests](../deployment/kubernetes-manifests.md) - Kubernetes deployment guides
