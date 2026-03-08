#!/bin/sh

DEBUG_OPTS=""
if [ "$DEBUG_MODE" = "true" ]; then
  DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
fi

exec java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:InitialRAMPercentage=50.0 \
  -XX:+ExitOnOutOfMemoryError \
  $DEBUG_OPTS \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.config.additional-location=optional:file:/etc/hlabmonitor/,${HLABMONITOR_CONFIG_LOCATION} \
  -jar /opt/app/app.jar
