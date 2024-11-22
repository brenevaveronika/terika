package com.example.terika.adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.R
import com.example.terika.habit_tracker.Habit

class HabitAdapter(private val onHabitClick: (Habit) -> Unit, private val habits: List<Habit>):
    RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {
//    var data: List<Habit> = emptyList()
//        set(newValue) {
//            field = newValue
//            notifyDataSetChanged()
//        }

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
            holder.habitCheckboxCircleFill.setImageResource(it.checkboxFillId)
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

}