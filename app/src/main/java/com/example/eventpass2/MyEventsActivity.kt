package com.example.eventpass2

import android.content.Intent
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

class MyEventsActivity : AppCompatActivity() {

    private val eventList = mutableListOf<Event>()
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_events)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Events"

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val recycler = findViewById<RecyclerView>(R.id.recyclerEvents)
        recycler.layoutManager = LinearLayoutManager(this)

        val tvEmpty = findViewById<TextView>(R.id.tvEmptyEvents)

        adapter = EventAdapter(eventList) { event ->
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("eventId", event.eventId)
            intent.putExtra("eventName", event.name)
            intent.putExtra("eventDate", event.date)
            intent.putExtra("eventLocation", event.location)
            intent.putExtra("eventDescription", event.description)
            intent.putExtra("hostId", event.hostId)
            startActivity(intent)
        }
        recycler.adapter = adapter

        FirebaseDatabase.getInstance()
            .getReference("events")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    eventList.clear()
                    for (child in snapshot.children) {
                        val event = child.getValue(Event::class.java) ?: continue
                        val isParticipant = child.child("participants").child(uid).exists()
                        if (event.hostId == uid || isParticipant) {
                            eventList.add(event)
                        }
                    }
                    // Sort: Active first, Ended last
                    eventList.sortBy { it.isEnded }
                    adapter.notifyDataSetChanged()

                    tvEmpty.visibility = if (eventList.isEmpty()) View.VISIBLE else View.GONE
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}