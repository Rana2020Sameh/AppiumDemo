#!/bin/bash

# Navigate to the project directory
cd "$(dirname "$0")"

echo "========================================="
echo "  Downloading missing Maven dependencies"
echo "========================================="
echo ""

# Force-download all missing dependencies (including Jackson)
mvn dependency:resolve -U

echo ""
echo "========================================="
echo "  Done! Now go to IntelliJ IDEA:"
echo "  Click the 'm' Maven icon on the right"
echo "  panel → 'Reload All Maven Projects'"
echo "========================================="
read -p "Press Enter to close..."
