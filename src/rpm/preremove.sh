#!/bin/bash

if [ $1 -eq 0 ]; then
    echo "Arrêt du service HLabMonitor..."
    systemctl stop hlabmonitor.service 2>/dev/null || true
    systemctl disable hlabmonitor.service 2>/dev/null || true
    echo "Service arrêté et désactivé."
fi

exit 0
