package com.example.eventpass2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceHistoryAdapter(
    private val records: List<AttendanceRecord>
) : RecyclerView.Adapter<AttendanceHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEventName: TextView = view.findViewById(R.id.tvHistoryEventName)
        val tvEventDate: TextView = view.findViewById(R.id.tvHistoryEventDate)
        val tvEventLocation: TextView = view.findViewById(R.id.tvHistoryEventLocation)
        val tvStatus: TextView = view.findViewById(R.id.tvHistoryStatus)
        val tvCheckinTime: TextView = view.findViewById(R.id.tvCheckinTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = records.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.tvEventName.text = record.eventName
        holder.tvEventDate.text = record.eventDate
        holder.tvEventLocation.text = record.eventLocation

        when {
            record.checkedIn -> {
                holder.tvStatus.text = "✅ Attended"
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_green_dark)
                )
                val sdf = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
                holder.tvCheckinTime.text = "Checked in: ${sdf.format(Date(record.timestamp))}"
                holder.tvCheckinTime.visibility = View.VISIBLE
            }
            record.isEnded -> {
                holder.tvStatus.text = "❌ Absent"
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(android.R.color.holo_red_dark)
                )
                holder.tvCheckinTime.visibility = View.GONE
            }
            else -> {
                holder.tvStatus.text = "⏳ Upcoming"
                holder.tvStatus.setTextColor(
                    holder.itemView.context.getColor(android.R.color.darker_gray)
                )
                holder.tvCheckinTime.visibility = View.GONE
            }
        }
    }
}