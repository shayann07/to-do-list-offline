# to-do-list-offline

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)]()
[![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> The offline-only version of my Reminders app â€” keep tasks entirely on-device with no account needed, and get local notifications when each reminder fires.

---

## 📖 Overview

The offline-only version of my Reminders app â€” keep tasks entirely on-device with no account needed, and get local notifications when each reminder fires.

---

## ✨ Key Features

- **Ios Style Ui**: Built-in support and optimized flows for ios style ui.
- **Notifications**: Built-in support and optimized flows for notifications.
- **Offline App**: Built-in support and optimized flows for offline app.

---

## 🛠️ Technology Stack

| Component / Layer | Technology |
|---|---|
| **Platform** | Android |
| **Primary Language** | Kotlin |
| **Architecture** | MVVM / Clean Architecture |
| **License** | Open Source (MIT) |

---

## 🚀 Getting Started

1. Open in Android Studio (Hedgehog or newer for AGP 8.10.1). Sync Gradle.
2. No external services to configure — fully offline.
3. Run on Android 8+ (`minSdk 26`). On Android 12+, grant `SCHEDULE_EXACT_ALARM` in system settings or alarms will fail (the app does not currently bounce to the settings page).
4. `./gradlew :app:assembleDebug`.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — Copyright (c) 2026 [shayann07](https://github.com/shayann07).
