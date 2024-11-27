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
import com.google.android.material.bottomnavigation.BottomNavigationView
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
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // настройка CalendarRecyclerView
        val titlesContainer = view.findViewById<ViewGroup>(R.id.titlesContainer)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dict = mapOf(
                    "Mon" to "ПН",
                    "Tue" to "ВТ",
                    "Wed" to "СР",
                    "Thu" to "ЧТ",
                    "Fri" to "ПТ",
                    "Sat" to "СБ",
                    "Sun" to "ВС"
                )
                val dayOfWeek = daysOfWeek[index]
                val title = dict.getValue(dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                textView.text = title
            }

        // настройка HabitRecyclerView
        val habitsRecycler: RecyclerView = view.findViewById(R.id.habitRecycler)
        habitsAdapter = HabitAdapter({ habit ->
            val fragment = HabitDetailFragment.newInstance(habit)
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }, habitsList, selectedDate)
        habitsRecycler.layoutManager = LinearLayoutManager(context)
        habitsRecycler.adapter = habitsAdapter

        calendarView = view.findViewById(R.id.weekCalendarView)
        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            lateinit var day: WeekDay
            init {
                view.setOnClickListener {

                    Log.d(TAG, "Day clicked: ${day.date}")
                    if (day.position == WeekDayPosition.RangeDate) {
                        val currentSelection = selectedDate
                        if (currentSelection == day.date) {
                            selectedDate = null
                            calendarView.notifyDateChanged(currentSelection)
                        } else {
                            selectedDate = day.date
                            calendarView.notifyDateChanged(day.date)
                            if (currentSelection != null) {
                                calendarView.notifyDateChanged(currentSelection)
                            }
                        }
                    }
                    val fragment = parentFragmentManager.findFragmentById(R.id.container) as? HomeFragment
                    if (fragment != null) {
                        Log.d(TAG, "Updating habit for date: ${day.date}")
                        fragment.updateFragmentData(day.date)
                        selectedDate = day.date
                    } else {
                        Log.d(TAG, "HomeFragment is null")
                    }
                }
            }
        }


        calendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) {
                container.day = WeekDay(data.date, WeekDayPosition.RangeDate)
                container.textView.text = data.date.dayOfMonth.toString()

                container.day = data
                val day = data
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()
                if (day.position == WeekDayPosition.RangeDate) {
                    textView.visibility = View.VISIBLE
                    if (day.date == selectedDate) {
                        textView.setTextColor(Color.MAGENTA)
                        textView.setBackgroundResource(R.drawable.rounded)
                    } else {
                        textView.setTextColor(Color.BLACK)
                        textView.background = null
                    }
                } else {
                    textView.visibility = View.INVISIBLE
                }
            }
        }

        val currentDate = LocalDate.now()
        val currentMonth = YearMonth.now()
        val startDate = currentMonth.minusMonths(100).atStartOfMonth()
        val endDate = currentMonth.plusMonths(100).atEndOfMonth()
        val firstDayOfWeek = DayOfWeek.MONDAY
        calendarView.setup(startDate, endDate, firstDayOfWeek)
        calendarView.scrollToWeek(currentDate)

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
                existingFragment.updateDiaryData(selectedDate)
            } else {
                val fragment = DiaryFragment.newInstance(selectedDate)
                val transaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.container, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }
        val plusButton = view.findViewById<View>(R.id.plusButton)
        val habitDialog = HabitDialog(requireContext())
        plusButton.setOnClickListener {
            habitDialog.show()
        }
        updateHabitList(habitsList)
        val arrowIcon2 = view.findViewById<ImageView>(R.id.arrowIcon2)
        arrowIcon2.setOnClickListener {
            val articleFragment = ArticlesFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.container, articleFragment)
            transaction.addToBackStack(null)
            transaction.commit()

            updateMenuSelection(R.id.articles) // Замените на ID вашего фрагмента
        }
    }
    private fun updateMenuSelection(fragmentId: Int) {
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNavigationView.selectedItemId = fragmentId
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
                            it.id = document.id
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
                    for (document in snapshot.documents) {
                        val habit = document.toObject(Habit::class.java)
                        habit?.let {
                            it.id = document.id
                            originalHabitsList.add(it) // Добавляем в оригинальный список
                        }
                    }
                    updateHabitList(getHabitsForDay(selectedDate)) // Обновляем список привычек для текущей даты
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
        if (result > 9) {
            result = result.toString().map { it.toString().toInt() }.sum()
        }
        return result
    }

    private fun getRandomAffirmation(): String {
        return if (affirmationsList.isNotEmpty()) {
            affirmationsList.random().text
        } else {
            "Я лчуший"
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
        habitsAdapter.updateSelectedDate(selectedDate)
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