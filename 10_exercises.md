# Practice Exercises 🧠

Build these to lock in each lesson. Struggle a little — that's the learning.

---

## 01–02 — Setup
1. Create the project, connect Firebase, and get `Firebase OK, current user: null`
   in Logcat. Screenshot it — that's your "hello world".
2. Run `./gradlew signingReport` and find your debug SHA-1. Add it to the
   Firebase console. (You'll thank yourself in lesson 04.)

## 03 — Email Auth
3. Build a login screen with email + password fields and Sign up / Sign in buttons.
   Show Firebase's error message in a Toast on failure.
4. Add "skip login if already signed in" with an `AuthStateListener`.
5. Add a "Forgot password?" button using `sendPasswordResetEmail`.

## 04 — Google Sign-In
6. Add the Google Sign-In button. Sign in, then display the user's
   **display name and photo** (from `currentUser`) on the next screen.
7. Implement sign-out so that pressing it TWICE in a row still lands on the
   login screen (hint: both auth layers — lesson 04 §6).
8. **Debug drill:** temporarily remove your SHA-1 from the Firebase console,
   run, and observe error 12500/10. Put it back. Now you'll recognize it forever.

## 05 — Firestore
9. Save your user profile (`name`, `email`, `uid`) to `users/<uid>` on first sign-in.
10. Build the notes CRUD from lesson 09: add, realtime list, edit, delete.
11. Add a query: show only notes whose title contains a search string.
    (Hint: `whereGreaterThanOrEqualTo` / `whereLessThanOrEqualTo` with `\uf8ff`.)
12. Store a `createdAt` server timestamp and sort newest-first.

## 06 — Storage
13. Let users pick a profile picture and upload it to `images/<uid>/avatar.jpg`.
    Show upload progress in a ProgressBar.
14. Save the download URL to the user's Firestore document and display it
    with Coil on every app start.

## 07 — FCM
15. Save the FCM token to the user's Firestore document on sign-in.
16. Send yourself a test notification from the Firebase console. Screenshot it.
17. Subscribe to a topic (e.g. `announcements`) and send a topic message.

## 08 — Security Rules
18. Publish the rules from lesson 08. Then, in the Rules playground:
    - simulate user A reading user A's note → expect ALLOW
    - simulate user A reading user B's note → expect DENY
    - simulate signed-out read → expect DENY
19. Add a rule: note titles max 100 characters. Try writing a longer one from
    the app and watch it fail — then handle the error gracefully.

## 09 — Capstone stretch
20. **Final boss:** finish the notes app + any TWO stretch goals from lesson 09
    (photos, push, search, dark mode, email verification).
    When it works end-to-end on a real device — you've completed the course. 🏆
