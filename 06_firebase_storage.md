# Lesson 06 — Firebase Storage

Goal: upload and download images/files (profile pictures, attachments...).

## 1. Create the bucket

1. Firebase console → **Build → Storage → Get started**.
2. **Start in test mode** (lesson 08 secures it).
3. Choose location → Done. You now have a cloud file bucket.

## 2. Upload a file

```kotlin
import com.google.firebase.Firebase
import com.google.firebase.storage.storage

val storage = Firebase.storage
val uid = Firebase.auth.currentUser?.uid ?: return

// Pick an image with the modern photo picker (no permission needed!)
private val pickImage = registerForActivityResult(
    ActivityResultContracts.PickVisualMedia()
) { uri ->
    if (uri != null) uploadImage(uri)
}

// Launch it from a button:
pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
```

```kotlin
fun uploadImage(uri: Uri) {
    // Organize files per user: images/<uid>/<random>.jpg
    val fileName = "${java.util.UUID.randomUUID()}.jpg"
    val ref = storage.reference.child("images/$uid/$fileName")

    ref.putFile(uri)
        .addOnProgressListener { task ->
            val percent = (100.0 * task.bytesTransferred / task.totalByteCount).toInt()
            progressBar.progress = percent   // show upload progress!
        }
        .addOnSuccessListener {
            // Upload done — now get the public download URL
            ref.downloadUrl.addOnSuccessListener { downloadUrl ->
                Log.d("Storage", "URL: $downloadUrl")
                // Save this URL in Firestore (lesson 05) to display the image later
                savePhotoUrlToProfile(downloadUrl.toString())
            }
        }
        .addOnFailureListener { e ->
            Log.w("Storage", "Upload failed", e)
        }
}
```

> **Two-step pattern:** `putFile()` uploads the bytes; `downloadUrl` gives you the
> URL to store/display. Beginners always forget the second step!

## 3. Download & display

```kotlin
// Option A — download to a local file
val localFile = File.createTempFile("avatar", ".jpg")
ref.getFile(localFile).addOnSuccessListener {
    imageView.setImageURI(Uri.fromFile(localFile))
}

// Option B — just use the download URL with an image library (recommended)
// With Coil (add: implementation("io.coil-kt:coil:2.6.0")):
imageView.load(downloadUrl)   // one line, handles caching for you
```

## 4. Delete & metadata

```kotlin
// Delete
ref.delete().addOnSuccessListener { Log.d("Storage", "Deleted") }

// Get file info
ref.metadata.addOnSuccessListener { meta ->
    Log.d("Storage", "size=${meta.sizeBytes}, type=${meta.contentType}")
}
```

## 5. Folder listing (use sparingly)

```kotlin
storage.reference.child("images/$uid").listAll()
    .addOnSuccessListener { result ->
        result.items.forEach { itemRef ->
            Log.d("Storage", "file: ${itemRef.name}")
        }
    }
```

> `listAll()` loads everything into memory — fine for a user's 20 photos,
> terrible for 20,000. For big collections, track file paths in Firestore instead.

## Common errors

| Problem | Fix |
|---|---|
| `Object does not exist` on download | The path is wrong — Storage paths are case-sensitive |
| Upload succeeds but URL is null | You must call `ref.downloadUrl` AFTER `putFile` succeeds (separate call!) |
| `PERMISSION_DENIED` | Storage rules — see lesson 08 |
| Huge uploads on mobile data | Compress first: decode the bitmap, `compress(Bitmap.CompressFormat.JPEG, 70, stream)` |

Next: `07_push_notifications_fcm.md` — reach your users anytime.
