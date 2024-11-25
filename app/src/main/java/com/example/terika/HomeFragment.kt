package com.example.terika

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.terika.adapter.HabitAdapter
import com.example.terika.affirmation.Affirmation
import com.example.terika.habit_tracker.Habit
// import com.example.terika.habit_tracker.HabitGenerator
import com.google.firebase.firestore.FirebaseFirestore
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekCalendarView
import com.kizitonwose.calendar.view.WeekDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var calendarView: WeekCalendarView
    private var param1: String? = null
    private var param2: String? = null
    private val originalHabitsList = mutableListOf<Habit>()
    val habitsList = mutableListOf<Habit>()
    val affirmationsList = mutableListOf<Affirmation>()
    private lateinit var habitsAdapter: HabitAdapter
    var selectedDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d(TAG, "onCreate called")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val plusButton = view.findViewById<View>(R.id.plusButton)
        val habitDialog = HabitDialog(requireContext())
        plusButton.setOnClickListener {
            habitDialog.show()
        }

        // настройка CalendarRecyclerView
        val titlesContainer = view.findViewById<ViewGroup>(R.id.titlesContainer)
        val daysOfWeek = daysOfWeek()
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }

        calendarView = view.findViewById(R.id.weekCalendarView)
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            lateinit var day: WeekDay

            init {
                view.setOnClickListener {
                    // Обновляем данные фрагмента при нажатии на день
                    Log.d(TAG, "Day clicked: ${day.date}") // Логируем нажатие
                    if (day.position == WeekDayPosition.RangeDate) {
                        // Keep a reference to any previous selection
                        // in case we overwrite it and need to reload it.
                        val currentSelection = selectedDate
                        if (currentSelection == day.date) {
                            // If the user clicks the same date, clear selection.
                            selectedDate = null
                            // Reload this date so the dayBinder is called
                            // and we can REMOVE the selection background.
                            calendarView.notifyDateChanged(currentSelection)
                        } else {
                            selectedDate = day.date
                            // Reload the newly selected date so the dayBinder is
                            // called and we can ADD the selection background.
                            calendarView.notifyDateChanged(day.date)
                            if (currentSelection != null) {
                                // We need to also reload the previously selected
                                // date so we can REMOVE the selection background.
                                calendarView.notifyDateChanged(currentSelection)
                            }
                        }
                    }
                    val fragment =
                        parentFragmentManager.findFragmentById(R.id.container) as? HomeFragment
                    if (fragment != null) {
                        fragment.updateFragmentData(day.date)
                        selectedDate = day.date
                    } else {
                        Log.d(TAG, "HomeFragment is null")
                    }
                    Log.d(TAG, "updateFragmentData called") // Логируем вызов метода
                }
            }
        }


        calendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            // Called only when a new container is needed.
            override fun create(view: View) = DayViewContainer(view)

            // Called every time we need to reuse a container.
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.day = WeekDay(data.date, WeekDayPosition.RangeDate)
                container.textView.text = data.date.dayOfMonth.toString()

                container.day = data
                val day = data
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.position == WeekDayPosition.RangeDate) {
                    // Show the month dates. Remember that views are reused!
                    textView.visibility = View.VISIBLE
                    if (day.date == selectedDate) {
                        // If this is the selected date, show a round background and change the text color.
                        textView.setTextColor(Color.MAGENTA)
                        textView.setBackgroundResource(R.drawable.rounded)
                    } else {
                        // If this is NOT the selected date, remove the background and reset the text color.
                        textView.setTextColor(Color.BLACK)
                        textView.background = null
                    }
                } else {
                    // Hide in and out dates
                    textView.visibility = View.INVISIBLE
                }
            }
        }

        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(100).atStartOfMonth() // Adjust as needed
        val endDate = currentMonth.plusMonths(100).atEndOfMonth() // Adjust as needed
        val firstDayOfWeek = firstDayOfWeekFromLocale() // Available from the library
        calendarView.setup(startDate, endDate, firstDayOfWeek)
        calendarView.scrollToWeek(currentDate)

        // настройка HabitRecyclerView

        val habitsRecycler: RecyclerView = view.findViewById(R.id.habitRecycler)
        habitsAdapter = HabitAdapter({ habit ->
            // Handle the click event
            val fragment = HabitDetailFragment.newInstance(habit)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment) // Use your container ID
            transaction.addToBackStack(null) // Optional: add to back stack
            transaction.commit()
        }, habitsList)
        habitsRecycler.layoutManager = LinearLayoutManager(context)
        habitsRecycler.adapter = habitsAdapter


        fetchHabits()
        fetchAffirmations()
        val fragment = parentFragmentManager.findFragmentById(R.id.container) as? HomeFragment
        if (fragment != null) {
            fragment.updateFragmentData(LocalDate.now())
        } else {
            Log.d(TAG, "HomeFragment is null")
        }

        val arrowIcon = view.findViewById<ImageView>(R.id.arrowIcon)
        arrowIcon.setOnClickListener {

            val existingFragment = parentFragmentManager.findFragmentById(R.id.container) as? DiaryFragment

            Log.d(TAG, "$existingFragment")
            if (existingFragment != null) {
                // Если фрагмент существует, обновите его данные
                existingFragment.updateDiaryData(selectedDate)
            } else {
                // Если фрагмент не существует, создайте новый
                val fragment = DiaryFragment.newInstance(selectedDate)
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.container, fragment) // Используйте ваш ID контейнера
                transaction.addToBackStack(null) // Добавляем в стек возврата
                transaction.commit()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
    }

    private fun fetchAffirmations() {
        val db = FirebaseFirestore.getInstance()
        db.collection("affirmations")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    affirmationsList.clear() // Очищаем список аффирмаций
                    for (document in snapshot.documents) {
                        val affirmation = document.toObject(Affirmation::class.java)
                        affirmation?.let {
                            it.id = document.id // Получаем ID документа
                            affirmationsList.add(it) // Добавляем в список аффирмаций
                        }
                    }
                } else {
                    Log.d(TAG, "Current data: null")
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


    fun updateFragmentData(selectedDate: LocalDate) {
        Log.d(TAG, "Selected date: $selectedDate")
        val dayNumber = calculateDayNumber(selectedDate)
        val affirmation = getRandomAffirmation()
        val habitsForDay = getHabitsForDay(selectedDate)

        // Обновляем UI
        updateDayNumber(dayNumber)
        updateAffirmation(affirmation)
        updateHabitList(habitsForDay)
    }

    private fun calculateDayNumber(date: LocalDate): Int {
        val day = date.dayOfMonth.toString().map { it.toString().toInt() }.sum()
        val month = date.monthValue.toString().map { it.toString().toInt() }.sum()
        val year = date.year.toString().map { it.toString().toInt() }.sum()
        var result = day + month + year
        if (result > 9) {
            result = result.toString().map { it.toString().toInt() }.sum()
        }
        return result
    }

    private fun getRandomAffirmation(): String {
        return if (affirmationsList.isNotEmpty()) {
            affirmationsList.random().text // Возвращаем случайную аффирмацию
        } else {
            "Нет доступных аффирмаций" // Возвращаем сообщение по умолчанию, если список пуст
        }
    }

    private fun getHabitsForDay(date: LocalDate): List<Habit> {
        val dayOfWeek = date.dayOfWeek.toString()
        val daysOfWeek = mapOf(
            "MONDAY" to "ПН",
            "TUESDAY" to "ВТ",
            "WEDNESDAY" to "СР",
            "THURSDAY" to "ЧТ",
            "FRIDAY" to "ПТ",
            "SATURDAY" to "СБ",
            "SUNDAY" to "ВС"
        )
        return originalHabitsList.filter { it.subheading.contains(daysOfWeek.getValue(dayOfWeek)) } // фильтруем оригинальный список
    }

    private fun updateDayNumber(dayNumber: Int) {
        val dayNumberTextView =
            view?.findViewById<TextView>(R.id.dayNumber)
        dayNumberTextView?.text = dayNumber.toString()
    }

    private fun updateAffirmation(affirmation: String) {
        val affirmationTextView =
            view?.findViewById<TextView>(R.id.affirmation)
        affirmationTextView?.text = affirmation
    }

    private fun updateHabitList(habits: List<Habit>) {
        habitsList.clear()
        habitsList.addAll(habits)
        habitsAdapter.notifyDataSetChanged()
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