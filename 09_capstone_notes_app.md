# Lesson 09 — Capstone Project: Notes App 📝

Goal: build ONE complete app using everything from lessons 01–08.

## What you're building

```
LoginActivity                NotesActivity
┌─────────────────┐          ┌─────────────────┐
│  [Sign in with  │          │  + Add note     │
│   Google] btn   │  ──────▶ │  ┌───────────┐  │
│  Email + pass   │  Google  │  │ note 1    │  │  realtime
│  [Sign in]      │  / email │  │ note 2    │  │  from
└─────────────────┘          │  └───────────┘  │  Firestore
                             └─────────────────┘
```

**Features:** Google Sign-In → skip login if already signed in →
realtime notes list (own notes only) → add / edit / delete →
sign out (both layers!) → secured by Firestore rules.

## Architecture (keep it simple)

```
LoginActivity.kt     → auth only (lessons 03–04)
NotesActivity.kt     → Firestore CRUD (lesson 05)
Note.kt              → data class
activity_login.xml   → login UI
activity_notes.xml   → notes UI (RecyclerView)
```

## Step 1 — Data class (Note.kt)

```kotlin
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Note(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val uid: String = "",
    @ServerTimestamp val createdAt: Date? = null
)
```

## Step 2 — LoginActivity (see full file: sample_GoogleSignInActivity.kt)

Key logic:
1. `onStart()` → if `Firebase.auth.currentUser != null`, go to `NotesActivity`
2. Google button → `googleSignInLauncher.launch(signInIntent)` → `signInWithCredential`
3. Email button → `signInWithEmailAndPassword`
4. On success → `startActivity(Intent(this, NotesActivity::class.java))` + `finish()`

## Step 3 — NotesActivity (see full file: sample_FirestoreNotesActivity.kt)

Key logic:
1. `uid = Firebase.auth.currentUser?.uid ?: returnToLogin()`
2. `addSnapshotListener` on `notes where uid == uid, order by createdAt DESC`
3. FAB (+) → dialog with title/text fields → `collection("notes").add(Note(...))`
4. Tap note → edit dialog → `document(id).update(...)`
5. Long-press → confirm → `document(id).delete()`
6. Menu → Sign out → `Firebase.auth.signOut()` + `googleSignInClient.signOut()`

## Step 4 — Firestore rules (lesson 08)

```javascript
match /notes/{noteId} {
  allow read, update, delete:
    if request.auth != null && request.auth.uid == resource.data.uid;
  allow create:
    if request.auth != null && request.auth.uid == request.resource.data.uid;
}
```

## Step 5 — Test the whole flow

- [ ] Fresh install → login screen shows
- [ ] Google sign-in → notes screen, name/photo from Google account
- [ ] Add note → appears instantly (realtime!)
- [ ] Kill & reopen app → still signed in, notes still there (offline cache!)
- [ ] Sign out → back to login; sign in again works
- [ ] Rules playground: user A cannot read user B's notes

## Stretch goals (pick any)

1. **Attach photos** to notes (lesson 06: upload → save URL in the note)
2. **Push notification** when a note is added on another device (lesson 07)
3. **Search** notes by title (`whereGreaterThanOrEqualTo("title", query)`)
4. **Dark mode** toggle
5. **Email verification** gate before entering the app (lesson 03 §8)

Ship it. This app already does more than most tutorial apps ever get to. 🚀
