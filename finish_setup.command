#!/bin/bash
echo "=== AppiumDemo Jenkins Setup – Final Step ==="
echo ""

# ── Step 1: Reload Jenkins config from disk ──────────────────────────────────
echo "Opening Jenkins 'Reload Configuration from Disk' page..."
open "http://localhost:8080/manage"
sleep 2

# ── Step 2: Open the Appium Pipeline job page ────────────────────────────────
echo "Opening the Appium job configuration page..."
open "http://localhost:8080/job/Appium/configure"
sleep 1

# ── Step 3: Open GitHub webhook settings ────────────────────────────────────
echo "Opening GitHub webhook settings for AppiumDemo..."
open "https://github.com/Rana2020Sameh/AppiumDemo/settings/hooks"

echo ""
echo "======================================================"
echo "  Three browser tabs just opened. Do this in order:"
echo ""
echo "  TAB 1 – Jenkins Manage page:"
echo "    → Click 'Reload Configuration from Disk'"
echo "    → Confirm if prompted"
echo ""
echo "  TAB 2 – Appium job Configure page:"
echo "    → Scroll down to 'Build Triggers'"
echo "    → Make sure 'GitHub hook trigger for GITScm polling' is CHECKED"
echo "    → Click SAVE"
echo ""
echo "  TAB 3 – GitHub Webhooks:"
echo "    → Click 'Add webhook'"
echo "    → Payload URL: http://<your-mac-IP>:8080/github-webhook/"
echo "    → Content type: application/json"
echo "    → Trigger: 'Just the push event'"
echo "    → Click 'Add webhook'"
echo ""
echo "  After that, every git push will auto-trigger a build!"
echo "======================================================"
echo ""
echo "Press any key to close..."
read -n1
