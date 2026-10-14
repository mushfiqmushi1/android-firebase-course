# Lesson 07 — Push Notifications (FCM)

Goal: receive push notifications, even when the app is closed.

## 1. How FCM works

```
Your server / Firebase console → FCM → Google Play services → your app
                                              (each device has a TOKEN)
```

Each app install gets a unique **registration token**. To message a device,
you send to its token. Tokens can change — always handle refreshes.

## 2. The service that receives messages

Create `MyMessagingService.kt`:

```kotlin
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyMessagingService : FirebaseMessagingService() {

    // Called when a DATA message arrives while the app is in FOREGROUND.
    // (Notification messages show automatically when app is in BACKGROUND.)
    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: "New message"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    // Called when FCM issues a NEW token — send it to YOUR server / Firestore
    // so you know where to send notifications later.
    override fun onNewToken(token: String) {
        Log.d("FCM", "New token: $token")
        saveTokenToFirestore(token)
    }
}
```

Register it in `AndroidManifest.xml`:

```xml
<service
    android:name=".MyMessagingService"
    android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

## 3. Get the token & save it

```kotlin
import com.google.firebase.messaging.FirebaseMessaging

FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
    if (task.isSuccessful) {
        val token = task.result
        // Store under the user's document so you can target them later
        Firebase.firestore.collection("users")
            .document(Firebase.auth.currentUser!!.uid)
            .update("fcmToken", token)
    }
}
```

> Call this after sign-in (lessons 03–04) and in `onNewToken`.
> Best practice: one token per user document, updated on every refresh.

## 4. Show the notification (with channel — required on Android 8+)

```kotlin
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat

private fun showNotification(title: String, body: String) {
    val channelId = "default_channel"
    val manager = getSystemService(NotificationManager::class.java)

    // Android 8+: notifications MUST belong to a channel
    manager.createNotificationChannel(
        NotificationChannel(channelId, "General", NotificationManager.IMPORTANCE_DEFAULT)
    )

    val notification = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_notification)  // REQUIRED — else it crashes!
        .setContentTitle(title)
        .setContentText(body)
        .setAutoCancel(true)
        .build()

    manager.notify(System.currentTimeMillis().toInt(), notification)
}
```

> ⚠️ `setSmallIcon()` is mandatory — a missing icon = silent crash.
> Use a simple white-on-transparent icon.

## 5. Android 13+ permission

```kotlin
// In your launcher Activity (Android 13 / API 33+):
if (Build.VERSION.SDK_INT >= 33) {
    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
}

private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    Log.d("FCM", "Notification permission: $granted")
}
```

Add to manifest:
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

## 6. Send a test notification (no server needed!)

1. Firebase console → **Engagement → Messaging → Create your first campaign**.
2. Enter title + text → Next.
3. **Target:** select your app → Next → Publish.
4. Your device gets the notification in seconds. 🎉

> Later, for real apps, you'll send via the FCM HTTP API from your backend
> (or Cloud Functions), targeting stored tokens or topics.

## 7. Topics (optional, powerful)

```kotlin
// Subscribe — then send ONE message to the topic to reach ALL subscribers
FirebaseMessaging.getInstance().subscribeToTopic("news")
    .addOnCompleteListener { Log.d("FCM", "Subscribed to news") }
```

Topics = mailing lists for devices. Great for announcements, no token
management needed.

## Common errors

| Problem | Fix |
|---|---|
| Nothing arrives, app in background | Use a **Notification** message (console campaign) — data-only messages need foreground handling |
| Crash on show | Missing `setSmallIcon()` |
| No notification on Android 13+ | `POST_NOTIFICATIONS` permission not granted |
| Token is null | Google Play services missing/outdated on the device/emulator |

Next: `08_security_rules.md` — lock it all down.
