# Database Setup

HLabMonitor supports multiple database backends for flexibility across different environments. By default, it uses an embedded **H2** database requiring zero configuration, making it ideal for quick starts and testing.

For production deployments or persistent storage requirements, PostgreSQL, SQL Server, and SQLite are fully supported with Liquibase managing all schema migrations automatically.

> 📝 **Configuration Syntax**: See [Application YAML Reference](application-yaml.md#database-configuration) for complete property reference and examples.

---

## Database Comparison

| Database | Best For | Setup Complexity | Production Ready | Embedded |
|----------|----------|------------------|------------------|----------|
| **H2** | Development, testing, demos | None | ❌ No | ✅ Yes |
| **SQLite** | Single-node, low traffic, homelab | Very Low | ⚠️ Limited | ✅ Yes |
| **PostgreSQL** | Production, high availability, scaling | Medium | ✅ Yes | ❌ No |
| **SQL Server** | Windows environments, enterprise integration | Medium | ✅ Yes | ❌ No |

---

## H2 Database (Default)

**Zero configuration required.** H2 runs in-memory by default and is automatically used when no database configuration is provided.

### When to Use
- Quick testing and development
- CI/CD pipelines
- Demo environments
- First-time setup

### Limitations
- ❌ **Data is not persisted** between restarts (in-memory mode)
- ❌ **Not suitable for production**
- ❌ Single connection limitations

### Configuration

No configuration needed. Simply start HLabMonitor:

``` bash
java -jar hlabmonitor.jar
# or
docker run wiserisk/hlabmonitor:latest
```

---

## SQLite

**File-based embedded database** requiring no external services. Ideal for homelab deployments with moderate monitoring loads.

### When to Use
- Single-instance deployments
- Homelab environments
- Low to moderate traffic
- Simplified backup (single file)

### Limitations
- ⚠️ **No concurrent write operations** (can limit scalability)
- ⚠️ **Single server only** (no clustering)
- Best for <100 monitored targets

### Setup

#### 1. Choose Storage Location

Default paths (used if `path` is not specified):
- **Linux/Unix**: `/var/lib/hlabmonitor/monitor.db`
- **Windows**: `C:\ProgramData\hlabmonitor\monitor.db`

#### 2. Ensure File Permissions

The user running HLabMonitor must have **read/write** access to:
- The database file (if it exists)
- The parent directory (to create the file)

``` bash
# Linux example
sudo mkdir -p /var/lib/hlabmonitor
sudo chown hlabmonitor:hlabmonitor /var/lib/hlabmonitor
sudo chmod 750 /var/lib/hlabmonitor
```

#### 3. Configure HLabMonitor

``` yaml
database:
  type: sqlite
  # path: /custom/path/monitor.db  # Optional: defaults to platform-specific location
```

### Backup

SQLite databases are single files. Backup is straightforward:

``` bash
# Stop HLabMonitor first to ensure consistency
systemctl stop hlabmonitor

# Copy the database file
cp /var/lib/hlabmonitor/monitor.db /backup/monitor-$(date +%Y%m%d).db

# Restart HLabMonitor
systemctl start hlabmonitor
```

For online backups, use SQLite's backup API (outside HLabMonitor scope).

### Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| `Unable to open database file` | Permission denied | Check file/directory ownership and permissions |
| `Database is locked` | Another process accessing the file | Ensure only one HLabMonitor instance is running |
| Slow performance | Large dataset on slow storage | Consider PostgreSQL or move to SSD |

---

## PostgreSQL

**Recommended for production deployments.** Provides robust performance, concurrent access, and horizontal scaling capabilities.

### When to Use
- Production environments
- High availability requirements
- Multiple concurrent users
- Large monitoring infrastructures (>100 targets)
- Kubernetes/multi-instance deployments

### Prerequisites

You need a running PostgreSQL server. HLabMonitor **does not install or manage PostgreSQL**.

**Official Resources:**
- [PostgreSQL Downloads](https://www.postgresql.org/download/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)

> 💡 **Homelab Tip**: Many homelabbers run PostgreSQL in Docker or as a shared service for multiple applications.

### Database Preparation

Before configuring HLabMonitor, prepare your PostgreSQL instance:

``` sql
-- Connect to PostgreSQL as admin
CREATE DATABASE hlabmonitor;
CREATE USER hlabmonitor_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE hlabmonitor TO hlabmonitor_user;

-- For PostgreSQL 15+ (additional permissions required)
\c hlabmonitor
GRANT ALL ON SCHEMA public TO hlabmonitor_user;
```

> ⚠️ **Security**: Use strong passwords and restrict network access appropriately.

### HLabMonitor Configuration

``` yaml
database:
  type: postgresql
  host: postgres.example.com  # or localhost for local installation
  port: 5432          # default PostgreSQL port
  name: hlabmonitor
  username: hlabmonitor_user
  password: secure_password
```

See [application-yaml.md](application-yaml.md#postgresql---full) for more examples.

### Docker Compose Example

``` yaml
version: '3.8'

  services:
    postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: hlabmonitor
      POSTGRES_USER: hlabmonitor_user
      POSTGRES_PASSWORD: secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U hlabmonitor_user"]
      interval: 10s
      timeout: 5s
      retries: 5
  
    hlabmonitor:
      image: wiserisk/hlabmonitor:latest
      depends_on:
        postgres:
          condition: service_healthy
      environment:
        DATABASE_TYPE: postgresql
        DATABASE_HOST: postgres
        DATABASE_NAME: hlabmonitor
        DATABASE_USERNAME: hlabmonitor_user
        DATABASE_PASSWORD: secure_password
      ports:
        - "8080:8080"

volumes:
  postgres_data:
```

### Connection Pooling

HLabMonitor uses HikariCP (Spring Boot default) with sensible defaults. For high-load scenarios, you can tune connection pool settings via standard Spring properties:

``` yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
```

> 📚 See [HikariCP Documentation](https://github.com/brettwooldridge/HikariCP) for advanced tuning.

### Troubleshooting

| Issue | Likely Cause | First Steps |
|-------|--------------|-------------|
| `Connection refused` | PostgreSQL not running or firewall blocking | Check PostgreSQL service status and firewall rules |
| `Authentication failed` | Wrong credentials or missing user | Verify username/password and user existence in PostgreSQL |
| `Database does not exist` | Database not created | Run `CREATE DATABASE hlabmonitor;` in PostgreSQL |
| `Permission denied` | User lacks privileges | Grant appropriate permissions (see Database Preparation) |
| `SSL connection error` | SSL/TLS configuration mismatch | Check PostgreSQL `ssl` setting and HLabMonitor SSL requirements |

> 🔗 **PostgreSQL Issues**: For PostgreSQL-specific problems (installation, replication, performance tuning), consult [PostgreSQL Community Resources](https://www.postgresql.org/support/).

> 🐛 **HLabMonitor Integration Issues**: If configuration is correct but HLabMonitor cannot connect, [open an issue](https://github.com/amaurydetremerie/HLabMonitor/issues).

---

## SQL Server

**Enterprise-grade option** for Windows-centric environments or existing SQL Server infrastructure.

### When to Use
- Existing SQL Server infrastructure
- Windows Server environments
- Enterprise compliance requirements
- Integration with Microsoft ecosystem

### Prerequisites

You need a running SQL Server instance. HLabMonitor **does not install or manage SQL Server**.

**Official Resources:**
- [SQL Server Downloads](https://www.microsoft.com/sql-server/sql-server-downloads) (Express is free)
- [SQL Server Documentation](https://docs.microsoft.com/sql/sql-server/)
- [SQL Server Docker Image](https://hub.docker.com/_/microsoft-mssql-server)

### Database Preparation

``` sql
-- Connect to SQL Server as admin (sa or equivalent)
CREATE DATABASE HLabMonitor;
GO

CREATE LOGIN hlabmonitor_user WITH PASSWORD = 'SecurePassword123!';
GO

USE HLabMonitor;
CREATE USER hlabmonitor_user FOR LOGIN hlabmonitor_user;
ALTER ROLE db_owner ADD MEMBER hlabmonitor_user;
GO
```

> ⚠️ **Security**: Consider using Windows Authentication in domain environments instead of SQL Authentication.

### HLabMonitor Configuration

``` yaml
database:
  type: sqlserver
  host: sqlserver.example.com
  port: 1433          # default SQL Server port
  name: HLabMonitor
  username: hlabmonitor_user
  password: SecurePassword123!
```

See [application-yaml.md](application-yaml.md#sql-server---full) for more examples.

### Docker Compose Example

``` yaml
version: '3.8'

services:
  sqlserver:
    image: mcr.microsoft.com/mssql/server:2022-latest
    environment:
      ACCEPT_EULA: "Y"
      SA_PASSWORD: "YourStrong@Passw0rd"
      MSSQL_PID: "Express"
    volumes:
      - sqlserver_data:/var/opt/mssql
    ports:
      - "1433:1433"
  
  hlabmonitor:
    image: wiserisk/hlabmonitor:latest
    depends_on:
      - sqlserver
    environment:
      DATABASE_TYPE: sqlserver
      DATABASE_HOST: sqlserver
      DATABASE_NAME: HLabMonitor
      DATABASE_USERNAME: sa
      DATABASE_PASSWORD: "YourStrong@Passw0rd"
    ports:
      - "8080:8080"

volumes:
  sqlserver_data:
```

> 💡 **Note**: For production, create a dedicated user instead of using `sa`.

### Troubleshooting

| Issue | Likely Cause | First Steps |
|-------|--------------|-------------|
| `Connection refused` | SQL Server not running or TCP disabled | Enable TCP/IP protocol in SQL Server Configuration Manager |
| `Login failed for user` | Wrong credentials or user not configured | Verify username/password and SQL Server authentication mode |
| `Cannot open database` | Database doesn't exist or user lacks access | Create database and grant permissions |
| `SSL/TLS handshake failed` | Certificate validation issues | Add `encrypt=false` to connection string for testing (not production) |

> 🔗 **SQL Server Issues**: For SQL Server-specific problems (installation, clustering, performance), consult [Microsoft SQL Server Documentation](https://docs.microsoft.com/sql/sql-server/).

> 🐛 **HLabMonitor Integration Issues**: If configuration is correct but HLabMonitor cannot connect, [open an issue](https://github.com/amaurydetremerie/HLabMonitor/issues).

---

## Schema Management

HLabMonitor uses **Liquibase** for database schema management. All migrations are applied automatically on startup.

### What This Means
- ✅ **No manual SQL scripts** required
- ✅ **Automatic schema creation** on first run
- ✅ **Safe upgrades** between HLabMonitor versions
- ✅ **Rollback support** (if needed)

### Migration Logs

Schema changes are logged during application startup:

``` log
INFO  [main] liquibase.changelog : Reading from hlabmonitor.databasechangelog
INFO  [main] liquibase.changelog : Running Changeset: db/changelog/001-initial-schema.sql
INFO  [main] liquibase.changelog : Successfully applied changeset
```

### Disabling Liquibase (Advanced)

For development or custom scenarios:

``` yaml
spring:
  liquibase:
    enabled: false
```

> ⚠️ **Warning**: Disabling Liquibase means you must manage the schema manually.

---

## Environment Variables

You can override database configuration using environment variables, useful for Docker and Kubernetes deployments:

``` bash
DATABASE_TYPE=postgresql
DATABASE_HOST=postgres.local
DATABASE_PORT=5432
DATABASE_NAME=hlabmonitor
DATABASE_USERNAME=monitor_user
DATABASE_PASSWORD=secure_password
```

See [Environment Variables](environment-variables.md) documentation for complete reference.

---

## Backup and Restore

### SQLite
Simple file copy (see [SQLite section](#backup) above).

### PostgreSQL
Use `pg_dump` and `pg_restore`:

``` bash
# Backup
pg_dump -h postgres.example.com -U hlabmonitor_user hlabmonitor > backup.sql

# Restore
psql -h postgres.example.com -U hlabmonitor_user hlabmonitor < backup.sql
```

📚 See [PostgreSQL Backup Documentation](https://www.postgresql.org/docs/current/backup.html)

### SQL Server
Use SQL Server backup tools:

``` sql
-- Backup
BACKUP DATABASE HLabMonitor TO DISK = 'C:\Backups\hlabmonitor.bak';

-- Restore
RESTORE DATABASE HLabMonitor FROM DISK = 'C:\Backups\hlabmonitor.bak';
```

📚 See [SQL Server Backup Documentation](https://docs.microsoft.com/sql/relational-databases/backup-restore/)

---

## Performance Considerations

### For Small Deployments (<50 targets)
- **SQLite** or **H2** is sufficient
- Minimal resource overhead

### For Medium Deployments (50-500 targets)
- **PostgreSQL** recommended
- Standard configuration works well
- Consider connection pooling tuning

### For Large Deployments (>500 targets)
- **PostgreSQL** required
- Tune connection pool size
- Monitor database performance metrics
- Consider read replicas for reporting

---

## Getting Help

### Database-Specific Issues
For issues with database installation, configuration, or operation **outside of HLabMonitor integration**:

- **PostgreSQL**: [PostgreSQL Mailing Lists](https://www.postgresql.org/list/), [Stack Overflow](https://stackoverflow.com/questions/tagged/postgresql)
- **SQL Server**: [Microsoft Q&A](https://docs.microsoft.com/answers/products/sql-server), [Stack Overflow](https://stackoverflow.com/questions/tagged/sql-server)
- **SQLite**: [SQLite Forum](https://sqlite.org/forum/), [Stack Overflow](https://stackoverflow.com/questions/tagged/sqlite)

### HLabMonitor Integration Issues
If your database is properly configured but HLabMonitor cannot connect or operate correctly:

1. Enable debug logging:
``` yaml
logging:
  level:
    be.wiserisk.hlabmonitor: DEBUG
    org.springframework.jdbc: DEBUG
```

2. Check HLabMonitor logs for specific error messages

3. [Open an issue](https://github.com/amaurydetremerie/HLabMonitor/issues) with:
  - Database type and version
  - HLabMonitor version
  - Configuration (sanitized, no passwords)
  - Relevant log excerpts

---

## Next Steps

- 📝 Review [Application YAML Reference](application-yaml.md) for configuration syntax
- 🐳 See [Docker Compose Examples](../deployment/docker-compose.md) for complete deployment examples
- ☸️ Check [Kubernetes Manifests](../deployment/kubernetes-manifests.md) for orchestrated deployments
