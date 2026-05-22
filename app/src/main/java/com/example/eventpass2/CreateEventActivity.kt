package com.example.eventpass2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CreateEventActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Event"

        val etName = findViewById<EditText>(R.id.etEventName)
        val etDate = findViewById<EditText>(R.id.etEventDate)
        val etLocation = findViewById<EditText>(R.id.etEventLocation)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)

                findViewById<Button>(R.id.btnCreateEvent).setOnClickListener {
                    val name = etName.text.toString().trim()
                    val date = etDate.text.toString().trim()
                    val location = etLocation.text.toString().trim()

                    if (name.isEmpty() || date.isEmpty() || location.isEmpty()) {
                        Toast.makeText(this, "Fill in all fields", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val ref = FirebaseDatabase.getInstance().getReference("events")
                    val eventId = ref.push().key ?: return@setOnClickListener

                    val event = Event(
                        eventId = eventId,
                        name = name,
                        date = date,
                        location = location,
                        hostId = uid,
                        hostName = user?.name ?: ""
                    )

                    ref.child(eventId).setValue(event)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Event created!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}