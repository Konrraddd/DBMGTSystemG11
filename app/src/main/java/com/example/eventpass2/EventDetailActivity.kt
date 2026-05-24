package com.example.eventpass2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        val isHost = uid == hostId

        val btnScan = findViewById<Button>(R.id.btnScanAttendance)
        val btnEndEvent = findViewById<Button>(R.id.btnEndEvent)
        val btnExportCsv = findViewById<Button>(R.id.btnExportCsv)
        val tvEventStatus = findViewById<TextView>(R.id.tvEventStatus)

        if (!isHost) {
            btnScan.visibility = View.GONE
            btnEndEvent.visibility = View.GONE
            btnExportCsv.visibility = View.GONE
        }

        // Listen to event status in real time
        FirebaseDatabase.getInstance()
            .getReference("events")
            .child(eventId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isEnded = snapshot.child("isEnded").getValue(Boolean::class.java) ?: false
                    if (isEnded) {
                        tvEventStatus.text = "🔴 Event Ended"
                        tvEventStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                        btnScan.isEnabled = false
                        btnScan.alpha = 0.5f
                        btnEndEvent.isEnabled = false
                        btnEndEvent.alpha = 0.5f
                    } else {
                        tvEventStatus.text = "🟢 Event Active"
                        tvEventStatus.setTextColor(getColor(android.R.color.holo_green_dark))
                        btnScan.isEnabled = true
                        btnScan.alpha = 1.0f
                        btnEndEvent.isEnabled = true
                        btnEndEvent.alpha = 1.0f
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        btnScan.setOnClickListener {
            val options = ScanOptions()
            options.setPrompt("Scan participant QR")
            options.setBeepEnabled(true)
            options.setOrientationLocked(true)
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setCaptureActivity(CaptureActivity::class.java)
            scanLauncher.launch(options)
        }

        btnEndEvent.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("End Event")
                .setMessage("Are you sure you want to end this event? Participants will no longer be able to check in.")
                .setPositiveButton("End Event") { _, _ ->
                    FirebaseDatabase.getInstance()
                        .getReference("events")
                        .child(eventId)
                        .child("isEnded")
                        .setValue(true)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Event has ended.", Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnExportCsv.setOnClickListener {
            exportToCsv(eventName)
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

    private fun exportToCsv(eventName: String) {
        if (participantList.isEmpty()) {
            Toast.makeText(this, "No participants to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val fileName = "${eventName.replace(" ", "_")}_attendance.csv"
            val file = File(getExternalFilesDir(null), fileName)
            val writer = FileWriter(file)

            writer.append("Name,QR ID,Checked In,Check-in Time\n")

            val sdf = SimpleDateFormat("MMM dd yyyy hh:mm a", Locale.getDefault())

            for (p in participantList) {
                val checkinTime = if (p.checked_in && p.timestamp > 0)
                    sdf.format(Date(p.timestamp)) else "—"
                writer.append("${p.name},${p.qr_id},${if (p.checked_in) "Yes" else "No"},$checkinTime\n")
            }

            writer.flush()
            writer.close()

            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$eventName Attendance Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Export Attendance Report"))

        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}