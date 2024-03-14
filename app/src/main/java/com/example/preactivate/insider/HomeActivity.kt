package com.example.preactivate.insider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.preactivate.R

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val searchbtn = findViewById<Button>(R.id.searchInputbtn)
        searchbtn.setOnClickListener{
            startActivity(Intent(this,SearchActivity::class.java))
        }
    }
}