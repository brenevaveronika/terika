package com.example.terika.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.affirmation.Affirmation
import com.example.terika.R

class AffirmationAdapter(private val affirmations: List<Affirmation>) :
    RecyclerView.Adapter<AffirmationAdapter.AffirmationViewHolder>() {

    class AffirmationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.affirmationTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AffirmationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_affirmation, parent, false)
        return AffirmationViewHolder(view)
    }

    override fun onBindViewHolder(holder: AffirmationViewHolder, position: Int) {
        val affirmation = affirmations[position]
        holder.textView.text = affirmation.text
    }

    override fun getItemCount() = affirmations.size
}
