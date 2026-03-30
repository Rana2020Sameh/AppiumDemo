#!/bin/bash
cd "$(dirname "$0")"
echo "=== AppiumDemo: Commit & Push to GitHub ==="
rm -f .git/index.lock
git add Jenkinsfile \
        src/test/java/core/DriverManager.java \
        src/test/java/resources/config.properites \
        src/test/java/tests/BaseTests.java
git commit -m "Configure Appium pipeline for iPhone 16 Pro iOS simulator

- Jenkinsfile: 4-stage pipeline with githubPush() trigger
- DriverManager: dynamic path via user.dir, deviceName from config
- BaseTests: platform switched to ios, tearDown enabled
- config.properites: relative app paths, iosDeviceName=iPhone 16 Pro

Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>"
git push origin main
echo ""
echo "=== Done! Press any key to close ==="
read -n1
