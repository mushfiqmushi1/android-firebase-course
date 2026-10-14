package com.example.firecourse

// SAMPLE — Lesson 03 + 04 combined: complete login screen.
// Copy into app/src/main/java/com/example/firecourse/LoginActivity.kt
// Layout: sample_activity_login.xml

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.firecourse.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // Modern replacement for onActivityResult — handles the Google picker result
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("Login", "Google sign-in failed: ${e.statusCode}", e)
            toast("Google sign-in failed (code ${e.statusCode})")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // --- Google Sign-In setup (Lesson 04) ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- Button wiring ---
        binding.btnGoogle.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
        binding.btnEmailSignIn.setOnClickListener { emailSignIn() }
        binding.btnEmailSignUp.setOnClickListener { emailSignUp() }
        binding.tvForgotPassword.setOnClickListener { resetPassword() }
    }

    override fun onStart() {
        super.onStart()
        // Already signed in? Skip this screen entirely.
        if (auth.currentUser != null) goToNotes()
    }

    // ---------- Email & password (Lesson 03) ----------

    private fun emailSignUp() {
        val (email, pass) = readInputs() ?: return
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("Welcome, $email!")
                    goToNotes()
                } else {
                    toast("Sign up failed: ${task.exception?.message}")
                }
            }
    }

    private fun emailSignIn() {
        val (email, pass) = readInputs() ?: return
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) goToNotes()
                else toast("Sign in failed: ${task.exception?.message}")
            }
    }

    private fun resetPassword() {
        val email = binding.etEmail.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email first")
            return
        }
        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            toast(if (task.isSuccessful) "Reset email sent" else "Failed: ${task.exception?.message}")
        }
    }

    private fun readInputs(): Pair<String, String>? {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Enter a valid email"); return null
        }
        if (pass.length < 6) {
            toast("Password must be 6+ characters"); return null
        }
        return email to pass
    }

    // ---------- Google Sign-In (Lesson 04) ----------

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    toast("Welcome, ${auth.currentUser?.displayName}!")
                    goToNotes()
                } else {
                    toast("Firebase auth failed: ${task.exception?.message}")
                }
            }
    }

    // ---------- Navigation & helpers ----------

    private fun goToNotes() {
        startActivity(Intent(this, NotesActivity::class.java))
        finish()  // don't let BACK return to login
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
