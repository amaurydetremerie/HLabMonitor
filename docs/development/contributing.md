# Contributing Guide

Contributions are welcome! This guide covers the essentials for contributing to HLabMonitor.

---

## Getting Started

``` bash
# Fork and clone
git clone https://github.com/YOUR_USERNAME/HLabMonitor.git
cd HLabMonitor

# Add upstream
git remote add upstream https://github.com/amaurydetremerie/HLabMonitor.git

# Create feature branch
git checkout -b feature/your-feature-name
```

---

## Development Workflow

### 1. Make Changes

``` bash
# Keep your branch up to date
git fetch upstream
git rebase upstream/main

# Make your changes
# ... edit files ...

# Build and test
mvn clean install
```

### 2. Commit

Follow [Conventional Commits](https://www.conventionalcommits.org/):

``` bash
# Format: <type>(<scope>): <subject>

# Examples:
git commit -m "feat(monitoring): add DNS check support"
git commit -m "fix(database): resolve connection pool leak"
git commit -m "docs(readme): update installation instructions"
git commit -m "refactor(scheduler): simplify check execution logic"
git commit -m "test(http): add certificate expiry tests"
```

**Commit Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation only
- `style` - Code style (formatting, no logic change)
- `refactor` - Code refactoring
- `perf` - Performance improvement
- `test` - Add or update tests
- `chore` - Build process, dependencies, tooling

### 3. Push and Create PR

``` bash
# Push to your fork
git push origin feature/your-feature-name

# Create Pull Request on GitHub
# Title: Same format as commit message
# Description: What, why, how
```

---

## Code Standards

### Java Style

Follow standard Java conventions:

``` java
// Package naming
be.wiserisk.hlabmonitor.domain.service

// Class naming - PascalCase
public class PingCheckService { }

// Method naming - camelCase
public CheckResult executeCheck(MonitoringTarget target) { }

// Constants - UPPER_SNAKE_CASE
public static final int DEFAULT_TIMEOUT = 5000;

// Variables - camelCase
private final CheckResultRepository repository;
```

### Spring Boot Conventions

``` java
// Use constructor injection (preferred)
@Service
public class MonitoringService {
    private final CheckResultRepository repository;
    
    public MonitoringService(CheckResultRepository repository) {
        this.repository = repository;
    }
}

// Component annotations
@RestController  // REST endpoints
@Service         // Business logic
@Repository      // Data access
@Component       // Generic components
@Configuration   // Configuration classes

// Use @Slf4j for logging
@Slf4j
@Service
public class PingCheckService {
    public void execute() {
        log.info("Executing ping check");
        log.debug("Details: {}", details);
        log.error("Failed to execute", exception);
    }
}
```

### Documentation

``` java
/**
 * Executes a ping check against the specified target.
 * 
 * @param target the monitoring target to check
 * @return check result with status and latency
 * @throws NetworkException if network operation fails
 */
public CheckResult executePing(MonitoringTarget target) {
    // Implementation
}
```

---

## Testing Requirements

### Write Tests for New Features

``` java
@SpringBootTest
class PingCheckServiceTest {
    
    @Autowired
    private PingCheckService pingCheckService;
    
    @Test
    void shouldReturnSuccessForReachableHost() {
        // Given
        MonitoringTarget target = new MonitoringTarget("google", "8.8.8.8");
        
        // When
        CheckResult result = pingCheckService.executePing(target);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(CheckStatus.SUCCESS);
        assertThat(result.getLatencyMs()).isGreaterThan(0);
    }
    
    @Test
    void shouldReturnFailureForUnreachableHost() {
        // Given
        MonitoringTarget target = new MonitoringTarget("invalid", "192.0.2.1");
        
        // When
        CheckResult result = pingCheckService.executePing(target);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(CheckStatus.FAILURE);
        assertThat(result.getErrorMessage()).isNotEmpty();
    }
}
```

### Test Coverage

``` bash
# Run tests with coverage
mvn clean test jacoco:report

# Aim for >80% coverage on new code
# View report: target/site/jacoco/index.html
```

### Integration Tests

``` java
@SpringBootTest
@AutoConfigureTestDatabase
@Sql(scripts = "/test-data.sql")
class MonitoringServiceIntegrationTest {
    
    @Autowired
    private MonitoringService monitoringService;
    
    @Autowired
    private CheckResultRepository repository;
    
    @Test
    void shouldPersistCheckResults() {
        // Test with real database (H2 in-memory)
    }
}
```

---

## Pull Request Guidelines

### Before Submitting

- [ ] Code builds without errors: `mvn clean install`
- [ ] All tests pass: `mvn test`
- [ ] New features have tests
- [ ] Documentation updated (if needed)
- [ ] Commit messages follow conventions
- [ ] No merge conflicts with `main`

### PR Title and Description

**Title:** Follow commit message format
``` text
feat(http): add custom headers support
```

**Description Template:**
``` markdown
## What
Brief description of changes

## Why
Problem being solved or feature rationale

## How
Technical approach taken

## Testing
How to test the changes

## Checklist
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] Breaking changes noted
```

### PR Size

Keep PRs focused and reasonably sized:
- ✅ **Small**: <200 lines changed
- ⚠️ **Medium**: 200-500 lines changed
- ❌ **Large**: >500 lines (consider splitting)

---

## Code Review Process

### Reviewer Guidelines

Reviewers check for:
- Code correctness and logic
- Test coverage
- Code style and readability
- Performance implications
- Security concerns
- Documentation completeness

### Addressing Feedback

``` bash
# Make requested changes
git add .
git commit -m "fix: address review comments"
git push origin feature/your-feature-name

# Force push if rebasing (use with caution)
git rebase -i HEAD~3
git push origin feature/your-feature-name --force-with-lease
```

### Approval and Merge

- Requires 1 approval from maintainer
- All CI checks must pass
- Merge using "Squash and merge" (default)

---

## Architecture Guidelines

### Hexagonal Architecture

Follow the ports and adapters pattern:

``` text
domain/              # Business logic (no external dependencies)
├── model/          # Entities and value objects
├── port/           # Interfaces (ports)
│   ├── in/        # Inbound ports (use cases)
│   └── out/       # Outbound ports (repositories, external services)
└── service/       # Domain services

application/        # Application logic
└── service/       # Orchestrates domain services

infrastructure/     # External adapters
├── adapter/
│   ├── persistence/  # Database implementations
│   ├── network/      # Network implementations
│   └── metrics/      # Metrics implementations
└── config/           # Infrastructure configuration

presentation/       # API layer
├── controller/     # REST controllers
├── dto/           # Data transfer objects
└── mapper/        # DTO ↔ Domain mappers
```

### Dependency Rules

- **Domain** - No external dependencies (pure Java)
- **Application** - Depends on Domain only
- **Infrastructure** - Implements ports from Domain
- **Presentation** - Depends on Application and Domain

``` java
// ✅ Good: Domain depends on nothing
package be.wiserisk.hlabmonitor.domain.service;

public class PingCheckService {
    public CheckResult execute(MonitoringTarget target) {
        // Pure business logic
    }
}

// ✅ Good: Infrastructure implements domain port
package be.wiserisk.hlabmonitor.infrastructure.adapter.network;

@Component
public class PingAdapter implements PingPort {
    @Override
    public boolean isReachable(String host, int timeout) {
        // Implementation using Java networking
    }
}

// ❌ Bad: Domain depending on infrastructure
package be.wiserisk.hlabmonitor.domain.service;

import org.springframework.stereotype.Service; // ❌ Spring annotation in domain

@Service // ❌ Framework dependency in domain
public class PingCheckService { }
```

---

## Adding New Features

### New Monitoring Check Type

Example: Adding a DNS check

1. **Domain Model**

``` java
// domain/model/DnsCheckTarget.java
public class DnsCheckTarget extends MonitoringTarget {
    private final String recordType; // A, AAAA, MX, etc.
    
    // Constructor, getters
}
```

2. **Domain Port**

``` java
// domain/port/out/DnsPort.java
public interface DnsPort {
    DnsResponse query(String hostname, String recordType);
}
```

3. **Domain Service**

``` java
// domain/service/DnsCheckService.java
public class DnsCheckService {
    private final DnsPort dnsPort;
    
    public CheckResult executeCheck(DnsCheckTarget target) {
        DnsResponse response = dnsPort.query(target.getTarget(), target.getRecordType());
        return CheckResult.success(target, response.getResponseTime());
    }
}
```

4. **Infrastructure Adapter**

``` java
// infrastructure/adapter/network/DnsAdapter.java
@Component
public class DnsAdapter implements DnsPort {
    @Override
    public DnsResponse query(String hostname, String recordType) {
        // Implementation using dnsjava or native Java
    }
}
```

5. **Configuration**

``` yaml
# Add to application.yaml schema
monitoring:
  dns:
    cloudflare:
      target: cloudflare.com
      record-type: A
      interval: 5m
```

6. **Tests**

``` java
@Test
void shouldResolveDnsRecord() {
    DnsCheckTarget target = new DnsCheckTarget("cloudflare", "cloudflare.com", "A");
    CheckResult result = dnsCheckService.executeCheck(target);
    assertThat(result.getStatus()).isEqualTo(CheckStatus.SUCCESS);
}
```

### New Database Support

Example: Adding MySQL support

1. Add dependency in `pom.xml`:

``` xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

2. Update `DatabaseConfig`:

``` java
public enum DatabaseType {
    H2, SQLITE, POSTGRESQL, SQLSERVER, MYSQL
}

// Add MySQL configuration
if (type == DatabaseType.MYSQL) {
    return DataSourceBuilder.create()
        .driverClassName("com.mysql.cj.jdbc.Driver")
        .url("jdbc:mysql://" + host + ":" + port + "/" + name)
        .username(username)
        .password(password)
        .build();
}
```

3. Document in `docs/configuration/database.md`

---

## Documentation

### When to Update Documentation

- New features → Update relevant guides
- Configuration changes → Update `application-yaml.md`
- API changes → Update API documentation
- Breaking changes → Update migration guide

### Documentation Structure

``` text
docs/
├── configuration/        # Configuration guides
├── deployment/          # Deployment guides
├── monitoring/          # Observability
├── development/         # Development guides (this folder)
└── troubleshooting/     # Common issues
```

---

## Release Process

Handled by maintainers, but FYI:

1. **Version bump** in `pom.xml`
2. **Tag release**: `git tag v1.0.0`
3. **Push tag**: `git push origin v1.0.0`
4. **GitHub Actions** builds and pushes Docker images
5. **GitHub Release** created with changelog

---

## Getting Help

- **Questions**: [GitHub Discussions](https://github.com/amaurydetremerie/HLabMonitor/discussions)
- **Bugs**: [GitHub Issues](https://github.com/amaurydetremerie/HLabMonitor/issues)
- **Security**: Email maintainer directly

---

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Assume good intentions
- Help others learn

---

## Common Scenarios

### Adding a Configuration Property

``` java
// 1. Add to config class
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringConfig {
    private Duration defaultTimeout = Duration.ofSeconds(5);
    // getter, setter
}

// 2. Document in docs/configuration/application-yaml.md

// 3. Add environment variable mapping in docs/configuration/environment-variables.md
// MONITORING_DEFAULT_TIMEOUT=10s
```

### Adding a REST Endpoint

``` java
// 1. Create DTO
public record TargetStatsResponse(
    String name,
    long totalChecks,
    long successCount,
    double successRate
) {}

// 2. Create controller method
@GetMapping("/targets/{name}/stats")
public ResponseEntity<TargetStatsResponse> getStats(@PathVariable String name) {
    // Implementation
}

// 3. Add tests
// 4. Update API documentation if needed
```

### Adding a Liquibase Migration

``` yaml
# src/main/resources/db/changelog/v1.1/004-add-dns-support.yaml
databaseChangeLog:
  - changeSet:
      id: 004-add-dns-support
      author: your-name
      changes:
        - addColumn:
            tableName: monitoring_targets
            columns:
              - column:
                  name: record_type
                  type: VARCHAR(10)
                  constraints:
                    nullable: true
      rollback:
        - dropColumn:
            tableName: monitoring_targets
            columnName: record_type
```

Include in `db/changelog/db.changelog-master.yaml`:

``` yaml
- include:
    file: db/changelog/v1.1/004-add-dns-support.yaml
```

---

## See Also

- [Architecture Overview](architecture.md)
- [Building from Source](building.md)
- [Configuration Reference](../configuration/application-yaml.md)
