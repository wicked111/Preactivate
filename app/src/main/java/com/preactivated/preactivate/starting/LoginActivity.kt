package com.preactivated.preactivate.starting

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.HomeActivity

class LoginActivity : AppCompatActivity() {
    private val imageIds = arrayOf(R.drawable.java, R.drawable.react, R.drawable.python)
    private var currentImageIndex = 0
    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            val imageView = findViewById<ImageView>(R.id.imagecoming)
            try {
                imageView.setImageResource(imageIds[currentImageIndex])
            } catch (e: Exception) {
                e.printStackTrace()
                currentImageIndex = (currentImageIndex + 1) % imageIds.size
            }
            currentImageIndex = (currentImageIndex + 1) % imageIds.size
            handler.postDelayed(this, 2000)
        }
    }

    private lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                task.result?.let { account ->
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { googleSignInTask ->
                            if (googleSignInTask.isSuccessful) {
                                val user: FirebaseUser? = auth.currentUser
                                user?.let {
                                    if (!it.isEmailVerified) {
                                        //sendVerificationEmail(it)
                                    }
                                    startActivity(Intent(this, HomeActivity::class.java))
                                    Toast.makeText(this, "Welcome", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Log.e(
                                    "LoginActivity",
                                    "Google sign-in failed",
                                    googleSignInTask.exception
                                )
                                Toast.makeText(
                                    this,
                                    "Failed to sign in with Google",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                }
            } else {
                Toast.makeText(this, "Failed to login", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        handler.post(runnable)

        auth = Firebase.auth

        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("223268467028-ccf0uu4njcij8teckimfbjl6j9e5bhmg.apps.googleusercontent.com")
            .requestEmail()
            .build()
            .also { gso ->
                googleSignInClient = GoogleSignIn.getClient(this, gso)
            }

        findViewById<LinearLayout>(R.id.googlebtn).setOnClickListener {
            googleSignInClient.signInIntent.also { signInClient ->
                launcher.launch(signInClient)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}