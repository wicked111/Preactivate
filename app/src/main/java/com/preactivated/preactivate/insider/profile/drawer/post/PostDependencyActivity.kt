package com.preactivated.preactivate.insider.profile.drawer.post

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.HomeActivity
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Objects
import java.util.UUID

class PostDependencyActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var mdialog: Dialog? = null
    var dependencytitle: EditText? = null
    var shortdescription: EditText? = null
    var longdescription: EditText? = null
    var dc = FirebaseFirestore.getInstance()
    private var StorageReference: StorageReference? = null
    private var Databasereference: DatabaseReference? = null
    private val REQUEST_CODE_PICK_IMAGES = 1001
    private val REQUEST_CODE_PICK_VIDEO = 1002

    private var selectedImageUris: MutableList<Uri> = mutableListOf()
    private var selectedVideoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_dependency)

        val techName = intent.getStringExtra("techName")
        val techImageResId = intent.getIntExtra("techImage", R.drawable.ic_thumb)
        //......................................

        dependencytitle = findViewById<EditText>(R.id.dependencytitle)
        shortdescription = findViewById<EditText>(R.id.shortdescription)
        longdescription = findViewById<EditText>(R.id.longdescription)

        Databasereference = FirebaseDatabase.getInstance().getReference()
        StorageReference = FirebaseStorage.getInstance().getReference()

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

        //..............Add Images.....

        val addImage = findViewById<ImageView>(R.id.addimage)
        addImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGES)
        }

        //................... Video Attach.................

        val selectVideoButton = findViewById<TextView>(R.id.selectVideoButton)
        selectVideoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO)
        }

        //....... Post .................
        val deployButton = findViewById<TextView>(R.id.deploy)

        deployButton.setOnClickListener {
            post()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_PICK_IMAGES -> {
                    // Handle image selection
                    val selectedImages = mutableListOf<Uri>()
                    val clipData = data.clipData
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            selectedImages.add(uri)
                        }
                    } else if (data.data != null) {
                        selectedImages.add(data.data!!)
                    }
                    if (selectedImages.size <= 3) {
                        displaySelectedImages(selectedImages)
                    } else {
                        Toast.makeText(
                            this,
                            "You can select up to 3 images only",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                REQUEST_CODE_PICK_VIDEO -> {
                    val selectedVideoUri = data.data
                    val videoView = findViewById<VideoView>(R.id.videoPreview)
                    videoView.setVideoURI(selectedVideoUri)
                    videoView.setOnPreparedListener { mp ->
                        mp.setOnVideoSizeChangedListener { mp, width, height ->
                            val videoDuration = mp.duration
                            if (videoDuration > 30000) { // If video is longer than 30 seconds
                                mp.seekTo(30000) // Seek to 30 seconds
                            }
                        }
                    }
                    videoView.start()
                    videoView.visibility = View.VISIBLE

                    // Extract video name
                    val videoName = getVideoNameFromUri(this, selectedVideoUri)
                    // Display video name
                    findViewById<EditText>(R.id.videoprompt).setText(videoName)
                    // Show Toast
                    Toast.makeText(this, "Video selected: $videoName", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun displaySelectedImages(selectedImages: List<Uri>) {
        val recyclerView = findViewById<RecyclerView>(R.id.promptimage)
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val imageUris = selectedImages.toMutableList()
        recyclerView.adapter = ImageAdapter(imageUris) { uri ->
            imageUris.remove(uri)
            recyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun getVideoNameFromUri(context: Context, uri: Uri?): String {
        var result: String? = null
        if (uri != null) {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                result = cursor.getString(nameIndex)
                cursor.close()
            }
        }
        return result ?: "Unknown Video"
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

    class ImageAdapter(
        private val imageUris: MutableList<Uri>,
        private val onDelete: (Uri) -> Unit
    ) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val uri = imageUris[position]
            Glide.with(holder.itemView.context).load(uri).into(holder.imageView)
            holder.deleteIcon.setOnClickListener {
                onDelete(uri)
            }
        }

        override fun getItemCount(): Int = imageUris.size

        class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
            val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
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
            Toast.makeText(this, "Title and Short Description are required.", Toast.LENGTH_SHORT).show()
            return
        }

        uploadContent()
    }

    private fun uploadContent() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Handle image uploads
                val imageUrls = selectedImageUris.mapNotNull { uri ->
                    async { uploadFile(uri) }.await()
                }.filterNotNull()

                // Handle video upload if a video is selected
                val videoUrl = selectedVideoUri?.let { uri ->
                    async { uploadFile(uri) }.await()
                }

                // Ensure Firestore is updated on the main thread after all uploads are complete
                withContext(Dispatchers.Main) {
                    if (imageUrls.isNotEmpty() || videoUrl != null) {
                        saveUrlsInFirestore(imageUrls, videoUrl)
                    } else {
                        Toast.makeText(this@PostDependencyActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                // Log the error and show a toast message
                Log.e("UploadError", "Error uploading files: ${e.localizedMessage}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PostDependencyActivity, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private suspend fun uploadFile(uri: Uri): String? {
        val storageReference = FirebaseStorage.getInstance().reference.child("uploads/${UUID.randomUUID()}")
        return try {
            val uploadTaskSnapshot = storageReference.putFile(uri).await()
            val downloadUri = storageReference.downloadUrl.await()
            downloadUri.toString()
        } catch (e: Exception) {
            Log.e("UploadError", "Error uploading file: ${e.localizedMessage}", e)
            null // Consider logging more details here or informing the user
        }
    }


    private fun saveUrlsInFirestore(imageUrls: List<String>, videoUrl: String?) {
        val userMap = hashMapOf<String, Any>(
            "dependencytitle" to dependencytitle?.text.toString().trim(),
            "shortdescription" to shortdescription?.text.toString().trim(),
            "longdescription" to longdescription?.text.toString().trim(),
            "Images" to imageUrls,
            "Video" to (videoUrl ?: ""),
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
    private fun getSelectedImageUris(): List<Uri> {
        val imageUriStrings = listOf("ImageUriString1", "ImageUriString2", "ImageUriString3")
        return imageUriStrings.map { Uri.parse(it) }
    }

    private fun getSelectedVideoUri(): Uri? {
        val videoUriString = "VideoUriString"
        return if (videoUriString.isNotEmpty()) Uri.parse(videoUriString) else null
    }
}