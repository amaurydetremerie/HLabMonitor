# Architecture Overview

HLabMonitor is built using modern Java and Spring Boot, following hexagonal (ports and adapters) architecture principles for maintainability and testability.

---

## High-Level Architecture

``` text
┌─────────────────────────────────────────────────────────────────┐
│                        Presentation Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ REST API     │  │  Actuator    │  │  Swagger UI  │          │
│  │ Controllers  │  │  Endpoints   │  │              │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Monitoring  │  │  Scheduling  │  │ Configuration│          │
│  │   Services   │  │   Services   │  │   Services   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────────┐
│                         Domain Layer                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   Entities   │  │  Value       │  │  Domain      │          │
│  │              │  │  Objects     │  │  Services    │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
│
▼
┌─────────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  Database    │  │   Network    │  │   Metrics    │          │
│  │  Adapters    │  │   Adapters   │  │   Adapters   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

---

## Technology Stack

### Core Framework

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Runtime** | Java (Eclipse Temurin / Amazon Corretto) | 21 | Application runtime |
| **Framework** | Spring Boot | 3.x | Application framework |
| **Build Tool** | Maven | 3.9+ | Dependency management & build |
| **Database Migration** | Liquibase | Latest | Schema versioning |

### Spring Boot Modules

- **Spring Web** - REST API endpoints
- **Spring Data JPA** - Database abstraction
- **Spring Boot Actuator** - Monitoring & health checks
- **Spring Boot Configuration Processor** - Type-safe configuration

### Database Support

- **H2** - In-memory (testing/demo only)
- **SQLite** - File-based (single-instance deployments)
- **PostgreSQL** - Recommended for production
- **Microsoft SQL Server** - Enterprise environments

### Observability

- **Micrometer** - Metrics collection
- **Prometheus** - Metrics export format
- **Logback** - Logging framework
- **Spring Boot Actuator** - Health & info endpoints

---

## Project Structure

``` text
hlabmonitor/
├── .github/
│   └── workflows/
│       ├── build.yml              # Build & test workflow
│       └── release.yml            # Release & Docker push workflow
│
├── docker/
│   ├── Dockerfile                 # Ubuntu-based image
│   ├── Dockerfile.alpine          # Alpine-based image
│   └── Dockerfile.corretto        # Amazon Corretto image
│
├── docs/
│   ├── configuration/             # Configuration guides
│   ├── deployment/                # Deployment guides
│   ├── monitoring/                # Monitoring documentation
│   ├── development/               # Development guides (this folder)
│   └── troubleshooting/           # Troubleshooting guides
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── be/wiserisk/hlabmonitor/
│   │   │       ├── HLabMonitorApplication.java
│   │   │       ├── config/        # Configuration classes
│   │   │       ├── domain/        # Domain models & business logic
│   │   │       ├── application/   # Application services
│   │   │       ├── infrastructure/# Infrastructure adapters
│   │   │       └── presentation/  # Controllers & DTOs
│   │   │
│   │   └── resources/
│   │       ├── application.yaml   # Default configuration
│   │       ├── db/
│   │       │   └── changelog/     # Liquibase migrations
│   │       └── static/            # Static resources
│   │
│   └── test/
│       ├── java/                  # Unit & integration tests
│       └── resources/             # Test resources
│
├── pom.xml                        # Maven configuration
├── README.md                      # Project overview
└── LICENSE                        # License file
```

---

## Package Structure

### Hexagonal Architecture Layers

``` text
be.wiserisk.hlabmonitor/
│
├── HLabMonitorApplication.java   # Main application class
│
├── config/                        # Configuration
│   ├── DatabaseConfig.java       # Database configuration
│   ├── SchedulingConfig.java     # Async & scheduling setup
│   ├── SecurityConfig.java       # Security configuration (if any)
│   └── WebConfig.java            # Web MVC configuration
│
├── domain/                        # Domain Layer (Core Business Logic)
│   ├── model/                    # Domain entities
│   │   ├── MonitoringTarget.java
│   │   ├── CheckResult.java
│   │   └── CheckStatus.java
│   ├── port/                     # Ports (interfaces)
│   │   ├── in/                   # Inbound ports (use cases)
│   │   │   ├── ExecuteCheckUseCase.java
│   │   │   └── GetCheckResultsUseCase.java
│   │   └── out/                  # Outbound ports (repositories)
│   │       ├── CheckResultRepository.java
│   │       └── MonitoringTargetRepository.java
│   └── service/                  # Domain services
│       ├── PingCheckService.java
│       ├── HttpCheckService.java
│       └── CertificateCheckService.java
│
├── application/                   # Application Layer (Use Case Implementation)
│   ├── service/                  # Application services
│   │   ├── MonitoringService.java
│   │   └── SchedulingService.java
│   └── scheduler/                # Scheduled tasks
│       ├── PingCheckScheduler.java
│       ├── HttpCheckScheduler.java
│       └── CertificateCheckScheduler.java
│
├── infrastructure/                # Infrastructure Layer (Adapters)
│   ├── adapter/
│   │   ├── persistence/          # Database adapters
│   │   │   ├── jpa/              # JPA repositories
│   │   │   │   ├── CheckResultJpaRepository.java
│   │   │   │   └── MonitoringTargetJpaRepository.java
│   │   │   └── entity/           # JPA entities
│   │   │       ├── CheckResultEntity.java
│   │   │       └── MonitoringTargetEntity.java
│   │   │
│   │   ├── network/              # Network adapters
│   │   │   ├── PingAdapter.java
│   │   │   ├── HttpAdapter.java
│   │   │   └── SslCertificateAdapter.java
│   │   │
│   │   └── metrics/              # Metrics adapters
│   │       └── MicrometerAdapter.java
│   │
│   └── config/                   # Infrastructure configuration
│       ├── database/             # Database-specific config
│       └── monitoring/           # Monitoring configuration
│
└── presentation/                  # Presentation Layer (API)
├── controller/               # REST controllers
│   ├── MonitoringController.java
│   ├── CheckResultController.java
│   └── HealthController.java
├── dto/                      # Data Transfer Objects
│   ├── request/
│   │   └── ExecuteCheckRequest.java
│   └── response/
│       ├── CheckResultResponse.java
│       └── TargetStatusResponse.java
└── mapper/                   # DTO ↔ Domain mappers
├── CheckResultMapper.java
└── MonitoringTargetMapper.java
```

---

## Core Components

### 1. Configuration Management

**Location:** `config/`

**Purpose:** Load and validate configuration from `application.yaml` and environment variables.

**Key Classes:**
- `DatabaseConfig` - Database type selection and connection setup
- `MonitoringConfig` - Load monitoring targets from YAML
- `SchedulingConfig` - Configure async executors and schedulers

**Example:**

``` java
@Configuration
@ConfigurationProperties(prefix = "database")
public class DatabaseConfig {
    private DatabaseType type = DatabaseType.H2;
    private String host = "localhost";
    private Integer port;
    private String name = "monitor";
    private String path;
    
    // Getters, setters, validation
}
```

### 2. Domain Model

**Location:** `domain/model/`

**Purpose:** Core business entities and value objects.

**Key Classes:**

``` java
@Entity
public class MonitoringTarget {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private TargetType type;  // PING, HTTP, CERTIFICATE
    private String target;
    private Duration interval;
    private LocalDateTime lastChecked;
    
    // Domain logic
}

@Entity
public class CheckResult {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private MonitoringTarget target;
    
    private CheckStatus status;  // SUCCESS, FAILURE
    private LocalDateTime timestamp;
    private Long latencyMs;
    private String errorMessage;
    
    // Domain logic
}
```

### 3. Monitoring Services

**Location:** `domain/service/` and `application/service/`

**Purpose:** Execute monitoring checks and record results.

**Key Services:**

``` java
@Service
public class PingCheckService {
    public CheckResult executePing(MonitoringTarget target) {
        try {
            InetAddress address = InetAddress.getByName(target.getTarget());
            long startTime = System.currentTimeMillis();
            boolean reachable = address.isReachable(5000);
            long latency = System.currentTimeMillis() - startTime;
            
            return CheckResult.success(target, latency);
        } catch (IOException e) {
            return CheckResult.failure(target, e.getMessage());
        }
    }
}

@Service
public class HttpCheckService {
    private final RestTemplate restTemplate;
    
    public CheckResult executeHttpCheck(MonitoringTarget target) {
        try {
            long startTime = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.getForEntity(
                target.getTarget(), String.class
            );
            long latency = System.currentTimeMillis() - startTime;
            
            return CheckResult.success(target, latency, response.getStatusCode());
        } catch (Exception e) {
            return CheckResult.failure(target, e.getMessage());
        }
    }
}
```

### 4. Scheduling

**Location:** `application/scheduler/`

**Purpose:** Schedule periodic monitoring checks.

**Implementation:**

``` java
@Component
public class PingCheckScheduler {
    private final MonitoringService monitoringService;
    private final TaskScheduler taskScheduler;
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void scheduleChecks() {
        List<MonitoringTarget> targets = monitoringService.getPingTargets();
        
        for (MonitoringTarget target : targets) {
            ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(
                () -> monitoringService.executeCheck(target),
                target.getInterval()
            );
            scheduledTasks.put(target.getName(), future);
        }
    }
}
```

### 5. Database Abstraction

**Location:** `infrastructure/adapter/persistence/`

**Purpose:** Abstract database operations using Spring Data JPA.

**Repository Pattern:**

``` java
public interface CheckResultRepository extends JpaRepository<CheckResultEntity, Long> {
    List<CheckResultEntity> findByTargetNameOrderByTimestampDesc(String targetName);
    
    @Query("SELECT cr FROM CheckResultEntity cr WHERE cr.timestamp > :since")
    List<CheckResultEntity> findRecentResults(@Param("since") LocalDateTime since);
    
    long countByStatusAndTargetName(CheckStatus status, String targetName);
}
```

### 6. REST API

**Location:** `presentation/controller/`

**Purpose:** Expose monitoring data via REST endpoints.

**Example Controller:**

``` java
@RestController
@RequestMapping("/api/v1/monitoring")
public class MonitoringController {
    private final MonitoringService monitoringService;
    
    @GetMapping("/targets")
    public ResponseEntity<List<TargetStatusResponse>> getTargets() {
        List<MonitoringTarget> targets = monitoringService.getAllTargets();
        return ResponseEntity.ok(
            targets.stream()
                .map(TargetStatusMapper::toResponse)
                .collect(Collectors.toList())
        );
    }
    
    @GetMapping("/results/{targetName}")
    public ResponseEntity<List<CheckResultResponse>> getResults(
        @PathVariable String targetName,
        @RequestParam(defaultValue = "24") int hours
    ) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<CheckResult> results = monitoringService.getResults(targetName, since);
        return ResponseEntity.ok(
            results.stream()
                .map(CheckResultMapper::toResponse)
                .collect(Collectors.toList())
        );
    }
}
```

---

## Database Schema

### Tables

#### `monitoring_targets`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Target ID |
| `name` | VARCHAR(255) | Unique target name |
| `type` | VARCHAR(50) | Type: PING, HTTP, CERTIFICATE |
| `target` | VARCHAR(512) | IP address, hostname, or URL |
| `interval_seconds` | INTEGER | Check interval in seconds |
| `ssl_enabled` | BOOLEAN | SSL/TLS enabled (HTTP checks) |
| `verify_certificate` | BOOLEAN | Verify SSL certificate |
| `created_at` | TIMESTAMP | Creation timestamp |
| `last_checked` | TIMESTAMP | Last check timestamp |

#### `check_results`

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK) | Result ID |
| `target_id` | BIGINT (FK) | Reference to monitoring_targets |
| `status` | VARCHAR(50) | SUCCESS, FAILURE |
| `timestamp` | TIMESTAMP | Check execution time |
| `latency_ms` | BIGINT | Response time in milliseconds |
| `status_code` | INTEGER | HTTP status code (HTTP checks) |
| `error_message` | TEXT | Error details (if failed) |
| `certificate_expiry` | TIMESTAMP | Certificate expiry (cert checks) |

### Indexes

``` sql
CREATE INDEX idx_results_target_timestamp 
    ON check_results(target_id, timestamp DESC);

CREATE INDEX idx_results_status 
    ON check_results(status);

CREATE INDEX idx_targets_name 
    ON monitoring_targets(name);
```

---

## Configuration Flow

``` text
┌──────────────────┐
│ application.yaml │
└────────┬─────────┘
│
▼
┌──────────────────────────┐
│ Environment Variables    │
│ (override YAML values)   │
└────────┬─────────────────┘
│
▼
┌──────────────────────────┐
│ @ConfigurationProperties │
│ (bind to Java objects)   │
└────────┬─────────────────┘
│
▼
┌──────────────────────────┐
│ Configuration Validation │
│ (@Validated)             │
└────────┬─────────────────┘
│
▼
┌──────────────────────────┐
│ Spring Bean Creation     │
│ (DatabaseConfig, etc.)   │
└──────────────────────────┘
```

---

## Monitoring Check Flow

``` text
┌─────────────┐
│  Scheduler  │
│  (Trigger)  │
└──────┬──────┘
│
▼
┌─────────────────────┐
│ MonitoringService   │
│ executeCheck()      │
└──────┬──────────────┘
│
▼
┌─────────────────────┐
│ Domain Service      │
│ (PingCheckService,  │
│  HttpCheckService)  │
└──────┬──────────────┘
│
▼
┌─────────────────────┐
│ Network Adapter     │
│ (Execute actual     │
│  network operation) │
└──────┬──────────────┘
│
▼
┌─────────────────────┐
│ Create CheckResult  │
│ (Domain object)     │
└──────┬──────────────┘
│
▼
┌─────────────────────┐
│ Save to Database    │
│ (Repository)        │
└──────┬──────────────┘
│
▼
┌─────────────────────┐
│ Emit Metrics        │
│ (Micrometer)        │
└─────────────────────┘
```

---

## Database Migration Process

HLabMonitor uses **Liquibase** for database schema versioning.

### Migration Structure

``` text
src/main/resources/db/changelog/
├── db.changelog-master.yaml       # Master changelog
├── v1.0/
│   ├── 001-create-targets.yaml   # Create monitoring_targets table
│   └── 002-create-results.yaml   # Create check_results table
└── v1.1/
└── 003-add-certificate.yaml  # Add certificate fields
```

### Master Changelog

``` yaml
databaseChangeLog:
  - include:
      file: db/changelog/v1.0/001-create-targets.yaml
  - include:
      file: db/changelog/v1.0/002-create-results.yaml
  - include:
      file: db/changelog/v1.1/003-add-certificate.yaml
```

### Example Migration

``` yaml
databaseChangeLog:
  - changeSet:
      id: 001-create-targets
      author: amaury
      changes:
        - createTable:
            tableName: monitoring_targets
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
              - column:
                  name: name
                  type: VARCHAR(255)
                  constraints:
                    nullable: false
                    unique: true
              - column:
                  name: type
                  type: VARCHAR(50)
                  constraints:
                    nullable: false
              - column:
                  name: target
                  type: VARCHAR(512)
                  constraints:
                    nullable: false
```

---

## Dependency Injection

HLabMonitor uses Spring's dependency injection:

``` java
@Service
public class MonitoringService {
    private final CheckResultRepository repository;
    private final PingCheckService pingService;
    private final HttpCheckService httpService;
    private final MeterRegistry meterRegistry;
    
    // Constructor injection (recommended)
    public MonitoringService(
        CheckResultRepository repository,
        PingCheckService pingService,
        HttpCheckService httpService,
        MeterRegistry meterRegistry
    ) {
        this.repository = repository;
        this.pingService = pingService;
        this.httpService = httpService;
        this.meterRegistry = meterRegistry;
    }
    
    // Business methods
}
```

---

## Design Patterns

### 1. Hexagonal Architecture (Ports & Adapters)

**Purpose:** Decouple business logic from infrastructure.

**Implementation:**
- **Ports** (interfaces) define contracts
- **Adapters** implement ports for specific technologies
- **Domain** layer is technology-agnostic

### 2. Repository Pattern

**Purpose:** Abstract data access.

**Implementation:**
- Spring Data JPA repositories
- Domain repositories (interfaces) in domain layer
- JPA repositories (implementations) in infrastructure layer

### 3. Strategy Pattern

**Purpose:** Different check types (ping, HTTP, certificate).

**Implementation:**

``` java
public interface CheckExecutor {
    CheckResult execute(MonitoringTarget target);
}

@Component
public class PingCheckExecutor implements CheckExecutor { /* ... */ }

@Component
public class HttpCheckExecutor implements CheckExecutor { /* ... */ }
```

### 4. Factory Pattern

**Purpose:** Create appropriate check executor based on target type.

``` java
@Component
public class CheckExecutorFactory {
    private final Map<TargetType, CheckExecutor> executors;
    
    public CheckExecutor getExecutor(TargetType type) {
        return executors.get(type);
    }
}
```

### 5. Observer Pattern

**Purpose:** Metrics collection on check execution.

**Implementation:**
- Micrometer's `MeterRegistry`
- Observers record metrics when checks complete

---

## Security Considerations

### Current State

- No authentication/authorization (homelab focus)
- Actuator endpoints exposed for monitoring
- Database credentials in configuration

### Future Enhancements

- Optional Spring Security integration
- API key authentication
- Rate limiting
- Audit logging

---

## Performance Considerations

### Async Execution

- Monitoring checks run in separate thread pool
- Non-blocking operations
- Configurable thread pool size

### Database Optimization

- Indexes on frequently queried columns
- Connection pooling (HikariCP)
- Batch inserts for results

### Caching

- Target configuration cached at startup
- Check results stored in database, not memory
- Metrics aggregated by Prometheus

---

## Testing Strategy

### Unit Tests

- Test domain logic in isolation
- Mock external dependencies
- Fast execution

### Integration Tests

- Test database interactions
- Use H2 in-memory database
- Spring Boot Test framework

### End-to-End Tests

- Full application context
- Test API endpoints
- Verify monitoring checks execute

---

## See Also

- [Building from Source](building.md)
- [Contributing Guide](contributing.md)
- [Configuration Reference](../configuration/application-yaml.md)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
