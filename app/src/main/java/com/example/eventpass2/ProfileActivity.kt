package com.example.eventpass2

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Profile"

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java) ?: return@addOnSuccessListener
                findViewById<TextView>(R.id.tvProfileName).text = user.name
                findViewById<TextView>(R.id.tvProfileEmail).text = user.email
                findViewById<TextView>(R.id.tvProfileQrId).text = "QR ID: ${user.qr_id}"
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}