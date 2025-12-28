# 🎸 ToneForge - Digital Pedalboard

ToneForge is a digital multi-effects pedalboard for Android, featuring real-time audio processing with native C++ code.

## 🚀 Quick Start

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd ToneForge
   ```
2. **Build the app:**
   ```bash
   ./gradlew assembleDebug
   ```
3. **Install on your Android device (API 27+):**
   - Open the project in Android Studio or install the APK manually.

## 🛠️ Main Technologies
- Android (Java, Fragments)
- Native C++ (JNI, real-time audio)
- Material Design UI
- Gradle & CMake

## 📖 Full Documentation
For detailed features, usage, architecture, and advanced guides, see the [project Wiki](https://github.com/ThiagoRech1997/ToneForge/wiki).

## ✨ Key Features

- **Real-time audio processing** with low latency
- **Multiple effects**: Gain, distortion, delay, reverb, chorus, flanger, phaser, EQ, compressor
- **Preset system** with favorites and export/import
- **Customizable effect order** via drag-and-drop
- **Real-time tuner** with pitch detection
- **Automation system** for dynamic performances
- **MIDI Learn** for external controller support
- **Background processing** with screen off
- **Modern dark interface** with tooltips

## 📱 Compatibility

- **Minimum Android**: 8.1 (API 27)
- **Architectures**: ARM64, ARM32, x86, x86_64
- **Sample rate**: 48kHz
- **Format**: PCM Float 32-bit
- **Channels**: Mono (input and output)

## 📚 Documentation

Comprehensive documentation is available in the [`docs/`](docs/) directory:

- **[Quick Start Guide](docs/QUICKSTART.md)** - Get started quickly
- **[CLAUDE.md](CLAUDE.md)** - Complete project architecture and development guide
- **[Setup Guides](docs/setup/)** - Environment configuration and ADB setup
- **[Testing Reports](docs/testing/)** - Build, deploy, and test reports
- **[Scripts Documentation](scripts/README.md)** - Available development scripts

## 🔧 Development

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK API 27+
- NDK (Native Development Kit)
- Android device with microphone

### Quick Setup
```bash
# Setup development environment (installs SDK, tools, etc.)
./scripts/setup/setup-dev-environment.sh

# Verify environment
./scripts/verify-environment.sh
```

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build and install on device
./gradlew installDebug

# Clean build
./gradlew clean assembleDebug
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests (device required)
./gradlew connectedAndroidTest

# Generate coverage report
./gradlew jacocoTestReport

# Test on device with automated script
./scripts/test-app-device.sh
```

### Useful Scripts

All development scripts are in [`scripts/`](scripts/):

- **`scripts/setup/setup-dev-environment.sh`** - Complete environment setup
- **`scripts/verify-environment.sh`** - Verify development environment
- **`scripts/test-app-device.sh`** - Automated device testing
- **`scripts/functional-validation.sh`** - Full project validation
- **`scripts/create-release.sh`** - Create a release build

See [`scripts/README.md`](scripts/README.md) for detailed documentation.

## 📊 Project Structure

```
ToneForge/
├── app/                    # Android application source code
├── docs/                   # Complete documentation
│   ├── setup/             # Setup and configuration guides
│   └── testing/           # Test reports and validation
├── scripts/               # Development and testing scripts
│   └── setup/            # Environment setup scripts
├── logs/                  # Build and test logs (git ignored)
├── CLAUDE.md             # Project architecture documentation
└── README.md             # This file
```

## 📄 License

This project is open source. Feel free to contribute!

## 👨‍💻 Author

Thiago Fernando Rech - Android Developer and Audio Enthusiast

---

**ToneForge** - Turn your Android into a professional pedalboard! 🎸
