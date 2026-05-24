package com.example.eventpass2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()

        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                findViewById<TextView>(R.id.tvWelcome).text = "Welcome, ${user?.name}!"
            }

        findViewById<Button>(R.id.btnMyQR).setOnClickListener {
            startActivity(Intent(this, MyQRActivity::class.java))
        }

        findViewById<Button>(R.id.btnCreateEvent).setOnClickListener {
            startActivity(Intent(this, CreateEventActivity::class.java))
        }

        findViewById<Button>(R.id.btnJoinEvent).setOnClickListener {
            startActivity(Intent(this, JoinEventActivity::class.java))
        }

        findViewById<Button>(R.id.btnMyEvents).setOnClickListener {
            startActivity(Intent(this, MyEventsActivity::class.java))
        }

        findViewById<Button>(R.id.btnAttendanceHistory).setOnClickListener {
            startActivity(Intent(this, AttendanceHistoryActivity::class.java))
        }

        findViewById<Button>(R.id.btnProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}