package com.example.eventpass2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EventAdapter(
    private val events: List<Event>,
    private val onClick: (Event) -> Unit
) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvEventName)
        val tvDate: TextView = view.findViewById(R.id.tvEventDate)
        val tvLocation: TextView = view.findViewById(R.id.tvEventLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = events.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val event = events[position]
        holder.tvName.text = event.name
        holder.tvDate.text = event.date
        holder.tvLocation.text = event.location
        holder.itemView.setOnClickListener { onClick(event) }
    }
}