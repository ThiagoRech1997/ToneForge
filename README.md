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
For detailed features, usage, architecture, and advanced guides, see the [project Wiki](features-new/wiki/).

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

## 🔧 Development

### Prerequisites
- Android Studio Hedgehog or newer
- Android SDK API 27+
- NDK (Native Development Kit)
- Android device with microphone

### Build
```bash
./gradlew assembleDebug
```

## 📄 License

This project is open source. Feel free to contribute!

## 👨‍💻 Author

Thiago Fernando Rech - Android Developer and Audio Enthusiast

---

**ToneForge** - Turn your Android into a professional pedalboard! 🎸 # Teste CI/CD
