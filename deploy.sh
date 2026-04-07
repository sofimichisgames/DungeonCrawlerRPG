#!/bin/bash
# Dungeon Crawler RPG - Deploy Script
# Install and launch the app on connected device

set -e

echo ""
echo "=========================================="
echo "  Dungeon Crawler - Deploy"
echo "=========================================="
echo ""

ADB="/c/Android/platform-tools/platform-tools/adb"
APK="app/build/outputs/apk/debug/app-debug.apk"

# Check APK exists
if [ ! -f "$APK" ]; then
    echo "✗ APK not found at $APK"
    echo ""
    echo "Run build first:"
    echo "  bash build_and_deploy.sh"
    exit 1
fi

echo "→ APK: $APK"
echo "  Size: $(ls -lh "$APK" | awk '{print $5}')"
echo ""

# Check device
echo "→ Checking device..."
DEVICES=$($ADB devices 2>&1 | grep "device$" | wc -l)
if [ "$DEVICES" -eq 0 ]; then
    echo "✗ No device connected"
    $ADB devices
    exit 1
fi
echo "  ✓ Device found"
echo ""

# Uninstall old version
echo "→ Removing old version..."
$ADB uninstall com.dungeoncrawler 2>&1 | grep -v "Error" || true
sleep 1

# Install
echo "→ Installing..."
if $ADB install "$APK" 2>&1 | grep -q "Success"; then
    echo "  ✓ Installed"
else
    echo "  ✗ Installation failed"
    exit 1
fi
echo ""

# Launch
echo "→ Launching app..."
$ADB shell am start -n com.dungeoncrawler/.MainActivity 2>&1 > /dev/null
sleep 2
echo "  ✓ App launched"
echo ""

echo "=========================================="
echo "  ✓ Deploy complete!"
echo "=========================================="
echo ""
