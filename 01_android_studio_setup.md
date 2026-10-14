# Lesson 01 — Android Studio Setup

Goal: install Android Studio and create your first Kotlin project.

## 1. Install Android Studio

1. Go to **https://developer.android.com/studio** and download the latest stable version
   (Windows / macOS / Linux all supported).
2. Run the installer. On the setup wizard, keep everything default, but make sure
   **Android SDK**, **Android SDK Platform** and **Android Virtual Device** are checked.
3. On first launch, the wizard downloads the SDK components (~2–3 GB). Let it finish.

> No Android phone? No problem — you'll create a virtual device (emulator) in step 3.

## 2. Create your first project

1. **New Project → Empty Activity** (the modern template).
2. Settings:
   - **Name:** `FireCourse`
   - **Package name:** `com.example.firecourse` ← remember this, Firebase needs it later
   - **Language:** Kotlin
   - **Minimum SDK:** API 24 (covers ~95%+ of devices, keeps things simple)
   - **Build configuration language:** Groovy (build.gradle) — most tutorials use this
3. Click **Finish** and wait for the Gradle sync (first sync downloads dependencies, takes a few minutes).

## 3. Create an emulator (if you have no physical device)

1. **Tools → Device Manager → Create Device**
2. Pick **Pixel 7** (or any recent Pixel) → Next
3. Download a system image: **API 34, Google Play** variant
   (the *Google Play* variant matters later for testing Google Sign-In properly)
4. Finish. Launch it once with the ▶ button to verify it boots.

## 4. Run the "Hello World" app

1. Click the green ▶ **Run** button (or Shift+F10).
2. Select your emulator (or your physical device with USB debugging enabled).
3. You should see "Hello Android!" on screen. 🎉

## 5. Project tour — where things live

```
FireCourse/
├── app/
│   ├── build.gradle            ← app-level dependencies (Firebase goes here)
│   ├── google-services.json    ← Firebase config file (you'll add this in lesson 02)
│   └── src/main/
│       ├── AndroidManifest.xml ← app permissions & components
│       ├── java/com/example/firecourse/
│       │   └── MainActivity.kt ← your code
│       └── res/layout/
│           └── activity_main.xml ← your UI
├── build.gradle                ← project-level plugins
└── settings.gradle
```

## Common errors

| Problem | Fix |
|---|---|
| Gradle sync fails / "offline" | File → Settings → Build Tools → Gradle → uncheck Offline Mode, sync again |
| Emulator won't start (HAXM/Hypervisor) | Enable virtualization (VT-x/AMD-V) in BIOS |
| "SDK not found" | File → Project Structure → SDK Location → set your SDK path |

Next: `02_firebase_console_setup.md` — create the Firebase project and connect it.
