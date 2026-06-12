# to-do-list-offline

Offline-only fork of [`to-do-list`](https://github.com/shayann07/to-do-list). Same iOS-Reminders-style Android UI, Firebase removed at the dependency level. `applicationId = com.shayan.remindersios`, app label `Reminders`, Gradle project name "Reminders (IOS)". Two Gradle modules: `:app` (22 `.kt` files) and `:library` — a vendored copy of [Dimezis/BlurView](https://github.com/Dimezis/BlurView) (Apache-2.0), namespace `eightbitlab.com.blurview`. Room v3 for local persistence, `AlarmManager.setExactAndAllowWhileIdle` + `BroadcastReceiver` for reminder notifications, single-Activity + Jetpack Navigation, ViewModel/LiveData + Coroutines, ultra-ptr pull-to-refresh. AGP 8.10.1, Kotlin 2.0.21, KSP for Room, viewBinding, kotlin-parcelize, `compileSdk 35`, `minSdk 26`, `targetSdk 35`, Java 11.

## 🚨 Things to fix before shipping

### 1. `TaskReminderReceiver` is exported with no permission gate

`app/src/main/AndroidManifest.xml:42-44` declares:

```xml
<receiver
    android:name="com.shayan.remindersios.receivers.TaskReminderReceiver"
    android:exported="true" />
```

…with no `android:permission` attribute and no intent filter. The manifest comment claims `exported="true"` is needed "to allow the system to trigger it" — but the system fires explicit-component `PendingIntent`s into non-exported receivers just fine. **As-is, any app on the device can broadcast directly to this receiver and post fake reminder notifications.** Switch to `android:exported="false"`.

### 2. "Firebase removed" is true at the dependency level — but ~163 lines of dead Firebase code remain in source

- `app/build.gradle.kts` has **no** Firebase dependencies, no `google-services.json`, no `com.google.gms.google-services` plugin. The dependency-level removal is real.
- But `app/src/main/java/com/shayan/remindersios/data/repository/Repository.kt` still carries roughly 163 lines of commented-out Firebase code (referenced as `FirebaseHelper` in the comments) at approximately lines 193-356. It compiles fine because it's all comments, but it's misleading and confuses anyone auditing the diff against the sibling repo.

Delete the commented Firebase block.

### 3. `.kotlin/` and most of `.idea/` are tracked

Same `.gitignore` template as the sibling repo — excludes `local.properties`, `/build`, `/captures`, `.gradle`, `*.iml`, `.DS_Store`, and a handful of cherry-picked `.idea/*.xml` files. The rest of `.idea/` and the `.kotlin/` Kotlin compiler build cache are tracked. Replace `.gitignore` with the standard Android template (entire `.idea/` excluded, `.kotlin/` excluded) and:

```bash
git rm -r --cached .idea .kotlin
```

### 4. Room database uses destructive migration — and there's no cloud backup here

`data/local/AppDatabase.kt` calls `fallbackToDestructiveMigration()`. Every schema bump wipes the user's tasks. The sibling repo has Firestore as a fallback; **this offline build does not**, so a schema-migration mistake = the user permanently loses every task. Write proper `Migration` objects.

### 5. `AlarmManagerHelper` does not gate `SCHEDULE_EXACT_ALARM` on Android 12+

`utils/AlarmManagerHelper.kt` calls `AlarmManager.setExactAndAllowWhileIdle` directly. On Android 12+ this throws `SecurityException` if the user has revoked `SCHEDULE_EXACT_ALARM`. Wrap with `canScheduleExactAlarms()` and bounce to `Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM` if denied.

### 6. The README understated real features and listed wrong directory paths

The previous README:

- Listed only "offline / Morning-Afternoon-Tonight / Today screen / dynamic UI / Kotlin+MVVM+Room" — and **omitted** the search-by-title `SearchView`, the Flagged / Completed / All / Scheduled filter screens, the high-priority "Task Reminders" notification channel, the exact-alarm reminders, the ultra-ptr pull-to-refresh, the BlurView behind the time-picker dialog, the bottom-sheet new-reminder UI, and the leftover (offline-meaningless) iCloud / Outlook sidebar entries.
- Claimed a project structure with `data/database/` and `data/model/` directories that **do not exist**. The actual paths are `data/local/dao/` and `data/models/`. The diagram also missed `adapters/`, `receivers/`, and `utils/` entirely.
- Glossed over the `applicationId = com.shayan.remindersios` (this is an Android app despite the "ios" in the package) and the vendored BlurView `:library` module.

This file is the corrected version.

### 7. The "iCloud" and "Outlook" sidebar entries are leftover from the Firebase fork

Both fragments were carried over from the sibling repo:

- `iCloudFragment.kt` shows every task with no actual iCloud source. The sibling at least had Firestore behind it; here, **there is no sync source at all** — the label is decorative.
- `OutlookFragment.kt` is a stub with a back button and nothing else.

In the offline build these are pure dead UI. Either remove them from `HomeFragment` and `nav_graph.xml`, or relabel them.

### 8. Vendored BlurView needs licence attribution; no top-level LICENSE either

The `:library` module is a vendored copy of [Dimezis/BlurView](https://github.com/Dimezis/BlurView) (Apache-2.0 upstream). Preserve the BlurView Apache-2.0 licence text under `app/library/LICENSE` (and a `NOTICE` for attribution), and decide what licence applies to the rest of the repo. There is currently no top-level `LICENSE` file.

## What the app actually does

1. **Launch.** `MainActivity` hosts a `NavHostFragment`. No auth — straight to `HomeFragment`.
2. **Dashboard.** `HomeFragment` shows tile counts: **Today, Scheduled, All, Flagged, Completed**, plus the leftover (offline-meaningless) "iCloud" and "Outlook" tiles. A `SearchView` queries `Tasks.title` via a Room `LIKE`.
3. **Create.** `NewReminderFragment` is a bottom-sheet form: title, notes, date, time (Morning / Afternoon / Tonight tag), flag toggle. While the time picker is open, the host fragment background is rendered behind a **BlurView** (vendored `:library` module). On Save: writes a row to Room (`Tasks` entity) and schedules an exact `AlarmManager` alarm via `AlarmManagerHelper`. (If no time is chosen, defaults to **11:00 AM** the same day.)
4. **List filters.**
   - `TodayFragment` — today's tasks, grouped Morning (5–11) / Afternoon (12–16) / Tonight (else). **Pull-to-refresh** via `ultra-ptr` `PtrClassicFrameLayout` wired through `utils/PullToRefreshUtil.kt`.
   - `ScheduledFragment` — future-dated tasks grouped by month (up to 12 months ahead).
   - `FlaggedFragment` — `flag = true`.
   - `CompletedFragment` — `isCompleted = true`. Has clear-all-completed.
   - `AllFragment` — every task.
5. **Detail.** `TaskDetailsFragment` is read-only, parcelled in via `kotlin-parcelize`.
6. **Notify.** `TaskReminderReceiver` (a `BroadcastReceiver` registered in the manifest) catches the `AlarmManager` `PendingIntent` and posts a notification on the high-priority "Task Reminders" channel via `utils/Notification.kt`. Tapping the notification opens `MainActivity`.

## Permissions

Declared in `app/src/main/AndroidManifest.xml`:

| Permission | Why |
| --- | --- |
| `SCHEDULE_EXACT_ALARM` | Exact reminder alarms on Android 12+ |
| `FOREGROUND_SERVICE` | Declared but **unused** — no foreground service is started; drop |
| `POST_NOTIFICATIONS` | Notification runtime permission on Android 13+ |

(There is no `INTERNET` permission requested by the app — Firebase is gone, so it's not needed.)

## Tech stack

- **AGP** 8.10.1 (newer than the sibling's 8.7.3). **Kotlin** 2.0.21. **Java target** 11. **KSP** for Room. **kotlin-parcelize**.
- **compileSdk** 35, **targetSdk** 35, **minSdk** 26.
- **Room** 2.6.1 (runtime + ktx + ksp).
- **Jetpack Navigation** 2.9.0. **Lifecycle ViewModel ktx** + **LiveData ktx**. **Coroutines Android**.
- **SwipeRefreshLayout** 1.1.0. **ultra-ptr** 1.0.11 (`in.srain.cube:ultra-ptr` — pull-to-refresh).
- **Material**, **AppCompat**, **ConstraintLayout**, **RecyclerView**, **Activity** + **Fragment** ktx.
- **`:library`** module — vendored copy of [Dimezis/BlurView](https://github.com/Dimezis/BlurView), namespace `eightbitlab.com.blurview`, applies `de.mannodermaus.android-junit5 1.8.2.0`, `compileSdkVersion 34`, `minSdkVersion 18`, Java 1.8.
- **viewBinding** on. Release: `isMinifyEnabled = false` (sibling has minify on — consider enabling here too).
- **No** Firebase / Hilt / Compose / Retrofit / OkHttp / WorkManager / DataStore.

## Project layout

```
to-do-list-offline/
├── README.md
├── build.gradle.kts                                root
├── settings.gradle.kts                             project name "Reminders (IOS)"; includes :app + :library (path app/library)
├── gradle/libs.versions.toml                       version catalog
├── gradle.properties
├── gradlew, gradlew.bat
├── .gitignore                                      🚨 cherry-picked .idea exclusions; .kotlin/ + most of .idea/ tracked
├── screenshots/                                    IMG_3339.JPG, IMG_3340.JPG, IMG_3343.JPG
└── app/
    ├── build.gradle.kts                            applicationId com.shayan.remindersios; isMinifyEnabled = false
    ├── library/                                    :library module — vendored BlurView (Apache-2.0)
    │   ├── build.gradle                            namespace eightbitlab.com.blurview; compileSdk 34
    │   ├── proguard-rules.pro
    │   └── src/                                    BlurView Java sources
    └── src/main/
        ├── AndroidManifest.xml                     🚨 TaskReminderReceiver exported="true"
        ├── java/com/shayan/remindersios/
        │   ├── adapters/TaskAdapter.kt
        │   ├── data/local/AppDatabase.kt           Room v3, fallbackToDestructiveMigration()
        │   ├── data/local/dao/TasksDao.kt
        │   ├── data/models/Tasks.kt                @Parcelize (no firebaseTaskId — only data-model diff vs sibling)
        │   ├── data/repository/Repository.kt       🚨 ~163 lines of commented dead Firebase code (~lines 193-356)
        │   ├── receivers/TaskReminderReceiver.kt
        │   ├── ui/MainActivity.kt
        │   ├── ui/fragments/
        │   │   ├── HomeFragment.kt                 tile dashboard + SearchView
        │   │   ├── NewReminderFragment.kt          bottom-sheet + BlurView behind time picker
        │   │   ├── TodayFragment.kt                ultra-ptr pull-to-refresh
        │   │   ├── ScheduledFragment.kt
        │   │   ├── FlaggedFragment.kt, CompletedFragment.kt, AllFragment.kt
        │   │   ├── TaskDetailsFragment.kt
        │   │   ├── iCloudFragment.kt               ⚠ leftover from Firebase fork — no offline meaning
        │   │   └── OutlookFragment.kt              ⚠ stub, back button only
        │   ├── ui/viewmodel/ViewModel.kt
        │   └── utils/{AlarmManagerHelper, Notification, PullToRefreshUtil, ToastExtensions}.kt
        └── res/
            ├── layout/                             activity_main + ~14 fragment XMLs
            ├── menu/                               toolbar entries
            ├── navigation/nav_graph.xml            ~10 destinations
            ├── values/strings.xml                  app_name = "Reminders"; theme = Theme.RemindersIOS
            ├── xml/{backup_rules, data_extraction_rules}.xml
            └── drawable*/, mipmap-*/               ic_launcher + UI icons
```

## Setup / run

1. Open in Android Studio (Hedgehog or newer for AGP 8.10.1). Sync Gradle.
2. No external services to configure — fully offline.
3. Run on Android 8+ (`minSdk 26`). On Android 12+, grant `SCHEDULE_EXACT_ALARM` in system settings or alarms will fail (the app does not currently bounce to the settings page).
4. `./gradlew :app:assembleDebug`.

## Status

- Working tree clean on `master`. **31 commits** — about half are descriptive (`Maintainence`, `Major refactor: ...`, `Improved New Remainders Fragment`, `BlurView behind timePicker`, `Added Pull-to-Refresh functionality`, etc.) and about half are message `.` or `complete`. **No GitPulse pollution.**
- Remote: `https://github.com/shayann07/to-do-list-offline.git`.
- **No `LICENSE` file.** Vendored `:library` module is upstream Apache-2.0 — preserve attribution if redistributing.
- **No tests.** JUnit + Espresso + AndroidJUnitRunner are declared in `app/build.gradle.kts` but `app/src/test/` and `app/src/androidTest/` are empty.
