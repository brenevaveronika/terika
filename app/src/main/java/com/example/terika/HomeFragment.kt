package com.example.terika

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.adapter.HabitAdapter
import com.example.terika.habit_tracker.HabitGenerator


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val habitsRecycler: RecyclerView = view.findViewById(R.id.habitRecycler)
        val habitsAdapter = HabitAdapter() { habit ->
            // Handle the click event
            val fragment = HabitDetailFragment.newInstance(habit)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment) // Use your container ID
            transaction.addToBackStack(null) // Optional: add to back stack
            transaction.commit()
        }
        habitsRecycler.layoutManager = LinearLayoutManager(context)
        habitsRecycler.adapter = habitsAdapter
        habitsAdapter.data = HabitGenerator.generateHabit(5)
        super.onViewCreated(view, savedInstanceState)
        // что-то здесь для того чтобы листенер повесить?
    }



    companion object {
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

}