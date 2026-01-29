# Common Issues

This document tracks known issues and their solutions. Updated as users report problems.

---

## Application Won't Start

### Port Already in Use

**Symptom:**
``` text
Web server failed to start. Port 8080 was already in use.
```

**Solution:**
``` bash
# Change port
java -jar hlabmonitor.jar --server.port=8081

# Or in application.yaml
server:
    port: 8081
```

### Database Connection Failed

**Symptom:**
``` text
Failed to configure a DataSource
```

**Solution:**
- Check database configuration in `application.yaml`
- Verify database is running and accessible
- Check credentials and connection string

---

## Monitoring Issues

### Checks Not Executing

**Symptom:**
Targets configured but no check results appearing.

**Possible Causes:**
1. Scheduler not configured properly
2. Targets misconfigured
3. Network connectivity issues

**Debug Steps:**
``` bash
# Enable debug logging
LOGGING_LEVEL_BE_WISERISK_HLABMONITOR=DEBUG

# Check scheduler status
curl http://localhost:8080/actuator/scheduledtasks

# Verify target configuration
curl http://localhost:8080/api/v1/monitoring/targets
```

---

## Docker Issues

### Container Exits Immediately

**Debug:**
``` bash
# Check logs
docker logs hlabmonitor

# Run with interactive shell
docker run -it --rm hlabmonitor:latest /bin/bash
```

---

## Performance Issues

### High Memory Usage

**Investigate:**
``` bash
# Check JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/jvm.memory.max

# Heap dump if needed
jmap -dump:live,format=b,file=heap.bin <PID>
```

---

## Database Issues

### Migration Failures

**Symptom:**
``` text
Liquibase migration failed
```

**Solution:**
Check `docs/configuration/database.md` for migration troubleshooting.

---

## Need Help?

If your issue isn't listed here:

1. Check logs: `docker logs hlabmonitor` or application logs
2. Search [GitHub Issues](https://github.com/amaurydetremerie/HLabMonitor/issues)
3. Create new issue with:
    - HLabMonitor version
    - Environment (Docker, Kubernetes, bare metal)
    - Configuration (sanitized)
    - Full error message and logs
    - Steps to reproduce

---

## See Also

- [FAQ](faq.md)
- [Configuration Reference](../configuration/application-yaml.md)
- [Logging Guide](../monitoring/logs.md)
