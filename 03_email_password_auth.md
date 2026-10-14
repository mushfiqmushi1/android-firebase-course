# Lesson 03 — Email & Password Authentication

Goal: sign up, sign in, sign out and password reset with Firebase Auth.

## 1. Enable the provider

1. Firebase console → **Build → Authentication → Get started**.
2. **Sign-in method** tab → **Email/Password** → **Enable** → Save.
   (Leave "Email link" off — that's a different flow.)

## 2. The Auth object

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Get the shared FirebaseAuth instance (do this once per Activity)
val auth = Firebase.auth
```

## 3. Sign up (create account)

```kotlin
fun signUp(email: String, password: String) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // task.result.user is now signed in
                val user = auth.currentUser
                toast("Welcome, ${user?.email}!")
                goToMainScreen()
            } else {
                // task.exception?.message explains what went wrong
                toast("Sign up failed: ${task.exception?.message}")
            }
        }
}
```

Firebase validates for you: malformed email, password < 6 chars, or an
already-registered email all come back as failures with clear messages.

## 4. Sign in

```kotlin
fun signIn(email: String, password: String) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                goToMainScreen()
            } else {
                toast("Sign in failed: ${task.exception?.message}")
            }
        }
}
```

## 5. Keep the user signed in (auth state listener)

Firebase persists the session automatically — the user stays signed in
across app restarts. React to it with a listener:

```kotlin
private lateinit var authListener: FirebaseAuth.AuthStateListener

override fun onStart() {
    super.onStart()
    authListener = FirebaseAuth.AuthStateListener { auth ->
        if (auth.currentUser != null) {
            // Already signed in → skip the login screen
            goToMainScreen()
        }
    }
    auth.addAuthStateListener(authListener)
}

override fun onStop() {
    super.onStop()
    auth.removeAuthStateListener(authListener)  // avoid leaks
}
```

## 6. Sign out & password reset

```kotlin
// Sign out — one line
auth.signOut()

// Password reset email
auth.sendPasswordResetEmail(email)
    .addOnCompleteListener { task ->
        toast(if (task.isSuccessful) "Reset email sent" else "Failed: ${task.exception?.message}")
    }
```

## 7. Reading the current user

```kotlin
val user = auth.currentUser
user?.let {
    val email = it.email            // "mushi@example.com"
    val uid = it.uid                // unique ID — use as the database key!
    val name = it.displayName       // null for email users until you set it
    val photo = it.photoUrl         // null for email users
}
```

> **The `uid` is the most important value in Firebase.** It's stable, unique per
> user, and you'll use it as the key for their data in Firestore (lesson 05).

## 8. (Optional) Email verification

```kotlin
auth.currentUser?.sendEmailVerification()
    .addOnCompleteListener { task ->
        toast(if (task.isSuccessful) "Verification sent" else "Failed")
    }

// Later, check:
val isVerified = auth.currentUser?.isEmailVerified == true
```

## Common errors

| Problem | Fix |
|---|---|
| "There is no user record" on sign-in | That email never signed up — call createUser first, or show a proper message |
| "The email address is badly formatted" | Validate with `android.util.Patterns.EMAIL_ADDRESS` before calling Firebase |
| Password reset never arrives | Check spam; also the sender is `noreply@<project>.firebaseapp.com` |

Next: `04_google_sign_in.md` — the one-tap Google button. ⭐
