package com.example.firecourse

// SAMPLE — Lesson 05 + 09: complete notes screen with realtime Firestore CRUD.
// Copy into app/src/main/java/com/example/firecourse/NotesActivity.kt
// Needs: Note data class (lesson 09), a RecyclerView + adapter (standard Android),
//        and view binding enabled in app/build.gradle.

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firecourse.databinding.ActivityNotesBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.firestore
import java.util.Date

// Firestore maps documents to this automatically (needs the defaults)
data class Note(
    val id: String = "",
    val title: String = "",
    val text: String = "",
    val uid: String = "",
    @ServerTimestamp val createdAt: Date? = null
)

class NotesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotesBinding
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private lateinit var adapter: NotesAdapter  // your RecyclerView adapter

    private val uid: String
        get() = auth.currentUser?.uid ?: run {
            // Safety net: signed out somehow → back to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            ""
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = NotesAdapter(
            onClick = { note -> showNoteDialog(note) },       // tap → edit
            onLongClick = { note -> confirmDelete(note) }     // long-press → delete
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener { showNoteDialog(null) }  // + → new note
        binding.btnSignOut.setOnClickListener { signOut() }

        listenToNotes()
    }

    // ---------- REALTIME READ (Lesson 05 §5) ----------
    private fun listenToNotes() {
        db.collection("notes")
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("Notes", "Listen failed", error)
                    toast("Couldn't load notes: ${error.message}")
                    return@addSnapshotListener
                }
                val notes = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Note::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                adapter.submitList(notes)
            }
    }

    // ---------- CREATE / UPDATE dialog ----------
    private fun showNoteDialog(existing: Note?) {
        val view = layoutInflater.inflate(R.layout.dialog_note, null)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etText = view.findViewById<EditText>(R.id.etText)
        existing?.let { etTitle.setText(it.title); etText.setText(it.text) }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "New note" else "Edit note")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isEmpty()) {
                    toast("Title can't be empty"); return@setPositiveButton
                }
                val text = etText.text.toString().trim()
                if (existing == null) addNote(title, text)
                else updateNote(existing.id, title, text)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNote(title: String, text: String) {
        db.collection("notes")
            .add(Note(title = title, text = text, uid = uid))
            .addOnFailureListener { e -> toast("Save failed: ${e.message}") }
        // No success toast needed — the realtime listener updates the list instantly
    }

    private fun updateNote(id: String, title: String, text: String) {
        db.collection("notes").document(id)
            .update(mapOf("title" to title, "text" to text))
            .addOnFailureListener { e -> toast("Update failed: ${e.message}") }
    }

    // ---------- DELETE ----------
    private fun confirmDelete(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Delete note?")
            .setMessage("\"${note.title}\" will be permanently deleted.")
            .setPositiveButton("Delete") { _, _ ->
                db.collection("notes").document(note.id).delete()
                    .addOnFailureListener { e -> toast("Delete failed: ${e.message}") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ---------- SIGN OUT (both layers — Lesson 04 §6) ----------
    private fun signOut() {
        auth.signOut()
        // NOTE: for full Google sign-out also call googleSignInClient.signOut().
        // Rebuild the client here or keep it in a shared AuthManager.
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
