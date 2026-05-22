package com.example.eventpass2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ParticipantAdapter(
    private val participants: List<Participant>
) : RecyclerView.Adapter<ParticipantAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvParticipantName)
        val tvCheckedIn: TextView = view.findViewById(R.id.tvCheckedIn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_participant, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = participants.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val p = participants[position]
        holder.tvName.text = p.name
        holder.tvCheckedIn.text = if (p.checked_in) "✔ Checked In" else "Not yet checked in"
    }
}