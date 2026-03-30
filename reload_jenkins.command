#!/bin/bash
echo "=== Reloading Jenkins to pick up new Appium Pipeline config ==="

# Try Homebrew Jenkins (most common Mac install)
if brew services list 2>/dev/null | grep -q "jenkins"; then
  SERVICE=$(brew services list | grep jenkins | awk '{print $1}')
  echo "Restarting Jenkins via Homebrew: $SERVICE"
  brew services restart "$SERVICE"
  sleep 5
  echo "Jenkins restarted. Opening in browser..."
  open "http://localhost:8080/job/Appium/"
  echo "=== Done! ==="
  read -n1
  exit 0
fi

# Try launchctl (manual install)
if launchctl list | grep -q jenkins; then
  echo "Restarting Jenkins via launchctl..."
  PLIST=$(find ~/Library/LaunchAgents /Library/LaunchDaemons -name "*jenkins*" 2>/dev/null | head -1)
  if [ -n "$PLIST" ]; then
    launchctl unload "$PLIST" && launchctl load "$PLIST"
    sleep 5
    open "http://localhost:8080/job/Appium/"
    echo "=== Done! ==="
    read -n1
    exit 0
  fi
fi

# Fallback: just open Jenkins in browser so you can reload manually
echo ""
echo "Could not auto-restart Jenkins."
echo "Please do ONE of the following:"
echo "  1. In your browser go to: http://localhost:8080/manage"
echo "     → Click 'Reload Configuration from Disk'"
echo "  OR"
echo "  2. In a terminal run: brew services restart jenkins-lts"
echo ""
echo "Opening Jenkins manage page now..."
open "http://localhost:8080/manage"
echo "Press any key to close..."
read -n1
