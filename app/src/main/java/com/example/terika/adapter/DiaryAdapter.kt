package com.example.terika.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.diary.DiaryEntry
import com.example.terika.R

class DiaryAdapter(val diaryEntries: MutableList<DiaryEntry>) : RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder>() {

    class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val moodTextView: TextView = itemView.findViewById(R.id.moodTextView)
        private val notesTextView: TextView = itemView.findViewById(R.id.notesTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)

        fun bind(entry: DiaryEntry) {
            moodTextView.text = entry.mood
            notesTextView.text = entry.notes
            dateTextView.text = entry.formattedDate
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_diary_entry, parent, false)
        return DiaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        holder.bind(diaryEntries[position])
    }

    override fun getItemCount(): Int = diaryEntries.size
}
