#!/bin/bash

systemctl daemon-reload

chown -R hlabmonitor:hlabmonitor /var/lib/hlabmonitor
chown -R hlabmonitor:hlabmonitor /var/log/hlabmonitor

echo ""
echo "========================================="
echo "HLabMonitor successfully installed!"
echo "========================================="
echo ""
echo "Configuration:"
echo "  - Main file: /etc/hlabmonitor/application.yaml"
echo "  - Environment variables: /etc/sysconfig/hlabmonitor"
echo "  - Example: /etc/hlabmonitor/application-override-example.yaml"
echo ""
echo "Start the service:"
echo "  sudo systemctl start hlabmonitor"
echo "  sudo systemctl enable hlabmonitor"
echo ""
echo "View logs:"
echo "  sudo journalctl -u hlabmonitor -f"
echo ""
echo "Check status:"
echo "  sudo systemctl status hlabmonitor"
echo ""

exit 0
