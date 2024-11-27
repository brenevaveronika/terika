package com.example.terika

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.terika.habit_tracker.Habit
import com.google.firebase.firestore.FirebaseFirestore

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HabitDetailFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    companion object {
        private const val ARG_HABIT = "habit"
        fun newInstance(habit: Habit): HabitDetailFragment {
            val fragment = HabitDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_HABIT, habit)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_habit_detail, container, false)
        val habit = arguments?.getParcelable<Habit>(ARG_HABIT)

        db = FirebaseFirestore.getInstance()


        val deleteButton: Button = view.findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            if (habit != null) {
                deleteHabit(habit.id) // Передаем ID привычки для удаления
            }
        }

        val backButton: Button = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        val editButton: Button = view.findViewById(R.id.editButton)
        editButton.setOnClickListener {
            if (habit != null) {
                openEditHabitDialog(habit)
            }
            requireActivity().onBackPressed()
        }


        val headingTextView: TextView = view.findViewById(R.id.headingTextView)
        val subheadingTextView: TextView = view.findViewById(R.id.subheadingTextView)
        val habitImageView: ImageView = view.findViewById(R.id.habitImageView)

        if (habit != null) {
            headingTextView.text = habit.heading
        }
        if (habit != null) {
            subheadingTextView.text = habit.subheading
        }
        if (habit != null) {
            habitImageView.setImageResource(habit.imageResId)
        }
        return view
    }

    private fun openEditHabitDialog(habit: Habit) {
        val dialog = HabitDialog(requireContext())
        dialog.setHabitData(habit) // Установите данные привычки в диалог
        dialog.show()
    }

    private fun deleteHabit(habitId: String) {
        db.collection("habits")
            .document(habitId)
            .delete()
            .addOnSuccessListener {
                // успех
                Toast.makeText(requireContext(), "Привычка удалена", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            }
            .addOnFailureListener { e ->
                // ошибки
                Toast.makeText(requireContext(), "Ошибка при удалении: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

