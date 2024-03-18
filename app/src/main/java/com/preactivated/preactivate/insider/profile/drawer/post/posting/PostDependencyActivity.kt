package com.preactivated.preactivate.insider.profile.drawer.post.posting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.HomeActivity
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Objects

class PostDependencyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var mdialog: Dialog? = null
    var dependencytitle: EditText? = null
    var shortdescription: EditText? = null
    var longdescription: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_dependency)

        dependencytitle = findViewById<EditText>(R.id.dependencytitle)
        shortdescription = findViewById<EditText>(R.id.shortdescription)
        longdescription = findViewById<EditText>(R.id.longdescription)

        val techName = intent.getStringExtra("techName")
        val techImageResId = intent.getIntExtra("techImage", R.drawable.ic_thumb)

        //......UI ............................

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = window.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.background)
            window.navigationBarColor = ContextCompat.getColor(this, R.color.background)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val decorView = window.decorView
            decorView.systemUiVisibility =
                decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }


        //.............Back Button...............

        val backArrow = findViewById<ImageView>(R.id.backbtn)
        backArrow.setOnClickListener {
            val i: Intent =
                Intent(this@PostDependencyActivity, DependencyCategorySelectorActivity::class.java)
            startActivity(i)
            finish()
        }

        //............... Data Upload ....................


        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            Objects.requireNonNull(userId)?.let { Log.d("Something wennt wrong...", it) }
        }

        val techNameTextView = findViewById<TextView>(R.id.techname)
        val techImageView = findViewById<ImageView>(R.id.techimage)

        techNameTextView.text = techName ?: "No technology selected"
        techImageView.setImageResource(techImageResId)

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

        val deployButton = findViewById<TextView>(R.id.deploy)

        deployButton.setOnClickListener {
            post()
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

        mdialog = Dialog(this)
        mdialog!!.setContentView(view)
        mdialog!!.setCancelable(false)
        mdialog!!.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        mdialog!!.show()
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
                                    findViewById<TextView>(R.id.currentpersonpostingnamefirst)
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

    private fun post() {
        val title = dependencytitle?.text.toString().trim()
        val shortDesc = shortdescription?.text.toString().trim()
        val longDesc = longdescription?.text.toString().trim()

        if (auth.currentUser == null) {
            Toast.makeText(this, "Please log in to post", Toast.LENGTH_SHORT).show()
            return
        }

        if (title.isEmpty() || shortDesc.isEmpty()) {
            Toast.makeText(this, "Title and Short Description are required.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        saveDetailsInFirestore()
    }

    private fun saveDetailsInFirestore() {
        val techNameTextView = findViewById<TextView>(R.id.techname)
        val techName = techNameTextView.text.toString().trim()

        val userMap = hashMapOf<String, Any>(
            "title" to dependencytitle?.text.toString().trim(),
            "short" to shortdescription?.text.toString().trim(),
            "long" to longdescription?.text.toString().trim(),
            "tech" to techName,
            "userId" to (auth.currentUser?.uid ?: "")
        )

        db.collection("Pending Dependency").add(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Posted Successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to post: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
