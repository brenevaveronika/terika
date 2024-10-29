package com.example.terika

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.terika.habit_tracker.Habit

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HabitDetailFragment : Fragment() {

    companion object {
        private const val ARG_HABIT = "habit"
        fun newInstance(habit: Habit): HabitDetailFragment {
            val fragment = HabitDetailFragment()
            val args = Bundle()
            args.putParcelable(ARG_HABIT, habit) // Assuming Habit implements Parcelable
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_habit_detail, container, false)
        val habit = arguments?.getParcelable<Habit>(ARG_HABIT)

        val backButton: Button = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Use habit data to populate your views here
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
}