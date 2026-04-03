#!/bin/bash
cd "$(dirname "$0")"
git add src/test/java/pages/LoginPage.java \
        src/test/java/pages/SignupPage.java \
        Jenkinsfile
git commit -m "Fix: use accessibilityId for iOS locators; open Appium Inspector on pipeline"
git push origin main
echo "✅ Done! Now trigger a new Jenkins build."
