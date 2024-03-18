package com.preactivated.preactivate.insider.profile.drawer.post.posting

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.profile.drawer.ReleaseDependencyActivity
import com.preactivated.preactivate.insider.profile.drawer.post.customspinner.adapter.TechnologyAdapter
import com.preactivated.preactivate.insider.profile.drawer.post.customspinner.inventory.Data
import com.preactivated.preactivate.insider.profile.drawer.post.customspinner.inventory.Technology


class DependencyCategorySelectorActivity : AppCompatActivity() {

    private lateinit var spinnerTech: Spinner
    private var adapter: TechnologyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dependency_category_selector)

        // UI setup
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

        // Back button setup..................................................

        val backArrow = findViewById<ImageView>(R.id.backbtn)
        backArrow.setOnClickListener {
            val i = Intent(this, ReleaseDependencyActivity::class.java)
            startActivity(i)
            finish()
        }

        //.................................................
        val proceedbtnButton = findViewById<TextView>(R.id.proceedbtn)

        proceedbtnButton.setOnClickListener {
            val selectedItem = adapter?.getItem(spinnerTech.selectedItemPosition) as? Technology
            Log.d("DEBUG", "Selected item: ${selectedItem?.name}, isDefault: ${selectedItem?.isDefault}")
            if (selectedItem?.isDefault == true) {
                Toast.makeText(this, "Select a Tech Stack..", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, PostDependencyActivity::class.java)
                // Pass the selected technology's name and image resource ID as extras
                intent.putExtra("techName", selectedItem?.name)
                intent.putExtra("techImage", selectedItem?.image)
                startActivity(intent)
            }
        }

        //.......................................................

        spinnerTech = findViewById(R.id.spinner_technology)
        spinnerTech.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                adapter?.setSelectedItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Handle the case where no item is selected
            }
        }

        adapter = TechnologyAdapter(this, Data.techList)
        spinnerTech.adapter = adapter
        spinnerTech.setSelection(0)
    }

    fun onPopupWindowOpened(spinner: Spinner) {
         spinner.background = ContextCompat.getDrawable(this, R.drawable.bg_spinner_technology_up)
     }

     fun onPopupWindowClosed(spinner: Spinner) {
         spinner.background = ContextCompat.getDrawable(this, R.drawable.bg_spinner_technology)
     }
}
