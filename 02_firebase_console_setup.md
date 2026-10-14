# Lesson 02 — Firebase Console Setup

Goal: create a Firebase project, register your Android app, and wire up Gradle.
Do this carefully — 90% of "Firebase not working" issues come from this lesson.

## 1. Create a Firebase project

1. Go to **https://console.firebase.google.com** and sign in with your Google account.
2. **Add project** → name it `fire-course` (or anything) → Continue.
3. Google Analytics: you can **disable** it for this course (fewer steps). Continue.
4. Wait ~30 seconds → **Continue** → you're in the console.

## 2. Register your Android app

1. On the project overview page, click the **Android icon** (Add an app).
2. **Android package name:** must EXACTLY match your app's `applicationId`
   (from lesson 01, e.g. `com.example.firecourse`). This is case-sensitive!
3. **App nickname:** `FireCourse` (optional, only for your reference).
4. **Debug signing certificate SHA-1** — ⚠️ REQUIRED for Google Sign-In (lesson 04).
   Get it from Android Studio's terminal:
   ```bash
   ./gradlew signingReport
   ```
   Look for the `debug` variant → copy the **SHA1** value → paste it here.
   (On Windows use `gradlew signingReport` without `./`.)
5. Click **Register app**.

> Adding the SHA-1 now saves you a painful "Error 12500 / developer error 10"
> later. Don't skip it.

## 3. Download google-services.json

1. After registering, Firebase offers **`google-services.json`** → **Download** it.
2. In Android Studio, switch to **Project view** and drop the file into the **`app/`** folder:
   ```
   FireCourse/
   └── app/
       ├── build.gradle
       └── google-services.json   ← HERE (app-level, NOT inside src/)
   ```
3. Click **Next** a couple of times → **Continue to console**.

> ⚠️ Never commit `google-services.json` to a PUBLIC repo if the project is real —
> it contains API keys. (For this course it's fine, but build the habit:
> add it to `.gitignore`.)

## 4. Gradle setup

**Project-level** `build.gradle` — add the Google Services plugin:

```groovy
plugins {
    id 'com.android.application' version '8.5.2' apply false
    id 'org.jetbrains.kotlin.android' version '1.9.24' apply false
    // Add this line:
    id 'com.google.gms.google-services' version '4.4.2' apply false
}
```

**App-level** `app/build.gradle` — apply the plugin and add Firebase dependencies:

```groovy
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
    // Add this line (must be at the BOTTOM of the plugins block):
    id 'com.google.gms.google-services'
}

android {
    // ... your existing config
}

dependencies {
    // Firebase BoM: one version to rule them all.
    // Check https://firebase.google.com/docs/android/setup for the latest number.
    implementation platform('com.google.firebase:firebase-bom:34.0.0')

    implementation 'com.google.firebase:firebase-auth'       // login
    implementation 'com.google.firebase:firebase-firestore'  // database
    implementation 'com.google.firebase:firebase-storage'    // files
    implementation 'com.google.firebase:firebase-messaging' // push notifications

    // Google Sign-In (lesson 04)
    implementation 'com.google.android.gms:play-services-auth:21.2.0'
}
```

Click **Sync Now**. The `google-services` plugin reads `google-services.json`
and generates `R.string.default_web_client_id` etc. automatically.

> A ready-to-paste version of both Gradle blocks is in `gradle_setup_snippets.txt`.

## 5. Verify the connection

Add this temporary check in `MainActivity.kt`:

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val auth = Firebase.auth
        // If this line runs without crashing, Firebase is wired up correctly.
        android.util.Log.d("FireCourse", "Firebase OK, current user: ${auth.currentUser}")
    }
}
```

Run the app and check **Logcat** (bottom panel, filter by `FireCourse`).
You should see: `Firebase OK, current user: null` — null just means nobody is
signed in yet, which is expected. No crash = success. ✅

## Common errors

| Problem | Fix |
|---|---|
| `File google-services.json is missing` | It's in the wrong folder — must be directly under `app/`, not `app/src/` |
| `No matching client found for package name` | Package name in Firebase console ≠ `applicationId`. Must match exactly |
| Gradle sync fails on `google-services` plugin | Plugin id must also be in the **project-level** file with `apply false` |
| App crashes on `Firebase.auth` | You forgot `id 'com.google.gms.google-services'` in the **app-level** plugins |

Next: `03_email_password_auth.md` — your first real auth flow.
