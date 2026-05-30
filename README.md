# PassVault — Password Manager
A secure Android app to store and manage passwords, protected by biometric authentication and SQLCipher encryption.

## Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| Android Studio | Hedgehog (2023.1.1) or newer |
| Android SDK | API 36 (download via SDK Manager) |
| JDK | 17 (bundled with Android Studio) |
| Device / Emulator | Android 14 (API 34) minimum |

### Setup

1. **Clone the repository**
   ```bash
   git clone <repo-url>
   cd password_vault
   ```

2. **Open in Android Studio**
   - Go to **File → Open** and select the project folder.
   - Android Studio will auto-generate `local.properties` pointing to your local Android SDK. Wait for Gradle sync to finish.

3. **Download missing SDK (if prompted)**
   - If Android Studio warns about a missing SDK platform, open **SDK Manager** (`Tools → SDK Manager`) and install **Android 14 (API 34)** or higher.

4. **Run the app**
   - Connect a physical device running Android 14+ or create an emulator with API 34+.
   - Press **Run** (▶) or use `Shift+F10`.

> **Note:** The app uses biometric authentication. On an emulator, go to **Settings → Security → Fingerprint** to enroll a fingerprint before launching.

### Build from command line

```bash
# Debug build
./gradlew assembleDebug

# Install directly to connected device
./gradlew installDebug
```

## Tech Stack

- **UI** — Jetpack Compose + Material 3
- **DI** — Hilt
- **Database** — Room + SQLCipher (encrypted)
- **Security** — AndroidX Biometric, EncryptedSharedPreferences
- **Async** — Kotlin Coroutines + WorkManager
