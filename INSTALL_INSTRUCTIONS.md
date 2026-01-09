# RescueMesh - Installation Instructions

## Android Installation

### Option 1: Download from GitHub Actions (Easiest)

1. Go to: https://github.com/pazussa/rescueMesh/actions
2. Click on the latest successful workflow run
3. Scroll down to "Artifacts"
4. Download `RescueMesh-Android-Debug`
5. Extract and install the APK on your device

### Option 2: Install the APK Directly (Recommended for Testing)

1. **Enable Unknown Sources** on your Android device:
   - Go to Settings > Security
   - Enable "Unknown sources" or "Install from unknown apps"

2. **Transfer the APK** to your device:
   - The APK file is located at: `RescueMesh-Android.apk`
   - Transfer via USB, email, cloud storage, or any file sharing method

3. **Install the APK**:
   - Open the file on your device
   - Tap "Install"
   - If prompted about security, tap "Install anyway"

4. **Grant Permissions**:
   - When first launching, grant all requested permissions:
     - Bluetooth
     - Location (required for Bluetooth scanning)
     - Nearby Devices

### Option 2: Build from Source

```bash
# Clone the repository
git clone https://github.com/pazussa/rescueMesh.git
cd rescueMesh

# Build debug APK
./gradlew :composeApp:assembleDebug

# Install via ADB
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## iOS Installation

### IMPORTANT: iOS Limitations Without Apple Developer Account

Installing apps on iPhone is restricted by Apple. Here are your options:

### Option 1: Ask Someone with a Mac (Free but needs Mac)

If someone you know has a Mac:
1. They clone the repo: `git clone https://github.com/pazussa/rescueMesh.git`
2. Open `iosApp/iosApp.xcodeproj` in Xcode
3. Connect the iPhone via USB
4. Build and run directly to the phone

The app will work for 7 days before needing to be reinstalled (free Apple ID limitation).

### Option 2: Use AltStore (No Mac Required - Recommended)

**AltStore** allows installing IPAs on iPhone without jailbreak, using a Windows PC.

**Requirements:**
- Windows PC
- iPhone with iOS 14+
- iTunes installed
- iCloud for Windows installed

**Steps:**
1. Download AltStore: https://altstore.io/
2. Install AltServer on Windows
3. Connect iPhone to PC via USB
4. Trust the computer on iPhone
5. Open AltServer > Install AltStore to your iPhone
6. On iPhone, go to Settings > General > Device Management > Trust AltStore

**To install RescueMesh:**
1. Download the IPA from GitHub Actions artifacts (when available)
2. Or use the unsigned archive from: `RescueMesh-iOS-Framework` artifact
3. Open AltStore on iPhone
4. Tap + and select the IPA file

**Note:** Apps installed via AltStore expire after 7 days and need refreshing.

### Option 3: Use Sideloadly (Windows/Mac - No Jailbreak)

Similar to AltStore but simpler interface:
1. Download: https://sideloadly.io/
2. Connect iPhone
3. Drag IPA file to Sideloadly
4. Enter your Apple ID
5. Click Start

### Option 4: TestFlight (Requires $99/year Developer Account)

If you get an Apple Developer account:
1. Configure signing in Xcode (on a Mac or via CI)
2. Upload to App Store Connect
3. Invite testers via TestFlight
4. Testers install via TestFlight app

### Option 5: MacinCloud / MacStadium (Rent a Mac)

Rent a Mac in the cloud:
- **MacinCloud**: ~$1/hour - https://www.macincloud.com/
- **MacStadium**: More expensive but better for CI

### GitHub Actions (Automatic Builds)

This repo has GitHub Actions configured to build both platforms automatically:
- Every push to `main` triggers builds
- Download artifacts from: https://github.com/pazussa/rescueMesh/actions

**Available artifacts:**
- `RescueMesh-Android-Debug` - Android APK (ready to install)
- `RescueMesh-Android-Release` - Android release APK (unsigned)
- `RescueMesh-iOS-Framework` - Kotlin framework for iOS
- `RescueMesh-iOS-Simulator` - iOS Simulator app

---

## Easiest Path for iOS Testing

**If the person with iPhone has a Mac:**
```bash
git clone https://github.com/pazussa/rescueMesh.git
cd rescueMesh
./gradlew :composeApp:linkDebugFrameworkIosArm64
open iosApp/iosApp.xcodeproj
# Connect iPhone, click Run
```

**If nobody has a Mac:**
1. Use AltStore + Windows PC (see Option 2 above)
2. Or rent MacinCloud for 1 hour (~$1)

---

## Quick Comparison

| Method | Cost | Difficulty | Duration |
|--------|------|------------|----------|
| Someone's Mac | Free | Easy | 7 days |
| AltStore (Windows) | Free | Medium | 7 days |
| Sideloadly | Free | Medium | 7 days |
| MacinCloud | ~$1/hr | Medium | Until signed |
| TestFlight | $99/year | Easy | 90 days |

---

## Testing the App

### On a Single Device
1. Launch the app
2. Enter your name
3. Create a new Incident Room
4. Note the Room Code for sharing

### Multi-Device Testing (Recommended)
1. Have 2-3 devices nearby
2. Device 1: Create an Incident Room
3. Other devices: Join using the Room Code and PIN
4. Send test messages (SOS, OK, Resources, Danger)
5. Verify messages appear on all devices

### Features to Test
- SOS button (red) - Emergency alert
- OK button (green) - I'm safe notification  
- Package button (blue) - Resource request
- Warning button (orange) - Danger report
- AI Summary (purple brain icon) - Situation analysis
- Network Status - Peer connections

---

## Troubleshooting

### Android - App Doesn't Connect
- Ensure Bluetooth is ON
- Ensure WiFi is ON (can be without internet)
- Grant all permissions
- Check if devices are within ~30m range

### Android - Permission Issues
- Go to Settings > Apps > RescueMesh > Permissions
- Enable all permissions manually

### iOS - Framework Not Found
- Run `./gradlew :composeApp:linkDebugFrameworkIosArm64` again
- Clean build folder in Xcode (Cmd+Shift+K)
- Rebuild

### Both Platforms - Messages Not Syncing
- Ensure all devices are in the same Incident Room
- Check Network Status screen for peer count
- Try the "Sync" button to request missing messages

---

## Requirements

| Platform | Minimum Version | Recommended |
|----------|-----------------|-------------|
| Android | API 26 (8.0) | API 33+ (13) |
| iOS | iOS 14.0 | iOS 16+ |

## Permissions Explained

| Permission | Why Needed |
|------------|------------|
| Bluetooth | P2P communication |
| Location | Required for Bluetooth/WiFi scanning |
| Nearby Devices | Discover other RescueMesh users |
| Local Network (iOS) | Bonjour service discovery |

**Note**: Location permission is required by the operating system for Bluetooth scanning, but RescueMesh does NOT collect or transmit your location data.
