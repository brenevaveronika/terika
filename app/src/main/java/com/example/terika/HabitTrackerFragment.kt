package com.example.terika

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.adapter.DiaryAdapter
import com.example.terika.adapter.HabitAdapter
import com.example.terika.diary.DiaryEntry
import com.example.terika.habit_tracker.Habit
import com.example.terika.habit_tracker.SwipeToDeleteCallback
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HabitTrackerFragment : Fragment() {

    private val originalHabitsList = mutableListOf<Habit>()
    val habitsList = mutableListOf<Habit>()
    private lateinit var habitsAdapter: HabitAdapter
    var selectedDate = LocalDate.now()
    private lateinit var recyclerView: RecyclerView
    private lateinit var diaryAdapter: DiaryAdapter
    private val diaryEntries = mutableListOf<DiaryEntry>()
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_habit_tracker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plusButton = view.findViewById<View>(R.id.plusButton)
        val habitDialog = HabitDialog(requireContext())
        plusButton.setOnClickListener {
            habitDialog.show()
        }

        val habitsRecycler: RecyclerView = view.findViewById(R.id.habitRecycler)
        habitsAdapter = HabitAdapter({ habit ->
            val fragment = HabitDetailFragment.newInstance(habit)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment) // Use your container ID
            transaction.addToBackStack(null) // Optional: add to back stack
            transaction.commit()
        }, habitsList)
        habitsRecycler.layoutManager = LinearLayoutManager(context)
        habitsRecycler.adapter = habitsAdapter

        fetchHabits()
        originalHabitsList.filter { it.subheading.contains(selectedDate.dayOfWeek.toString()) }

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        diaryAdapter = DiaryAdapter(diaryEntries)
        recyclerView.adapter = diaryAdapter

        db = FirebaseFirestore.getInstance()
        loadDiaryEntries()

        setupMoodButtons(view)
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(diaryAdapter, db))
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun updateHabitList(habits: List<Habit>) {
        habitsList.clear() // Очищаем текущий список
        habitsList.addAll(habits) // Добавляем отфильтрованные привычки
        habitsAdapter.notifyDataSetChanged() // Обновляем адаптер
    }

    private fun loadDiaryEntries() {
        db.collection("diary")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val entry = document.toObject(DiaryEntry::class.java)
                    entry.id = document.id
                    diaryEntries.add(entry)
                }
                diaryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }


    private fun addDiaryEntry(mood: String, notes: String) {
        val entry = db.collection("diary").document().id
        val diary = DiaryEntry(mood, notes, entry)
        db.collection("diary")
            .add(diary)
            .addOnSuccessListener {
                diaryEntries.add(diary)
                diaryAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding diary entry", e)
            }
    }

    private fun setupMoodButtons(view: View) {
        val happyButton: Button = view.findViewById(R.id.happyButton)
        val calmButton: Button = view.findViewById(R.id.calmButton)
        val sadButton: Button = view.findViewById(R.id.sadButton)
        val notesEditText: EditText = view.findViewById(R.id.notesEditText)

        happyButton.setOnClickListener {
            addDiaryEntry("Отлично", notesEditText.text.toString())
            notesEditText.text.clear() // Clear the input after adding
        }

        calmButton.setOnClickListener {
            addDiaryEntry("Нормально", notesEditText.text.toString())
            notesEditText.text.clear() // Clear the input after adding
        }

        sadButton.setOnClickListener {
            addDiaryEntry("Плохо", notesEditText.text.toString())
            notesEditText.text.clear() // Clear the input after adding
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HabitTrackerFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HabitTrackerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun fetchHabits() {
        val db = FirebaseFirestore.getInstance()
        db.collection("habits")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    originalHabitsList.clear() // Очищаем оригинальный список
                    habitsList.clear() // Очищаем текущий список для обновления
                    for (document in snapshot.documents) {
                        val habit = document.toObject(Habit::class.java)
                        habit?.let {
                            it.id = document.id // Получаем ID документ
                            originalHabitsList.add(it) // Добавляем в оригинальный список
                            habitsList.add(it) // Добавляем в текущий список
                        }
                    }
                    habitsAdapter.notifyDataSetChanged() // Обновляем адаптер
                } else {
                    Log.d(TAG, "Current data: null")
                }
            }
    }
}