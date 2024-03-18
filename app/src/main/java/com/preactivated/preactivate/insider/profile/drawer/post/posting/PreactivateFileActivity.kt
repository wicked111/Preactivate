package com.preactivated.preactivate.insider.profile.drawer.post.posting

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.preactivated.preactivate.R
import de.hdodenhof.circleimageview.CircleImageView
import jp.wasabeef.richeditor.RichEditor
import java.util.Objects

class PreactivateFileActivity : AppCompatActivity() {

    private lateinit var editor: RichEditor
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preactivate_file)

        val backArrow = findViewById<ImageView>(R.id.backbtn)
        backArrow.setOnClickListener {
            val i: Intent =
                Intent(this@PreactivateFileActivity, DependencyCategorySelectorActivity::class.java)
            startActivity(i)
            finish()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            Objects.requireNonNull(userId)?.let { Log.d("Something wennt wrong...", it) }
        }

        fetchUserData();

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        userId?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val profilePicUrl = document.getString("profile")
                    val profileImageView =
                        findViewById<CircleImageView>(R.id.profileoftheuserinsider)
                    Glide.with(this).load(profilePicUrl).into(profileImageView)
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting user profile", exception)
                }
        }

        editor = findViewById(R.id.editor)
        editor.setEditorHeight(200)
        editor.setPadding(10, 10, 10, 10)
        editor.setPlaceholder("Dependency Documentation here...")

        val backgroundColor = ContextCompat.getColor(this, R.color.background)
        val textColor = ContextCompat.getColor(this, R.color.tabcolor)

        editor.setEditorBackgroundColor(backgroundColor)
        editor.setEditorFontColor(textColor)

        findViewById<ImageView>(R.id.action_bold).setOnClickListener {
            editor.setBold()
        }

        findViewById<ImageView>(R.id.action_italic).setOnClickListener {
            editor.setItalic()
        }

        findViewById<ImageView>(R.id.action_image).setOnClickListener {
            // Implement your logic for selecting and inserting an image
        }

    }
    @SuppressLint("RestrictedApi")
    fun fetchUserData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            val db = FirebaseFirestore.getInstance()
            putUserData(userId)
            db.collection("users")
                .get()
                .addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            val dataMap = document.getData()
                            val field1 = dataMap["email"] as String?
                            val a = field1?.compareTo(currentUser.email!!) ?: -1
                            if (a == 0) {
                                val userName = dataMap["name"] as String?
                                val usernameTextView =
                                    findViewById<TextView>(R.id.titlecomehere)
                                usernameTextView.text = userName ?: "Username not found"
                            }
                        }
                    } else {
                        Log.d(
                            FragmentManager.TAG,
                            "Error getting documents: ",
                            task.exception
                        )
                    }
                }
        }
    }

    private fun putUserData(phoneNumber: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val query = db.collection("users").whereEqualTo("email", phoneNumber)
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        db.collection("users").document(document.id)
                            .update("realtimeId", currentUser.uid)
                            .addOnSuccessListener {
                                Log.d(
                                    "Firestore",
                                    "DocumentSnapshot updated successfully!"
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    "Firestore",
                                    "Error updating document",
                                    e
                                )
                            }
                    }
                } else {
                    Log.w("Firestore", "Error getting documents.", task.exception)
                }
            }
        }
    }


}

