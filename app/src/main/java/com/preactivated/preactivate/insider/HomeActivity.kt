package com.preactivated.preactivate.insider

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.profile.BottomSheetDialogFragment
import com.preactivated.preactivate.insider.profile.fragments.AndroidFragment
import com.preactivated.preactivate.insider.profile.fragments.ExploreFragment
import com.preactivated.preactivate.insider.profile.fragments.FlutterFragment
import com.preactivated.preactivate.insider.profile.fragments.PythonFragment
import com.preactivated.preactivate.insider.profile.fragments.ReactFragment
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Objects
import java.util.prefs.Preferences

class HomeActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileImageView: CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        profileImageView = findViewById(R.id.profileoftheuser)

        val filter = IntentFilter("PROFILE_UPDATED")
        registerReceiver(profileUpdateReceiver, filter)

        setupUI()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.email
            Objects.requireNonNull(userId)?.let { Log.d("Something wennt wrong...", it) }
        }

        fetchUserData()

        Objects.requireNonNull(
            currentUser!!.email
        )?.let {
            Log.d(
                "Something went wrong..", it
            )
        }

        val preferences = Preferences.userRoot()
        if (preferences["name", null] != null) {
            val name = preferences["name", null]
            Log.d("Something went wrong in the name", name)
        }

        val profileImageView = findViewById<CircleImageView>(R.id.profileoftheuser)
        profileImageView.setOnClickListener {
            val bottomSheetDialogFragment = BottomSheetDialogFragment()
            bottomSheetDialogFragment.show(supportFragmentManager, "BottomSheetDialogFragment")
        }


    }

    private val profileUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "PROFILE_UPDATED") {
                val profilePicUrl = intent.getStringExtra("profilePicUrl")
                Glide.with(this@HomeActivity).load(profilePicUrl).into(profileImageView)
                Toast.makeText(this@HomeActivity, "Profile updated", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupUI() {
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

        val searchbtn = findViewById<Button>(R.id.searchInputbtn)
        searchbtn.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        viewPager = findViewById(R.id.view_pager_preactivate)
        viewPager.adapter = ViewPagerAdapter(this)

        val tabLayout = findViewById<com.google.android.material.tabs.TabLayout>(R.id.tabLayoutpreactivate)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Explore"
                1 -> tab.text = "Android"
                2 -> tab.text = "Flutter"
                3 -> tab.text = "React"
                4 -> tab.text = "Python"
            }
        }.attach()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        userId?.let {
            db.collection("users").document(it)
                .get()
                .addOnSuccessListener { document ->
                    val profilePicUrl = document.getString("profile")
                    val profileImageView = findViewById<CircleImageView>(R.id.profileoftheuser)
                    Glide.with(this).load(profilePicUrl).into(profileImageView)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting user profile", exception)
                }
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
                            val dataMap =
                                document.getData()
                            val field1 = dataMap["email"] as String?
                            val a =
                                field1?.compareTo(currentUser.email!!) ?: -1
                            if (a == 0) {
                                val userName = dataMap["name"] as String?
                                val userEmail = dataMap["email"] as String?
                                val preferences =
                                    Preferences.userRoot()
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

    private inner class ViewPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {

        override fun getItemCount(): Int {
            return 5 // Number of tabs
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ExploreFragment()
                1 -> AndroidFragment()
                2 -> FlutterFragment()
                3 -> ReactFragment()
                4 -> PythonFragment()
                else -> throw IllegalStateException("Unexpected position: $position")
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(profileUpdateReceiver)
    }

}
