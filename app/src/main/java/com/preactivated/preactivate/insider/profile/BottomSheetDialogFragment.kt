package com.preactivated.preactivate.insider.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.preactivated.preactivate.R
import com.preactivated.preactivate.starting.LoginActivity

class BottomSheetDialogFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false)

        val signOutButton = view.findViewById<LinearLayout>(R.id.signout)
        signOutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
            activity?.finish()
            Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}
