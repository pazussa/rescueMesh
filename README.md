# RescueMesh 

**Offline Emergency Communication Network using Mesh Networking**

> A Kotlin Multiplatform application that enables emergency communication between nearby devices without internet connectivity. Messages propagate automatically through the mesh network using Google Nearby Connections API.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-purple.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.6.10-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

##  About

RescueMesh is designed for disaster scenarios where traditional communication infrastructure fails. It creates a local mesh network between phones allowing critical messages (SOS, "I'm OK", resource requests, danger reports) to propagate even when devices aren't in direct range.

### Key Innovation

**Multi-hop mesh networking** - If device A can only reach device B, and B can reach C, messages from A will reach C through B's automatic forwarding. This extends communication range significantly in disaster areas.

---

##  Features

### Core Features (Part 1)
- [x] **Incident Rooms** - Create/join emergency rooms with PIN protection
- [x] **SOS Messages** - Send categorized distress signals (medical, fire, trapped, etc.)
- [x] **"I'm OK" Status** - Let others know you're safe
- [x] **Resource Requests** - Request water, food, medicine, transport, etc.
- [x] **Danger Reports** - Report fires, collapses, blocked routes
- [x] **Mesh Multi-hop** - Automatic store-and-forward message propagation
- [x] **Message Deduplication** - Prevents infinite message loops
- [x] **TTL (Time-To-Live)** - Controls message propagation depth
- [x] **Priority Queues** - SOS messages always propagate first

### AI Features (Part 2) - Fully Offline
- [x] **Automatic Triage** - AI-powered urgency scoring (0-100)
- [x] **Priority Classification** - Critical/High/Medium/Low categorization
- [x] **Situation Summary** - "What's happening in 30 seconds" digest
- [x] **Basic Translation** - Spanish  English for mixed rescue teams
- [x] **Keyword Detection** - Identifies emergency terms automatically

### Technical Features
- [x] **Bluetooth State Monitoring** - Warns when Bluetooth is disabled
- [x] **Network Status Dashboard** - Real-time mesh health visualization
- [x] **Local Persistence** - Messages survive app restarts
- [x] **Inventory Sync** - Request missing messages from peers
- [x] **Multi-language Support** - English (default) / Spanish

---

##  Architecture

```
rescuemesh/
├── composeApp/
│   └── src/
│       ├── commonMain/              # ~70% shared code
│       │   └── kotlin/com/rescuemesh/app/
│       │       ├── model/           # Data models (MeshMessage, Room, etc.)
│       │       ├── mesh/            # MeshEngine, RoomManager
│       │       ├── ai/              # OfflineAIEngine
│       │       ├── localization/    # Strings, LanguageManager
│       │       └── ui/              # Compose UI (screens, components, theme)
│       │
│       └── androidMain/             # Android-specific
│           └── kotlin/com/rescuemesh/app/
│               ├── nearby/          # Google Nearby Connections
│               ├── bluetooth/       # BluetoothStateMonitor
│               ├── persistence/     # SharedPreferences storage
│               ├── viewmodel/       # Android ViewModel
│               ├── App.kt           # Main navigation
│               └── MainActivity.kt  # Entry point
```

### Code Sharing Breakdown

| Layer | Location | Shared |
|-------|----------|--------|
| Data Models | commonMain | [x] 100% |
| Mesh Logic | commonMain | [x] 100% |
| AI Engine | commonMain | [x] 100% |
| Localization | commonMain | [x] 100% |
| UI/Compose | commonMain | [x] 100% |
| Transport (Nearby) | androidMain | Android-only* |
| Persistence | androidMain | Android-only* |

*These use `expect/actual` pattern for future iOS support

---

##  Quick Start

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- Android device with API 26+ (Android 8.0)
- Bluetooth and WiFi enabled

### Build Instructions

```bash
# Clone the repository
git clone https://github.com/yourusername/rescuemesh.git
cd rescuemesh

# Build debug APK
./gradlew assembleDebug

# APK location:
# composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### Install on Device

```bash
# Via ADB
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Or transfer APK manually and install
```

---

##  How to Test

### Basic Flow
1. **Launch app** on two or more devices
2. **Enter your name** on the welcome screen
3. **Device 1**: Create an Incident Room (note the Room Code and PIN)
4. **Device 2+**: Join Room using the code and PIN
5. **Send messages**: Use the action buttons ( SOS, OK I'm OK, [Package] Resources, [WARNING] Danger)
6. **View AI Summary**: Tap [AI] button to see situation digest
7. **Change language**: Access settings to switch between English/Spanish

### Testing Mesh Networking
1. Connect 3+ devices
2. Move Device C out of range from Device A (but in range of B)
3. Send SOS from A → verify it reaches C through B
4. Check Network Status screen for peer count

### Key Screens
| Screen | Purpose |
|--------|---------|
| Welcome | Name entry, create/join room |
| Room | Main chat, message feed, quick actions |
| Network Status | Mesh health, connected peers, sync |
| AI Summary | Situation digest, critical alerts |

---

##  AI Implementation

### Important: Fully Offline, No External Models

The "AI" in RescueMesh is implemented using **local pattern matching and keyword detection** - it does NOT use:
- [!] Cloud AI services (GPT, Claude, etc.)
- [!] Pre-trained ML models
- [!] TensorFlow/PyTorch
- [!] Any network requests

### How It Works

```kotlin
// Simplified urgency scoring algorithm
fun calculateUrgencyScore(message: MeshMessage): Int {
    var score = baseScoreByType(message.type)  // SOS=50, Danger=30, etc.
    
    // Pattern matching for critical keywords
    score += countCriticalKeywords(message) * 30  // "trapped", "fire", etc.
    score += countHighUrgencyKeywords(message) * 15
    
    // Contextual factors
    score += if (peopleCount > 5) 20 else 0
    score += categoryBonus(sosCategory)
    
    return minOf(score, 100)
}
```

### Why This Approach?
1. **Works offline** - No internet required (critical for disasters)
2. **Fast** - Pure Kotlin, no model loading
3. **Transparent** - Deterministic, auditable logic
4. **Light** - No heavy dependencies
5. **Compliant** - Uses only original code, no third-party AI APIs

---

##  Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin | 2.0.0 | Language |
| Kotlin Multiplatform | 2.0.0 | Cross-platform |
| Compose Multiplatform | 1.6.10 | UI Framework |
| Google Nearby Connections | 19.2.0 | P2P Transport |
| kotlinx.serialization | 1.6.3 | JSON encoding |
| kotlinx.coroutines | 1.8.0 | Async operations |
| kotlinx.datetime | 0.5.0 | Time handling |

---

##  Permissions Required

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES" />
```

**Why location?** Google Nearby Connections requires location permission for Bluetooth/WiFi scanning - we do NOT collect or transmit location data.

---

##  Demo Video

> A 3-5 minute screencast demonstrating key features is recommended. 
> [Link to video - TODO: Record before submission]

---

##  Roadmap

- [ ] iOS support via KMP
- [ ] Desktop support (JVM)
- [ ] GPS coordinates in messages
- [ ] Photo attachments (compressed)
- [ ] Voice message support
- [ ] Map visualization of incidents
- [ ] Integration with official emergency services

---

##  License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

##  Acknowledgments

- [Kotlin Foundation](https://kotlinfoundation.org) for the Kotlin Student Coding Competition
- [Google Nearby Connections](https://developers.google.com/nearby) for the P2P transport layer
- [JetBrains](https://www.jetbrains.com) for Kotlin and Compose Multiplatform

---

##  Author

Built with  for the Kotlin Student Coding Competition 2025-2026
