# Production Checklist

This checklist helps ensure HLabMonitor is properly configured and ready for production deployment. While "production" might mean different things (enterprise datacenter vs. homelab), these guidelines help you run a reliable monitoring system.

> 💡 **Note**: Not every item applies to every deployment. Use your judgment based on your environment and requirements.

---

## Pre-Deployment

### Database Selection

- [ ] **Database type chosen** based on your needs:
    - [ ] H2: ❌ Not for production (in-memory, no persistence)
    - [ ] SQLite: ✅ Single-instance homelabs (<100 targets)
    - [ ] PostgreSQL: ✅ Recommended for production/scaling
    - [ ] SQL Server: ✅ Windows environments/enterprise

- [ ] **Database server provisioned** (if using PostgreSQL/SQL Server)
    - [ ] Database created
    - [ ] User/credentials configured
    - [ ] Appropriate permissions granted
    - [ ] Network connectivity verified

- [ ] **Persistent storage configured** (if using SQLite)
    - [ ] Volume/directory created
    - [ ] Appropriate permissions set
    - [ ] Sufficient disk space allocated (5GB+ recommended)

### Configuration Review

- [ ] **Monitoring targets defined** in `application.yaml`
    - [ ] All targets are valid and reachable
    - [ ] Intervals appropriate for your use case
    - [ ] SSL certificate monitoring configured (if needed)

- [ ] **Database connection tested**
    - [ ] Connection string validated
    - [ ] Credentials verified
    - [ ] Network access confirmed

- [ ] **Debug features disabled**
    - [ ] `debug.controller.enabled: false` (or not set)

- [ ] **Logging configured appropriately**
    - [ ] Log level set (INFO for production)
    - [ ] Debug logging only for necessary packages

### Resource Planning

- [ ] **System requirements verified**
    - [ ] CPU: Minimum 250m, recommended 500m+ (Kubernetes) or 1 vCPU (VM)
    - [ ] Memory: Minimum 512MB, recommended 1GB+ for moderate loads
    - [ ] Disk: 5-10GB for SQLite, depends on monitoring data retention

- [ ] **Resource limits configured** (Kubernetes/Docker)
    - [ ] Memory limits set to prevent OOM
    - [ ] CPU limits configured (if needed)
    - [ ] JVM heap size tuned (via `JAVA_OPTS` if needed)

### Network & DNS

- [ ] **Network connectivity verified**
    - [ ] Application can reach database
    - [ ] Application can reach monitoring targets
    - [ ] ICMP (ping) allowed if using ping monitoring
    - [ ] HTTP/HTTPS egress allowed

- [ ] **DNS resolution working**
    - [ ] Internal hostnames resolve (if used)
    - [ ] External hostnames resolve (if monitoring external sites)

- [ ] **Port access configured**
    - [ ] Port 8080 (or custom) accessible
    - [ ] Firewall rules configured (if applicable)

---

## Security

### Secrets Management

- [ ] **Database credentials secured**
    - [ ] ❌ Not hardcoded in configuration files
    - [ ] ✅ Using environment variables or secrets management
    - [ ] ✅ Strong passwords used (PostgreSQL/SQL Server)
    - [ ] ✅ Secrets stored in vault/encrypted (Kubernetes Secrets, Docker Secrets, etc.)

- [ ] **Configuration files protected**
    - [ ] `application.yaml` has restricted permissions (600 or 644)
    - [ ] `.env` files not committed to version control
    - [ ] Secrets excluded from logs

### Access Control

- [ ] **Application running as non-root**
    - [ ] Docker: ✅ Already configured (UID 1001)
    - [ ] Systemd/bare metal: Dedicated user created

- [ ] **Database user has minimal permissions**
    - [ ] Only necessary privileges granted
    - [ ] Not using `root`/`sa` in production (create dedicated user)

- [ ] **Network security configured**
    - [ ] Firewall rules limit access to necessary ports
    - [ ] Database not exposed to internet (unless required)
    - [ ] Kubernetes NetworkPolicies configured (if applicable)

### Updates & Patching

- [ ] **Update strategy defined**
    - [ ] Process for updating HLabMonitor documented
    - [ ] Process for updating database documented
    - [ ] Testing procedure before production updates

- [ ] **Image versions pinned** (recommended)
    - [ ] Not using `:latest` in production
    - [ ] Using specific version tags (e.g., `1.0.0`)

---

## High Availability (Optional)

### Application HA

- [ ] **Multiple replicas configured** (if using PostgreSQL/SQL Server)
    - [ ] At least 2 replicas for redundancy
    - [ ] Pod anti-affinity configured (Kubernetes)
    - [ ] Load balancing configured

- [ ] **Health checks configured**
    - [ ] Liveness probe: `/actuator/health/liveness`
    - [ ] Readiness probe: `/actuator/health/readiness`
    - [ ] Appropriate timeouts and thresholds set

- [ ] **Rolling updates configured**
    - [ ] `maxUnavailable` and `maxSurge` set appropriately
    - [ ] PodDisruptionBudget configured (Kubernetes)

### Database HA

- [ ] **Database backup strategy implemented** (see [Backup & Recovery](#backup--recovery))

- [ ] **Database replication considered** (for high-criticality)
    - [ ] PostgreSQL replication configured (if needed)
    - [ ] SQL Server Always On configured (if needed)

---

## Monitoring & Observability

### Logging

- [ ] **Log aggregation configured** (recommended)
    - [ ] Logs shipped to centralized system (ELK, Loki, etc.)
    - [ ] Log rotation configured
    - [ ] Log retention policy defined

- [ ] **Log level appropriate**
    - [ ] Production: INFO or WARN
    - [ ] Troubleshooting: DEBUG (temporarily)

- [ ] **Structured logging considered** (if using log aggregation)

### Metrics

- [ ] **Metrics endpoint accessible**
    - [ ] `/actuator/metrics` reachable
    - [ ] `/actuator/prometheus` exposed (if using Prometheus)

- [ ] **Metrics collection configured** (recommended)
    - [ ] Prometheus ServiceMonitor created (Kubernetes)
    - [ ] Grafana dashboard configured
    - [ ] Key metrics monitored:
        - [ ] Application uptime
        - [ ] JVM memory usage
        - [ ] Database connection pool
        - [ ] Monitoring check success rate

### Alerting

- [ ] **Alerting rules configured** (recommended)
    - [ ] Application down alerts
    - [ ] High memory/CPU alerts
    - [ ] Database connection failures
    - [ ] Monitoring target failures (critical targets)

- [ ] **Alert destinations configured**
    - [ ] Email, Slack, PagerDuty, etc.
    - [ ] On-call rotation defined (if applicable)

### Health Endpoints

- [ ] **Health endpoints tested**
    - [ ] `/actuator/health` returns 200 when healthy
    - [ ] `/actuator/health/liveness` works
    - [ ] `/actuator/health/readiness` works

---

## Backup & Recovery

### Database Backup

- [ ] **Backup strategy implemented**
    - [ ] Automated daily backups configured
    - [ ] Backup retention policy defined (e.g., 7 days, 4 weeks, 6 months)
    - [ ] Backups stored securely (offsite/different storage)

- [ ] **Backup methods configured** based on database:
    - **SQLite**:
        - [ ] File-based backup (simple copy)
        - [ ] Volume snapshots (if supported)
    - **PostgreSQL**:
        - [ ] `pg_dump` scheduled (via cron/CronJob)
        - [ ] WAL archiving considered (for PITR)
    - **SQL Server**:
        - [ ] SQL Server backup jobs configured
        - [ ] Transaction log backups (if needed)

- [ ] **Backup monitoring configured**
    - [ ] Backup success/failure alerts
    - [ ] Backup size monitoring

### Restore Testing

- [ ] **Restore procedure documented**
    - [ ] Step-by-step restore instructions written
    - [ ] Recovery time objective (RTO) defined

- [ ] **Restore tested** (critical!)
    - [ ] Successful restore performed in test environment
    - [ ] Restore verified (data integrity checked)
    - [ ] Restore time measured

### Configuration Backup

- [ ] **Configuration files backed up**
    - [ ] `application.yaml` versioned (Git recommended)
    - [ ] Deployment manifests versioned (Docker Compose, Kubernetes)
    - [ ] Environment-specific configs documented

---

## Performance

### Application Tuning

- [ ] **JVM heap size configured** (if needed)
    - [ ] `-Xmx` and `-Xms` set via `JAVA_OPTS`
    - [ ] Appropriate for monitoring load (default 75% of container memory)

- [ ] **Resource limits appropriate**
    - [ ] Not too restrictive (causing OOMKilled/throttling)
    - [ ] Not too generous (wasting resources)

### Database Performance

- [ ] **Connection pooling configured** (PostgreSQL/SQL Server)
    - [ ] Pool size appropriate for load
    - [ ] Default HikariCP settings reviewed

- [ ] **Database indexes verified** (auto-created by Liquibase)

- [ ] **Database maintenance scheduled**
    - [ ] PostgreSQL: `VACUUM` and `ANALYZE` scheduled
    - [ ] SQL Server: Index maintenance configured

### Monitoring Load

- [ ] **Check intervals optimized**
    - [ ] Not too aggressive (causing load)
    - [ ] Not too infrequent (missing issues)
    - [ ] Typical: 1-5 minutes for critical, 5-15 for non-critical

- [ ] **Number of targets reasonable**
    - [ ] <50 targets: SQLite sufficient
    - [ ] 50-500 targets: PostgreSQL recommended
    - [ ] >500 targets: PostgreSQL with tuning required

---

## Maintenance

### Update Strategy

- [ ] **Update process defined**
    - [ ] Testing in non-production first
    - [ ] Rollback plan documented
    - [ ] Maintenance window scheduled (if needed)

- [ ] **Update notifications configured**
    - [ ] Watching GitHub releases
    - [ ] Docker Hub notifications enabled (optional)

### Rollback Plan

- [ ] **Rollback procedure documented**
    - [ ] How to revert to previous version
    - [ ] Database migration rollback (if needed)
    - [ ] Estimated rollback time

- [ ] **Previous version available**
    - [ ] Old container images tagged/saved
    - [ ] Old binaries archived (if not containerized)

### Documentation

- [ ] **Deployment documented**
    - [ ] Architecture diagram created
    - [ ] Configuration parameters documented
    - [ ] Network topology documented

- [ ] **Runbook created**
    - [ ] Common operational tasks documented
    - [ ] Troubleshooting procedures written
    - [ ] Contact information/escalation path defined

---

## Post-Deployment

### Verification

- [ ] **Application accessible**
    - [ ] Web UI loads (if applicable)
    - [ ] API endpoints respond
    - [ ] Swagger UI accessible: `/swagger-ui.html`

- [ ] **Health checks passing**
    - [ ] `/actuator/health` returns 200 OK
    - [ ] All health indicators show "UP"

- [ ] **Database connection verified**
    - [ ] Application connected to database
    - [ ] Schema migrations applied
    - [ ] Tables created

### Functional Testing

- [ ] **Monitoring checks working**
    - [ ] Ping checks executing
    - [ ] HTTP checks executing
    - [ ] Certificate checks executing (if configured)
    - [ ] Check results visible in database/API

- [ ] **Critical targets monitored**
    - [ ] All critical infrastructure monitored
    - [ ] Alerts trigger correctly (test with intentional failure)

### Performance Validation

- [ ] **Resource usage acceptable**
    - [ ] CPU usage within limits
    - [ ] Memory usage stable (no leaks)
    - [ ] Database connections stable

- [ ] **Response times acceptable**
    - [ ] API responds quickly (<500ms typical)
    - [ ] UI loads quickly (if applicable)

### Documentation

- [ ] **Deployment documented**
    - [ ] Deployment date recorded
    - [ ] Version deployed documented
    - [ ] Configuration snapshot saved

- [ ] **Team notified**
    - [ ] Operations team informed
    - [ ] Documentation shared
    - [ ] Access credentials distributed (securely)

---

## Quick Reference Checklists

### Minimal Production (Homelab)

Essential items for a basic reliable deployment:

- [x] SQLite or PostgreSQL database
- [x] Persistent storage configured
- [x] Database credentials in environment variables
- [x] Daily automated backups
- [x] Health checks configured
- [x] Monitoring targets defined
- [x] Restore tested once

### Standard Production

Additional items for standard production:

- [x] All items from Minimal Production
- [x] PostgreSQL database (not SQLite)
- [x] Multiple replicas (2+)
- [x] Centralized logging
- [x] Metrics collection (Prometheus)
- [x] Alerting configured
- [x] Network security (firewall/NetworkPolicy)
- [x] Backup monitoring
- [x] Documentation complete

### High Availability Production

Additional items for mission-critical deployments:

- [x] All items from Standard Production
- [x] 3+ replicas with anti-affinity
- [x] Database replication (if critical)
- [x] Load balancing
- [x] Automated failover
- [x] Geographic redundancy (if needed)
- [x] 24/7 on-call rotation
- [x] Disaster recovery plan tested
- [x] SLA defined and monitored

---

## Environment-Specific Considerations

### Docker Compose

- [ ] **Named volumes used** (not bind mounts for database)
- [ ] **Health checks in docker-compose.yml**
- [ ] **Restart policy: `unless-stopped` or `always`**
- [ ] **Resource limits configured** (deploy.resources)
- [ ] **Secrets via `.env` file** (not committed to Git)

### Kubernetes

- [ ] **Namespace created** (not `default`)
- [ ] **ConfigMaps for configuration**
- [ ] **Secrets for credentials**
- [ ] **PersistentVolumeClaims for data**
- [ ] **Ingress/Service configured**
- [ ] **Resource requests/limits set**
- [ ] **Probes configured** (liveness/readiness)
- [ ] **NetworkPolicy configured** (recommended)

### Bare Metal / VM

- [ ] **Systemd service created**
- [ ] **Service enabled** (`systemctl enable`)
- [ ] **Log rotation configured** (`/etc/logrotate.d/`)
- [ ] **Firewall rules applied** (`ufw`, `firewalld`)
- [ ] **Dedicated user created** (not running as root)
- [ ] **Application directory secured** (appropriate permissions)

---

## Common Pitfalls to Avoid

### ❌ Don't Do This

- **Using H2 in production** - Data is lost on restart
- **Hardcoding passwords** - Security risk
- **Running as root** - Security risk
- **No backups** - Recipe for disaster
- **Using `:latest` tags** - Unpredictable updates
- **Single replica with PostgreSQL** - Wasted opportunity for HA
- **No health checks** - Failed deployments go unnoticed
- **Ignoring resource limits** - OOMKilled or resource exhaustion
- **No monitoring of the monitor** - Ironic failure
- **Never testing restores** - Backups that don't work

### ✅ Do This Instead

- **Use PostgreSQL or SQLite** (with backups)
- **Use environment variables/secrets** for credentials
- **Run as non-root** (UID 1001 in containers)
- **Implement automated backups** and test restores
- **Pin specific versions** (e.g., `1.0.0`)
- **Use 2+ replicas** for high availability
- **Configure liveness/readiness probes**
- **Set appropriate resource requests/limits**
- **Monitor HLabMonitor** with external tools (Prometheus, uptime checks)
- **Test restores regularly** (quarterly recommended)

---

## Troubleshooting Production Issues

### Application Won't Start

1. Check logs: `docker logs` / `kubectl logs` / `journalctl -u hlabmonitor`
2. Verify database connectivity
3. Check configuration file syntax
4. Verify resource availability (memory/CPU)

### High Memory Usage

1. Check metrics: `/actuator/metrics/jvm.memory.used`
2. Adjust JVM heap: `JAVA_OPTS="-Xmx512m"`
3. Increase container limits
4. Review number of monitoring targets

### Database Issues

1. Verify database is running
2. Test connectivity: `psql` / `sqlcmd` / `sqlite3`
3. Check credentials
4. Review connection pool settings
5. Check disk space (especially SQLite)

### Monitoring Not Working

1. Verify network connectivity to targets
2. Check firewall rules (ICMP for ping, HTTP/HTTPS for web)
3. Review target configuration in `application.yaml`
4. Check application logs for errors

---

## Getting Help

- **Documentation**: [docs/](../)
- **Issues**: [GitHub Issues](https://github.com/amaurydetremerie/HLabMonitor/issues)
- **Discussions**: [GitHub Discussions](https://github.com/amaurydetremerie/HLabMonitor/discussions)

When reporting production issues:
1. Sanitize logs (remove passwords/sensitive data)
2. Include HLabMonitor version
3. Include database type and version
4. Include deployment method (Docker/Kubernetes/bare metal)
5. Describe expected vs. actual behavior

---

## See Also

- [Application YAML Reference](../configuration/application-yaml.md)
- [Database Setup](../configuration/database.md)
- [Environment Variables](../configuration/environment-variables.md)
- [Docker Compose Examples](docker-compose.md)
- [Kubernetes Manifests](kubernetes-manifests.md)
