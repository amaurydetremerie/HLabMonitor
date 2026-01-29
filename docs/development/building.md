# Building from Source

Quick guide to build and run HLabMonitor locally.

---

## Prerequisites

- Java 21+
- Maven 3.9+
- Docker (optional, for container builds)

---

## Quick Start

``` bash
# Clone and build
git clone https://github.com/amaurydetremerie/HLabMonitor.git
cd HLabMonitor
mvn clean install

# Run locally
java -jar target/hlabmonitor-1.0.0.jar

# Or with Maven
mvn spring-boot:run
```

---

## Building

### Standard Build

``` bash
mvn clean install
```

### Skip Tests

``` bash
mvn clean install -DskipTests
```

### Run Tests

``` bash
# All tests
mvn test

# Specific test
mvn test -Dtest=PingCheckServiceTest

# With coverage
mvn clean test jacoco:report
# Report: target/site/jacoco/index.html
```

---

## Running Locally

### With Custom Configuration

``` bash
# Using application.yaml in current directory
java -jar target/hlabmonitor-1.0.0.jar

# Custom config location
java -jar target/hlabmonitor-1.0.0.jar \
--spring.config.location=file:./my-config.yaml

# Inline parameters
java -jar target/hlabmonitor-1.0.0.jar \
--database.type=postgresql \
--database.host=localhost \
--database.username=monitor \
--database.password=secret
```

### With Maven

``` bash
# Default
mvn spring-boot:run

# With custom args
mvn spring-boot:run -Dspring-boot.run.arguments="--database.type=sqlite"
```

---

## Building Docker Images

### Local Build (Single Platform)

``` bash
# Ubuntu variant
docker build -t hlabmonitor:local -f docker/Dockerfile .

# Alpine variant
docker build -t hlabmonitor:alpine -f docker/Dockerfile.alpine .

# Corretto variant
docker build -t hlabmonitor:corretto -f docker/Dockerfile.corretto .

# Run locally
docker run --rm -p 8080:8080 hlabmonitor:local
```

### Multi-Platform Build

``` bash
# Setup buildx (once)
docker buildx create --name hlabmonitor-builder --use

# Build for multiple platforms
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t wiserisk/hlabmonitor:latest \
  -f docker/Dockerfile \
  --push \
  .
```

### Build All Variants

``` bash
# Build script for all variants
for variant in Dockerfile Dockerfile.alpine Dockerfile.corretto; do
  suffix=$(echo $variant | sed 's/Dockerfile//' | sed 's/\.//')
  tag="wiserisk/hlabmonitor${suffix:+:$suffix}"
  
  docker buildx build \
    --platform linux/amd64,linux/arm64 \
    -t $tag \
    -f docker/$variant \
    --push \
    .
done
```

---

## Maven Profiles

``` bash
# Development profile
mvn clean install -Pdev

# Production profile
mvn clean install -Pprod
```

---

## Project Structure

``` text
hlabmonitor/
├── src/
│   ├── main/
│   │   ├── java/be/wiserisk/hlabmonitor/
│   │   │   ├── config/              # Configuration
│   │   │   ├── domain/              # Domain layer
│   │   │   ├── application/         # Application services
│   │   │   ├── infrastructure/      # Adapters
│   │   │   └── presentation/        # REST controllers
│   │   └── resources/
│   │       ├── application.yaml
│   │       └── db/changelog/        # Liquibase migrations
│   └── test/
├── docker/
│   ├── Dockerfile                   # Ubuntu-based
│   ├── Dockerfile.alpine            # Alpine-based
│   └── Dockerfile.corretto          # Corretto-based
└── pom.xml
```

---

## Troubleshooting

### Build Issues

``` bash
# Force update dependencies
mvn clean install -U

# Clear local Maven cache
rm -rf ~/.m2/repository/be/wiserisk/hlabmonitor
mvn clean install
```

### Docker Build Issues

``` bash
# Ensure JAR exists
mvn clean package
ls -lh target/hlabmonitor-*.jar

# Build for host platform only
docker build --platform $(uname -m | sed 's/x86_64/linux\/amd64/') \
  -t hlabmonitor:local \
  -f docker/Dockerfile .
```

### Port Conflicts

``` bash
# Run on different port
java -jar target/hlabmonitor-1.0.0.jar --server.port=8081
```

---

## CI/CD

See `.github/workflows/` for:
- `build.yml` - Build and test on push/PR
- `release.yml` - Build and push Docker images on release

---

## See Also

- [Architecture Overview](architecture.md)
- [Contributing Guide](contributing.md)
- [Configuration Reference](../configuration/application-yaml.md)
