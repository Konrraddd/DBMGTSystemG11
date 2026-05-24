package com.example.eventpass2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class JoinEventActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join_event)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Join Event"

        val etCode = findViewById<EditText>(R.id.etEventCode)
        val btnJoin = findViewById<Button>(R.id.btnJoin)

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { userSnapshot ->
                val currentUser = userSnapshot.getValue(User::class.java) ?: return@addOnSuccessListener

                btnJoin.setOnClickListener {
                    val code = etCode.text.toString().trim().uppercase()
                    if (code.isEmpty()) {
                        Toast.makeText(this, "Enter an event code", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    btnJoin.isEnabled = false
                    btnJoin.text = "Joining..."

                    FirebaseDatabase.getInstance()
                        .getReference("events")
                        .orderByChild("eventCode")
                        .equalTo(code)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    Toast.makeText(this@JoinEventActivity, "Event not found. Check the code and try again.", Toast.LENGTH_LONG).show()
                                    btnJoin.isEnabled = true
                                    btnJoin.text = "Join"
                                    return
                                }

                                val eventSnapshot = snapshot.children.first()
                                val event = eventSnapshot.getValue(Event::class.java) ?: return

                                if (event.hostId == uid) {
                                    Toast.makeText(this@JoinEventActivity, "You are the host of this event!", Toast.LENGTH_SHORT).show()
                                    btnJoin.isEnabled = true
                                    btnJoin.text = "Join"
                                    return
                                }

                                val participantRef = eventSnapshot.ref.child("participants").child(uid)
                                participantRef.get().addOnSuccessListener { existing ->
                                    if (existing.exists()) {
                                        Toast.makeText(this@JoinEventActivity, "You already joined this event!", Toast.LENGTH_SHORT).show()
                                        btnJoin.isEnabled = true
                                        btnJoin.text = "Join"
                                        return@addOnSuccessListener
                                    }

                                    val participant = Participant(
                                        uid = uid,
                                        name = currentUser.name,
                                        qr_id = currentUser.qr_id,
                                        checked_in = false,
                                        timestamp = 0
                                    )

                                    participantRef.setValue(participant)
                                        .addOnSuccessListener {
                                            Toast.makeText(this@JoinEventActivity, "Successfully joined \"${event.name}\"!", Toast.LENGTH_LONG).show()
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this@JoinEventActivity, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                            btnJoin.isEnabled = true
                                            btnJoin.text = "Join"
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@JoinEventActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                btnJoin.isEnabled = true
                                btnJoin.text = "Join"
                            }
                        })
                }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}