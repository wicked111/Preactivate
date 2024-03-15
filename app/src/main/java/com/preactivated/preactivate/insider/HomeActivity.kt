package com.preactivated.preactivate.insider

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.preactivated.preactivate.R
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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

    }
}