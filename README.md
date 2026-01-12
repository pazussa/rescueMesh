# RescueMesh

**Offline Emergency Communication Network**

RescueMesh is a mobile mesh network application designed for emergency and disaster scenarios where traditional communication infrastructure is unavailable. It enables direct device-to-device communication using Bluetooth and WiFi Direct, creating a decentralized network that works completely offline.

## Key Features

### Core Functionality
- **Offline mesh networking** - No internet or cellular connection required
- **Multi-hop message forwarding** - Messages automatically relay through nearby devices to extend range
- **Store-and-forward protocol** - Messages persist locally and sync when devices come into range
- **Automatic peer discovery** - Devices automatically find and connect to nearby participants
- **End-to-end encryption** - All connections are encrypted for privacy

### Emergency Communication
- **SOS broadcasts** - Send critical emergency alerts with location and details
- **Resource requests** - Request specific supplies (water, medical aid, shelter, etc.)
- **Danger reports** - Report hazards like fires, collapsed buildings, or blocked roads
- **Status updates** - Let others know you're safe with "I'm OK" messages
- **Group chat** - Text messaging within incident rooms

### Network Technology
- **Google Nearby Connections API** - Uses WiFi Direct, Bluetooth LE, and Bluetooth Classic automatically
- **Approximately 100m range per hop** - Extended coverage through multi-hop routing
- **P2P_CLUSTER strategy** - Supports multiple simultaneous connections
- **Automatic inventory synchronization** - Devices sync message history when connecting

### Intelligence & Analysis
- **Offline AI summaries** - Automatic situation analysis without internet
- **Priority message sorting** - Critical messages automatically ranked by urgency
- **Real-time statistics** - Network status, connected peers, message counts
- **Danger zone detection** - Identifies and highlights reported hazards

### Sharing & Deployment
- **In-app APK sharing** - Share the app with others via Bluetooth or Quick Share
- **QR code room joining** - Easy onboarding for new participants
- **Emergency broadcast mode** - Make your device visible to all nearby phones

## Installation

### Prerequisites
- Android device running Android 6.0 (API 23) or higher
- Bluetooth and Location permissions (required for Nearby Connections)

### Installing from APK

1. Download the APK file to your Android device
2. Open the APK file to begin installation
3. If prompted, enable "Install from unknown sources" in your device settings
4. Complete the installation
5. Open RescueMesh and grant required permissions (Location, Bluetooth, Notifications)

### Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/pazussa/rescueMesh.git
   cd rescuemesh
   ```

2. Build the debug APK:
   ```bash
   ./gradlew :composeApp:assembleDebug
   ```

3. The APK will be generated at:
   ```
   composeApp/build/outputs/apk/debug/composeApp-debug.apk
   ```

4. Install on a connected device:
   ```bash
   adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
   ```

## Quick Start

1. **Launch the app** and enter your name
2. **Create an Incident Room** or join an existing one with a room code
3. **Grant permissions** when prompted (required for mesh networking)
4. **Start communicating** - The app will automatically discover nearby devices
5. **Share the app** with others using the built-in APK sharing feature

## Usage Scenarios

- **Natural disasters** - Earthquakes, hurricanes, floods
- **Search and rescue operations** - Coordinate rescue teams in remote areas
- **Large events** - Communication backup when cellular networks are overloaded
- **Remote areas** - Communication where infrastructure doesn't exist
- **Emergency drills** - Training and preparedness exercises

## Technical Architecture

### Technology Stack
- **Kotlin Multiplatform** - Cross-platform codebase (Android primary target)
- **Jetpack Compose** - Modern declarative UI
- **Google Nearby Connections** - Mesh networking transport layer
- **kotlinx.serialization** - Message serialization
- **Coroutines & Flow** - Asynchronous programming

### Network Protocol
- Messages are JSON-serialized and transmitted via Nearby Connections
- Each message has a unique ID for deduplication
- Automatic message forwarding with hop count limits
- Inventory-based synchronization to prevent message loss
- Local storage persistence using Android SharedPreferences

### Security
- Incident Rooms are protected with PIN codes
- Nearby Connections provides automatic encryption
- No data is sent to external servers (100% offline)

## Permissions Required

- **Location** - Required by Android for Bluetooth device discovery
- **Bluetooth** - Core functionality for mesh networking
- **Nearby Devices** - To discover and connect to nearby peers
- **Notifications** - To alert users of incoming emergency messages

## Project Structure

```
rescuemesh/
├── composeApp/
│   ├── src/
│   │   ├── androidMain/     # Android-specific implementation
│   │   ├── commonMain/      # Shared Kotlin code
│   │   ├── desktopMain/     # Desktop implementation (experimental)
│   │   └── iosMain/         # iOS implementation (experimental)
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

## License

This project is open source and available for use in emergency and disaster response scenarios.

## Contributing

Contributions are welcome, especially:
- Bug fixes and stability improvements
- Network protocol optimizations
- UI/UX enhancements
- Translations to additional languages
- Testing and documentation

## Disclaimer

RescueMesh is designed as an emergency communication tool. While it employs encryption and privacy measures, users should be aware:
- The app works best in areas with multiple active users
- Range is limited by device capabilities and environmental factors
- This is not a replacement for professional emergency services
- Always follow official emergency protocols and instructions

## Contact
juanpa1@unicaucax.edu.co 
Repository: https://github.com/pazussa/rescueMesh 


Developed for the Kotlin Multiplatform Contest 2025/2026 ❤️ 
 
