#!/bin/bash

getent group hlabmonitor >/dev/null || groupadd -r hlabmonitor
getent passwd hlabmonitor >/dev/null || \
    useradd -r -g hlabmonitor -d /var/lib/hlabmonitor \
    -s /sbin/nologin -c "HLabMonitor service user" hlabmonitor

exit 0