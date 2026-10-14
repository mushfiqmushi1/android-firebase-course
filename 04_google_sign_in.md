# Lesson 04 — Google Sign-In with Firebase Auth ⭐

Goal: the familiar "Sign in with Google" button, wired into Firebase Auth.
This is the lesson most people get stuck on — follow every step.

## How it works (the 30-second version)

```
Your app → Google account picker → Google returns an ID token
         → your app sends the token to Firebase
         → Firebase verifies it with Google → user is signed in ✅
```

Firebase never sees the user's Google password. The ID token is the proof.

## 1. Prerequisites (do not skip!)

- [ ] Lesson 02 done: `google-services.json` in `app/`, Gradle synced
- [ ] **SHA-1 fingerprint** added in Firebase console
  (Project Settings → Your apps → Android app → Add fingerprint).
  Get it via `./gradlew signingReport` → copy the `SHA1` under the `debug` variant.
- [ ] **Google** provider enabled: Firebase console → Authentication →
  Sign-in method → **Google → Enable** → pick a support email → Save.
- [ ] Dependency present (lesson 02): `implementation 'com.google.android.gms:play-services-auth:21.2.0'`

> Missing SHA-1 = the infamous **"Error 12500"** or **"Developer error 10"**.
> If Google Sign-In fails, check the SHA-1 first. Always.

## 2. Configure Google Sign-In options

```kotlin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

// requestIdToken: THE critical line. It asks Google for an ID token
// that Firebase can verify. The value comes free from google-services.json
// via the google-services Gradle plugin — you don't type it yourself.
val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
    .requestIdToken(getString(R.string.default_web_client_id))
    .requestEmail()
    .build()

val googleSignInClient = GoogleSignIn.getClient(this, gso)
```

## 3. Launch the Google account picker (modern API)

```kotlin
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.common.api.ApiException

// Register the launcher (as a property of your Activity)
private val googleSignInLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
) { result ->
    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
    try {
        val account = task.getResult(ApiException::class.java)
        // account.idToken is the proof Firebase needs
        firebaseAuthWithGoogle(account.idToken!!)
    } catch (e: ApiException) {
        Log.w("GoogleSignIn", "signInResult:failed code=${e.statusCode}")
        toast("Google sign-in failed (code ${e.statusCode})")
    }
}

// Call this from your "Sign in with Google" button:
private fun signInWithGoogle() {
    googleSignInLauncher.launch(googleSignInClient.signInIntent)
}
```

## 4. Exchange the token with Firebase

```kotlin
import com.google.firebase.auth.GoogleAuthProvider

private fun firebaseAuthWithGoogle(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    Firebase.auth.signInWithCredential(credential)
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = Firebase.auth.currentUser
                toast("Welcome, ${user?.displayName}!")
                goToMainScreen()
            } else {
                toast("Firebase auth failed: ${task.exception?.message}")
            }
        }
}
```

That's the whole flow. Four pieces: options → picker → token → Firebase credential.

## 5. Skip login when already signed in

```kotlin
override fun onStart() {
    super.onStart()
    // Firebase persists the session; Google accounts too
    if (Firebase.auth.currentUser != null) goToMainScreen()
}
```

## 6. Sign out properly (both layers!)

```kotlin
fun signOut() {
    Firebase.auth.signOut()                       // Firebase layer
    googleSignInClient.signOut().addOnCompleteListener {
        // Google layer — without this, the next sign-in
        // silently reuses the last Google account
        goToLoginScreen()
    }
}
```

> ⚠️ Forgetting `googleSignInClient.signOut()` is the #1 "logout doesn't work" bug.
> Firebase signs out, but Google remembers the account and instantly signs back in.

## 7. Reading Google profile data

```kotlin
val user = Firebase.auth.currentUser
user?.let {
    Log.d("User", "name=${it.displayName}")  // "Mushi Rahman" (from Google!)
    Log.d("User", "email=${it.email}")
    Log.d("User", "photo=${it.photoUrl}")    // profile picture URL — free avatar!
    Log.d("User", "uid=${it.uid}")
}
```

Google users come with display name + photo URL built in — perfect for profile
screens with zero extra work.

## Common errors

| Code / Symptom | Cause | Fix |
|---|---|---|
| `12500` | SHA-1 missing/wrong in Firebase console | Add debug SHA-1 (lesson 02 §2), re-download `google-services.json` |
| `10` (Developer error) | Same as above, or package name mismatch | SHA-1 + exact package name |
| `default_web_client_id` unresolved | google-services plugin not applied / not synced | Check app-level `build.gradle`, Sync Now |
| Sign-in works, then user is null | You read `currentUser` before `signInWithCredential` completed | Use the `addOnCompleteListener` callback |
| Logout "doesn't work" | Only called `Firebase.auth.signOut()` | Also call `googleSignInClient.signOut()` |

> **Note for the future:** Google now recommends the **Credential Manager**
> API instead of `play-services-auth`. It works the same conceptually
> (get ID token → `GoogleAuthProvider.getCredential` → `signInWithCredential`),
> so everything you learned here transfers directly.

Next: `05_firestore_crud.md` — store the user's data in the cloud.
