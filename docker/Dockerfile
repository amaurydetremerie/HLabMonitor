FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /opt/app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy AS final
WORKDIR /opt/app
EXPOSE 8080

RUN groupadd --gid 1001 appuser && \
    useradd --uid 1001 --gid appuser --shell /bin/bash --create-home appuser

RUN mkdir -p /etc/hlabmonitor /var/lib/hlabmonitor /var/log/hlabmonitor && \
    chown -R appuser:appuser /etc/hlabmonitor /var/lib/hlabmonitor /var/log/hlabmonitor

COPY --from=builder /opt/app/target/*.jar /opt/app/app.jar

ENV HLABMONITOR_CONFIG_LOCATION=""

USER appuser

ENTRYPOINT ["sh", "-c", "\
    java \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:InitialRAMPercentage=50.0 \
    -XX:+ExitOnOutOfMemoryError \
    -Djava.security.egd=file:/dev/./urandom \
    -Dspring.config.additional-location=optional:file:/etc/hlabmonitor/,${HLABMONITOR_CONFIG_LOCATION} \
    -jar /opt/app/app.jar"]
