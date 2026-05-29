package com.example.eventpass2

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.UUID

class CreateEventActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Create Event"

        val etName = findViewById<EditText>(R.id.etEventName)
        val etDate = findViewById<EditText>(R.id.etEventDate)
        val etLocation = findViewById<EditText>(R.id.etEventLocation)
        val etDescription = findViewById<EditText>(R.id.etEventDescription)

        // Date picker
        etDate.isFocusable = false
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                etDate.setText(String.format("%02d/%02d/%04d", month + 1, day, year))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

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
                    val description = etDescription.text.toString().trim()

                    if (name.isEmpty() || date.isEmpty() || location.isEmpty()) {
                        Toast.makeText(this, "Fill in all required fields", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    val ref = FirebaseDatabase.getInstance().getReference("events")
                    val eventId = ref.push().key ?: return@setOnClickListener
                    val eventCode = UUID.randomUUID().toString().substring(0, 6).uppercase()

                    val event = Event(
                        eventId = eventId,
                        name = name,
                        date = date,
                        location = location,
                        description = description,
                        hostId = uid,
                        hostName = user?.name ?: "",
                        eventCode = eventCode,
                        isEnded = false
                    )

                    ref.child(eventId).setValue(event)
                        .addOnSuccessListener {
                            AlertDialog.Builder(this)
                                .setTitle("Event Created!")
                                .setMessage("Your event code is:\n\n$eventCode\n\nShare this with participants so they can join.")
                                .setPositiveButton("OK") { _, _ -> finish() }
                                .setCancelable(false)
                                .show()
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