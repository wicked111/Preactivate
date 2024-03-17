package com.preactivated.preactivate.insider.profile.drawer.post.customspinner.inventory

import com.preactivated.preactivate.R

object Data {
    val techList: List<Technology>
        get() {
            val techList: MutableList<Technology> = ArrayList<Technology>()
            val Default = Technology()
            Default.name = "Select Technology"
            Default.image = R.drawable.select
            Default.isDefault = true
            techList.add(Default)
            val Android = Technology()
            Android.name = "Android"
            Android.image = R.drawable.android
            techList.add(Android)
            val Flutter = Technology()
            Flutter.name = "Flutter"
            Flutter.image = R.drawable.flutter
            techList.add(Flutter)
            val React = Technology()
            React.name = "React"
            React.image = R.drawable.reactlogo
            techList.add(React)
            val Python = Technology()
            Python.name = "Python"
            Python.image = R.drawable.python
            techList.add(Python)
            return techList
        }
}
