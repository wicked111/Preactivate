package com.preactivated.preactivate.insider.profile

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.preactivated.preactivate.R
import com.preactivated.preactivate.starting.LoginActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Objects
import java.util.prefs.Preferences

class BottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            Objects.requireNonNull(userId)?.let { Log.d("Something wennt wrong...", it) }
        }

        fetchUserData(view)

        val signOutButton = view.findViewById<LinearLayout>(R.id.signout)
        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
        }

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        userId?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val profilePicUrl = document.getString("profile")
                    val profileImageView = view.findViewById<CircleImageView>(R.id.profileoftheusercurrent)
                    Glide.with(this).load(profilePicUrl).into(profileImageView)
                }
                .addOnFailureListener { exception ->
                    Log.w(ContentValues.TAG, "Error getting user profile", exception)
                }
        }

        return view
    }

    @SuppressLint("RestrictedApi")
    fun fetchUserData(view: View) {
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
                                val userEmail = dataMap["email"] as String?
                                // Find and update the TextViews
                                val usernameTextView = view.findViewById<TextView>(R.id.usernameplaceholder)
                                val emailTextView = view.findViewById<TextView>(R.id.emaplaceholder)
                                usernameTextView.text = userName ?: "Username not found"
                                emailTextView.text = userEmail ?: "Email not found"
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
