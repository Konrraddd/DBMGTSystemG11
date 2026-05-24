package com.example.eventpass2

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AttendanceHistoryActivity : AppCompatActivity() {

    private val historyList = mutableListOf<AttendanceRecord>()
    private lateinit var adapter: AttendanceHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendance_history)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Attendance History"

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val recycler = findViewById<RecyclerView>(R.id.recyclerHistory)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = AttendanceHistoryAdapter(historyList)
        recycler.adapter = adapter

        val tvEmpty = findViewById<TextView>(R.id.tvEmptyHistory)

        FirebaseDatabase.getInstance()
            .getReference("events")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    historyList.clear()
                    for (eventSnap in snapshot.children) {
                        val event = eventSnap.getValue(Event::class.java) ?: continue
                        // Only show events user is a participant in (not host)
                        val participantSnap = eventSnap.child("participants").child(uid)
                        if (participantSnap.exists() && event.hostId != uid) {
                            val checkedIn = participantSnap.child("checked_in")
                                .getValue(Boolean::class.java) ?: false
                            val timestamp = participantSnap.child("timestamp")
                                .getValue(Long::class.java) ?: 0L
                            historyList.add(
                                AttendanceRecord(
                                    eventName = event.name,
                                    eventDate = event.date,
                                    eventLocation = event.location,
                                    isEnded = event.isEnded,
                                    checkedIn = checkedIn,
                                    timestamp = timestamp
                                )
                            )
                        }
                    }
                    adapter.notifyDataSetChanged()
                    tvEmpty.visibility = if (historyList.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}