package com.example.preactivate.starting

import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.preactivate.R

class LoginActivity : AppCompatActivity() {
    private val imageIds = arrayOf(R.drawable.java, R.drawable.react, R.drawable.python)
    private var currentImageIndex = 0
    private val handler = Handler()
    private val runnable = object : Runnable {
        override fun run() {
            val imageView = findViewById<ImageView>(R.id.imagecoming)
            try {
                // Try setting the image resource
                imageView.setImageResource(imageIds[currentImageIndex])
            } catch (e: Exception) {
                // If there's an error, log it and skip to the next drawable
                e.printStackTrace()
                currentImageIndex = (currentImageIndex + 1) % imageIds.size
            }
            currentImageIndex = (currentImageIndex + 1) % imageIds.size
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
