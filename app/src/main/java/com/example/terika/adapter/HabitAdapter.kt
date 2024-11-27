package com.example.terika.adapter
import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.R
import com.example.terika.habit_tracker.Habit
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class HabitAdapter(private val onHabitClick: (Habit) -> Unit, private val habits: List<Habit>, var selectedDate: LocalDate):
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(itemView)
    }

    override fun getItemCount(): Int = habits.size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        habits[position].let {

            holder.habitIcon.setImageResource(it.imageResId)
            holder.habitHeading.text = it.heading
            holder.habitSubheading.text = it.subheading
            val isCompleted = habits[position].completionStatus[selectedDate.toString()] ?: false
            if (isCompleted) {
                holder.habitCheckboxCircleFill.setImageResource(R.drawable.check)
            } else {
                holder.habitCheckboxCircleFill.setImageResource(R.drawable.habit_checkbox_circle_fill)
            }
            holder.habitCheckboxCircleFill.setOnClickListener {
                val newStatus = !isCompleted
                habits[position].completionStatus[selectedDate.toString()] = newStatus
                notifyItemChanged(position)
                updateHabitInFirestore(habits[position], selectedDate.toString(), newStatus)
            }
            holder.habitCheckboxCircleLine.setImageResource(it.checkboxLineId)
            holder.habitHeading.setOnClickListener {
                onHabitClick(habits[position])
            }
        }
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val habitIcon: ImageView = itemView.findViewById(R.id.habitIcon)
        val habitHeading: TextView = itemView.findViewById(R.id.habitHeading)
        val habitSubheading: TextView = itemView.findViewById(R.id.habitSubheading)
        val habitCheckboxCircleFill: ImageView = itemView.findViewById(R.id.habitCheckboxCircleFill)
        val habitCheckboxCircleLine: ImageView = itemView.findViewById(R.id.habitCheckboxCircleLine)
    }

    private fun updateHabitInFirestore(habit: Habit, date: String, newStatus: Boolean) {
        val db = FirebaseFirestore.getInstance()
        db.collection("habits").document(habit.id)
            .update("completionStatus.$date", newStatus)
            .addOnSuccessListener {
                Log.d(TAG, "Habit successfully updated for date: $date!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating habit", e)
            }
    }
    fun updateSelectedDate(newDate: LocalDate) {
        if (selectedDate != null) {
            this.selectedDate = newDate
        }
        notifyDataSetChanged() // Обновляем адаптер
    }

}