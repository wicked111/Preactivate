package com.preactivated.preactivate.insider.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.profile.drawer.ConnectLocalDeviceActivity
import com.preactivated.preactivate.insider.profile.drawer.ReleaseDependencyActivity
import com.preactivated.preactivate.starting.LoginActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.util.Objects

class BottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var mdialog: Dialog? = null
    private val REQUEST_IMAGE_PICK = 1
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

        val changePhotoButton = view.findViewById<ImageView>(R.id.changephoto)
        changePhotoButton.setOnClickListener {
            openGallery()
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
        //....Navigate to Activities ......

        val settingsButton = view.findViewById<LinearLayout>(R.id.settings)
        settingsButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        val connectlocalButton = view.findViewById<LinearLayout>(R.id.connectlocal)
        connectlocalButton.setOnClickListener {
            val intent = Intent(activity, ConnectLocalDeviceActivity::class.java)
            startActivity(intent)
        }

        val readdocumentationButton = view.findViewById<LinearLayout>(R.id.readdoc)
        readdocumentationButton.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        val releasedependency = view.findViewById<LinearLayout>(R.id.releasedependency)
        releasedependency.setOnClickListener {
            val intent = Intent(activity, ReleaseDependencyActivity::class.java)
            startActivity(intent)
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
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data
            // Handle the selected image URI
            updateProfilePicture(selectedImageUri)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun updateProfilePicture(imageUri: Uri?) {
        showCustomProgressDialog("Updating...")

        val inputStream = context?.contentResolver?.openInputStream(imageUri!!)
        val byteArrayOutputStream = ByteArrayOutputStream()
        inputStream?.copyTo(byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference
        val photoRef = storageRef.child("profile_pictures/${auth.currentUser?.uid}")
        val uploadTask = photoRef.putBytes(byteArray)
        uploadTask.addOnSuccessListener {
            photoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val userRef = db.collection("users").document(auth.currentUser?.uid!!)
                userRef.update("profile", downloadUri.toString())
                    .addOnSuccessListener {
                        // Notify HomeActivity to update the profile picture
                        val intent = Intent("PROFILE_UPDATED")
                        intent.putExtra("profilePicUrl", downloadUri.toString())
                        context?.sendBroadcast(intent)

                        val profileImageView = view?.findViewById<CircleImageView>(R.id.profileoftheusercurrent)
                        if (profileImageView != null) {
                            Glide.with(this).load(downloadUri).into(profileImageView)
                        }
                        mdialog?.dismiss()
                    }
                    .addOnFailureListener { e ->
                        mdialog?.dismiss()
                    }
            }
        }.addOnFailureListener { e ->
            mdialog?.dismiss()
        }
    }

    private fun showCustomProgressDialog(message: String) {
        if (mdialog != null && mdialog!!.isShowing()) {
            mdialog!!.dismiss()
        }
        val inflater = getLayoutInflater()
        @SuppressLint("InflateParams") val view: View =
            inflater.inflate(R.layout.custom_dialogue, null)
        val lottieAnimationView: LottieAnimationView = view.findViewById(R.id.lottieAnimationView)
        val progressText = view.findViewById<TextView>(R.id.progressText)
        progressText.text = message

        // Create a new Dialog instance with the correct context
        mdialog = Dialog(requireActivity())
        mdialog!!.setContentView(view)
        mdialog!!.setCancelable(false)
        mdialog!!.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mdialog!!.show()
    }

}
