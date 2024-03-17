package com.preactivated.preactivate.insider.profile.drawer.post.customspinner.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.preactivated.preactivate.R
import com.preactivated.preactivate.insider.profile.drawer.post.customspinner.inventory.Technology

class TechnologyAdapter(private val context: Context, private val techList: List<Technology>) :
    BaseAdapter() {

    private var selectedItemIndex: Int = 0

    override fun getCount(): Int {
        return techList.size
    }

    override fun getItem(i: Int): Any {
        return techList[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    fun setSelectedItem(index: Int) {
        selectedItemIndex = index
        notifyDataSetChanged()
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        val rootView: View = LayoutInflater.from(context)
            .inflate(R.layout.item_technology, viewGroup, false)
        val txtName = rootView.findViewById<TextView>(R.id.name)
        val image = rootView.findViewById<ImageView>(R.id.image)
        txtName.text = techList[i].name
        image.setImageResource(techList[i].image)

        // Highlight the selected item
        if (i == selectedItemIndex) {
            txtName.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            txtName.setTextColor(ContextCompat.getColor(context, R.color.smallheading))
        }

        return rootView
    }

}
