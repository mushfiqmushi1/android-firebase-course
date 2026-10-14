# Lesson 05 — Cloud Firestore: CRUD

Goal: a realtime NoSQL database — create, read, update, delete, and listen live.

## 1. Create the database

1. Firebase console → **Build → Firestore Database → Create database**.
2. **Start in test mode** (open rules for 30 days — fine while learning;
   lesson 08 locks it down properly).
3. Choose a location (e.g. `asia-south1`) → Enable.

## 2. Mental model (60 seconds)

```
Collection ("notes")          ← like a table
 └── Document (auto-ID)        ← like a row
      ├── title: "Shopping"    ← fields (like columns)
      ├── text: "Buy milk"
      ├── uid: "abc123"        ← WHO owns it (the Firebase Auth uid!)
      └── createdAt: timestamp
```

**Golden rule:** always store the owner's `uid` in each document.
That's how you show users only *their* data — and how security rules
enforce it (lesson 08).

## 3. Get the Firestore instance

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

val db = Firebase.firestore
```

## 4. CREATE — add documents

```kotlin
val uid = Firebase.auth.currentUser?.uid ?: return

val note = hashMapOf(
    "title" to "Shopping list",
    "text" to "Milk, eggs, bread",
    "uid" to uid,
    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
)

db.collection("notes")
    .add(note)  // auto-generates a document ID
    .addOnSuccessListener { docRef ->
        Log.d("Firestore", "Saved with ID: ${docRef.id}")
    }
    .addOnFailureListener { e ->
        Log.w("Firestore", "Error saving", e)
    }
```

## 5. READ — realtime listener (the magic of Firestore)

```kotlin
// Listen to ONLY this user's notes, newest first.
// The callback fires immediately AND on every change — no refresh button needed.
db.collection("notes")
    .whereEqualTo("uid", uid)
    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
    .addSnapshotListener { snapshot, error ->
        if (error != null) {
            Log.w("Firestore", "Listen failed", error)
            return@addSnapshotListener
        }
        val notes = snapshot?.documents?.map { doc ->
            Note(
                id = doc.id,
                title = doc.getString("title") ?: "",
                text = doc.getString("text") ?: ""
            )
        } ?: emptyList()
        adapter.submitList(notes)  // update your RecyclerView
    }
```

> ⚠️ `whereEqualTo` + `orderBy` on different fields needs a **composite index**.
> Firestore throws an error containing a direct link to create it — just click it.
> (Or create it manually in console → Firestore → Indexes.)

One-time read (no listener):

```kotlin
db.collection("notes").document(noteId).get()
    .addOnSuccessListener { doc ->
        if (doc.exists()) Log.d("Firestore", "Title: ${doc.getString("title")}")
    }
```

## 6. UPDATE & DELETE

```kotlin
val docRef = db.collection("notes").document(noteId)

// Update specific fields (others untouched)
docRef.update("title" to "New title", "text" to "New text")

// Delete the document
docRef.delete()
    .addOnSuccessListener { Log.d("Firestore", "Deleted") }
```

## 7. Data class pattern (clean code)

```kotlin
// Firestore can map documents straight to Kotlin data classes
data class Note(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val uid: String = "",
    // @ServerTimestamp fills this in automatically on write
    @com.google.firebase.firestore.ServerTimestamp
    val createdAt: java.util.Date? = null
)

// Write an object directly:
db.collection("notes").add(Note(title = "Hi", text = "Hello", uid = uid))

// Read as objects:
val note = doc.toObject(Note::class.java)
```

> Needs: `import com.google.firebase.firestore.ServerTimestamp`.
> The no-arg defaults (`= ""`) are required — Firestore creates the object via reflection.

## 8. Offline support (free!)

Firestore caches data on-device automatically. Reads work offline;
writes queue up and sync when the connection returns. Nothing to configure.
(You can tune it with `Firebase.firestore.settings`, but defaults are fine.)

## Common errors

| Problem | Fix |
|---|---|
| `PERMISSION_DENIED` | Rules (lesson 08) or still in test mode expired — check console → Rules |
| "The query requires an index" | Click the link in the error message to auto-create it |
| Listener never fires | `addSnapshotListener` must be called on the main thread; also check `error` param |
| Timestamps are null right after write | `serverTimestamp()` resolves on the server — read it back via listener |

Next: `06_firebase_storage.md` — images & files.
