#!/bin/bash
# Dungeon Crawler RPG - Build & Deploy Script
# Full build + install + launch

set -e

export JAVA_HOME="C:\\Program Files\\Java\\jdk-21.0.10"
export PATH="$JAVA_HOME\\bin:$PATH"

echo ""
echo "=========================================="
echo "  Dungeon Crawler - Build & Deploy"
echo "=========================================="
echo ""

# Check Java
echo "→ Checking Java 21..."
java -version 2>&1 | head -1 | grep -q "21" && echo "  ✓ Java 21 found" || (echo "  ✗ Java 21 not found"; exit 1)
echo ""

# Note: Using deploy.sh to install existing APK
# For full rebuild from scratch, manually run:
#   cd to project directory
#   rm -rf app/build
#   ./gradlew clean assembleDebug
#   bash deploy.sh

echo "→ Checking for existing APK..."
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    APK_SIZE=$(ls -lh "app/build/outputs/apk/debug/app-debug.apk" | awk '{print $5}')
    echo "  ✓ APK found (Size: $APK_SIZE)"
    echo ""
    echo "→ Deploying to device..."
    bash deploy.sh
else
    echo "  ✗ APK not found. Please build first:"
    echo "    ./gradlew clean assembleDebug"
    echo "  Then run:"
    echo "    bash deploy.sh"
    exit 1
fi
