#!/bin/bash

systemctl daemon-reload

if [ $1 -eq 0 ]; then
    echo ""
    echo "HLabMonitor removed."
    echo ""
    echo "Note: Configuration and data files are not deleted:"
    echo "  - /etc/hlabmonitor/"
    echo "  - /var/lib/hlabmonitor/"
    echo "  - /var/log/hlabmonitor/"
    echo ""
    echo "To remove completely:"
    echo "  sudo rm -rf /etc/hlabmonitor"
    echo "  sudo userdel hlabmonitor"
    echo "  sudo groupdel hlabmonitor"
    echo ""

fi

exit 0
