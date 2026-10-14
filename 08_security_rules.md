# Lesson 08 — Security Rules

Goal: replace "test mode" with rules that protect real user data.
**Test-mode rules expire after 30 days** — your app WILL break if you forget this.

## 1. The mindset

> Firebase security is NOT in your app code — it's in the **rules**.
> Anyone can decompile your app, so the server must enforce who can read/write what.

Rules get three facts about each request:
- `request.auth` — who is asking (null if signed out)
- `request.resource.data` — the data being WRITTEN
- `resource.data` — the data already STORED (for read/update/delete)

## 2. Firestore rules — users own their data

Console → Firestore Database → **Rules** tab:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Notes: signed-in users can only touch THEIR OWN notes
    match /notes/{noteId} {
      allow read, update, delete:
        if request.auth != null
        && request.auth.uid == resource.data.uid;

      allow create:
        if request.auth != null
        && request.auth.uid == request.resource.data.uid;
    }

    // User profiles: anyone signed in can READ, but only the owner can WRITE
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

Why `resource.data` vs `request.resource.data`?
- **create**: nothing stored yet → check the *incoming* data (`request.resource`)
- **read/update/delete**: document exists → check the *stored* data (`resource`)

Click **Publish**. Test immediately with the rules playground
(console → Rules → **Rules playground** — simulate reads/writes as any uid).

## 3. Storage rules — same idea for files

Console → Storage → **Rules** tab:

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {

    // Each user gets their own folder: images/<uid>/...
    match /images/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null
                   && request.auth.uid == userId
                   // bonus: only images, max 5 MB
                   && request.resource.contentType.matches('image/.*')
                   && request.resource.size < 5 * 1024 * 1024;
    }
  }
}
```

## 4. Validation in rules (pro move)

Rules can enforce data SHAPE, not just access:

```javascript
match /notes/{noteId} {
  allow create: if request.auth != null
    && request.auth.uid == request.resource.data.uid
    && request.resource.data.title is string
    && request.resource.data.title.size() > 0
    && request.resource.data.title.size() <= 100;
}
```

Now junk data can't even be written — the server rejects it.

## 5. Checklist before "launch"

- [ ] No `allow read, write: if true;` anywhere (that's test mode!)
- [ ] Every collection has a rule — unmatched paths are **denied by default** ✅
- [ ] Tested with the Rules playground: owner allowed, stranger denied, signed-out denied
- [ ] Storage has size + content-type limits

## Common errors

| Problem | Fix |
|---|---|
| `PERMISSION_DENIED` after publishing | Your rule is stricter than your code — check `uid` field exists on the document |
| Rules work in playground, fail in app | The app's `uid` doesn't match — print `auth.currentUser?.uid` and compare |
| "Missing or insufficient permissions" | Same as above — also check you're not reading a collection with no rule |

Next: `09_capstone_notes_app.md` — put it ALL together. 🚀
