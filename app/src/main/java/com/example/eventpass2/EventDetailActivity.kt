package com.example.eventpass2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class EventDetailActivity : AppCompatActivity() {

    private val participantList = mutableListOf<Participant>()
    private lateinit var adapter: ParticipantAdapter
    private lateinit var eventId: String
    private lateinit var hostId: String

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            checkInParticipant(result.contents)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        eventId = intent.getStringExtra("eventId") ?: return
        hostId = intent.getStringExtra("hostId") ?: ""
        val eventName = intent.getStringExtra("eventName") ?: ""
        val eventDate = intent.getStringExtra("eventDate") ?: ""
        val eventLocation = intent.getStringExtra("eventLocation") ?: ""

        supportActionBar?.title = eventName

        findViewById<TextView>(R.id.tvEventTitle).text = eventName
        findViewById<TextView>(R.id.tvEventInfo).text = "$eventDate • $eventLocation"

        val recycler = findViewById<RecyclerView>(R.id.recyclerParticipants)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = ParticipantAdapter(participantList)
        recycler.adapter = adapter

        val uid = FirebaseAuth.getInstance().currentUser?.uid

        val btnScan = findViewById<Button>(R.id.btnScanAttendance)
        if (uid != hostId) {
            btnScan.visibility = android.view.View.GONE
        }

        btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt("Scan participant QR")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setCaptureActivity(CaptureActivity::class.java)
            scanLauncher.launch(options)
        }

        loadParticipants()
    }

    private fun loadParticipants() {
        FirebaseDatabase.getInstance()
            .getReference("events")
            .child(eventId)
            .child("participants")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    participantList.clear()
                    var checkedCount = 0
                    for (child in snapshot.children) {
                        val p = child.getValue(Participant::class.java)
                        if (p != null) {
                            participantList.add(p)
                            if (p.checked_in) checkedCount++
                        }
                    }
                    adapter.notifyDataSetChanged()
                    findViewById<TextView>(R.id.tvAttendanceCount).text =
                        "Attendance: $checkedCount / ${participantList.size}"
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkInParticipant(qrId: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("events")
            .child(eventId)
            .child("participants")

        ref.orderByChild("qr_id")
            .equalTo(qrId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    Toast.makeText(this, "Participant not registered for this event", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val childSnapshot = snapshot.children.first()
                val participant = childSnapshot.getValue(Participant::class.java)

                if (participant?.checked_in == true) {
                    Toast.makeText(this, "Already checked in!", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                childSnapshot.ref.child("checked_in").setValue(true)
                childSnapshot.ref.child("timestamp").setValue(System.currentTimeMillis())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Checked in: ${participant?.name}", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}