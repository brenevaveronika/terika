package com.example.terika

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.example.terika.habit_tracker.Habit
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.* //

class HabitDialog(context: Context) : Dialog(context) {

    private var selectedDays = mutableListOf<String>()
    private var selectedDateType: String = ""
    private var calendarView: CalendarView
    private var habitId: String? = null

    init {
        setContentView(R.layout.dialog_add_habit)
        val etHeading = findViewById<EditText>(R.id.et_heading)
        val etStartDate = findViewById<EditText>(R.id.et_start_date)
        val etEndDate = findViewById<EditText>(R.id.et_end_date)

        etHeading.requestFocus()
        // Инициализация CalendarView
        calendarView = findViewById(R.id.calendar_view)
        calendarView.visibility = View.GONE

        val rootView = findViewById<View>(R.id.rootView) // Убедитесь, что этот ID существует в вашем XML
        rootView.setOnClickListener {
            // Скрытие клавиатуры при нажатии на корневое представление
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etHeading.windowToken, 0)
            etHeading.clearFocus()
        }

        etHeading.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // Скрытие клавиатуры, если поле теряет фокус
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etHeading.windowToken, 0)
            }
        }

        setupDayButtons()

        etStartDate.setOnClickListener {
            selectedDateType = "start"
            calendarView.visibility = View.VISIBLE
            Log.d("HabitDialog", "Start date EditText clicked")
        }

        etEndDate.setOnClickListener {
            selectedDateType = "end"
            calendarView.visibility = View.VISIBLE
            Log.d("HabitDialog", "End date EditText clicked")
        }

        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)

            if (selectedDateType == "start") {
                etStartDate.setText(formattedDate)
                Log.d("HabitDialog", "Start date set: $formattedDate")
            } else if (selectedDateType == "end") {
                val startDateText = etStartDate.text.toString()
                if (startDateText.isNotEmpty()) {
                    val startDate = dateFormat.parse(startDateText)
                    // Compare the dates
                    if (startDate != null && selectedDate.time <= startDate) {
                        etEndDate.setText("")
                        Log.d("HabitDialog", "End date cleared: $formattedDate (must be greater than start date)")
                        Toast.makeText(context, "Конечная дата должна быть позже начальной!", Toast.LENGTH_SHORT).show()
                        return@setOnDateChangeListener
                    }
                }
                etEndDate.setText(formattedDate)
                Log.d("HabitDialog", "End date set: $formattedDate")
            }

            calendarView.visibility = View.GONE
        }


        findViewById<Button>(R.id.btn_add_habit).setOnClickListener {
            val heading = etHeading.text.toString()
            val startDate = etStartDate.text.toString()
            val endDate = etEndDate.text.toString()

            if (heading.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                val completionStatus = mutableMapOf<String, Boolean>()
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val startDateParsed = dateFormat.parse(startDate) ?: Date()
                val endDateParsed = dateFormat.parse(endDate) ?: Date()

                val calendar = Calendar.getInstance()
                calendar.time = startDateParsed

                while (calendar.time <= endDateParsed) {
                    val dateKey = dateFormat.format(calendar.time)
                    completionStatus[dateKey] = false // Изначально все дни не выполнены
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

// Создание привычки с инициализированным completionStatus
                val habit = Habit(
                    heading = heading,
                    subheading = selectedDays.joinToString(", "),
                    startDate = startDate,
                    endDate = endDate,
                    completionStatus = completionStatus // Используем completionStatus вместо isCompleted
                )

// Сохранение привычки в базе данных
                saveHabitToDatabase(habit)
                dismiss()
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDayButtons() {
        val days = listOf(
            Pair(R.id.btn_monday, "ПН"),
            Pair(R.id.btn_tuesday, "ВТ"),
            Pair(R.id.btn_wednesday, "СР"),
            Pair(R.id.btn_thursday, "ЧТ"),
            Pair(R.id.btn_friday, "ПТ"),
            Pair(R.id.btn_saturday, "СБ"),
            Pair(R.id.btn_sunday, "ВС")
        )

        days.forEach { (buttonId, day) ->
            findViewById<Button>(buttonId).setOnClickListener {
                if (selectedDays.contains(day)) {
                    selectedDays.remove(day)
                    it.setBackgroundColor(Color.LTGRAY) // Сбрасываем цвет кнопки
                } else {
                    selectedDays.add(day)
                    it.setBackgroundColor(Color.MAGENTA) // Подсвечиваем выбранный день
                }
            }
        }
    }

    fun setHabitData(habit: Habit) {
        habitId = habit.id
        val etHeading = findViewById<EditText>(R.id.et_heading)
        val etStartDate = findViewById<EditText>(R.id.et_start_date)
        val etEndDate = findViewById<EditText>(R.id.et_end_date)

        etHeading.setText(habit.heading)
        etStartDate.setText(habit.startDate)
        etEndDate.setText(habit.endDate)

        selectedDays = habit.subheading.split(", ").toMutableList()
        updateDayButtonColors() // Обновите цвета кнопок дней
    }

    private fun updateDayButtonColors() {
        val days = listOf(
            Pair(R.id.btn_monday, "ПН"),
            Pair(R.id.btn_tuesday, "ВТ"),
            Pair(R.id.btn_wednesday, "СР"),
            Pair(R.id.btn_thursday, "ЧТ"),
            Pair(R.id.btn_friday, "ПТ"),
            Pair(R.id.btn_saturday, "СБ"),
            Pair(R.id.btn_sunday, "ВС")
        )

        days.forEach { (buttonId, day) ->
            val button = findViewById<Button>(buttonId)
            if (selectedDays.contains(day)) {
                button.setBackgroundColor(Color.MAGENTA) // Подсвечиваем выбранный день
            } else {
                button.setBackgroundColor(Color.LTGRAY) // Сбрасываем цвет кнопки
            }
        }
    }

    private fun saveHabitToDatabase(habit: Habit) {
        val db = FirebaseFirestore.getInstance()

        if (habitId == null) {
            val newHabitId = db.collection("habits").document().id
            val completionStatus = mutableMapOf<String, Boolean>()
            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val startDate = dateFormat.parse(habit.startDate) ?: Date()
            val endDate = dateFormat.parse(habit.endDate) ?: Date()

            val calendar = Calendar.getInstance()
            calendar.time = startDate

            while (calendar.time <= endDate) {
                val dateKey = dateFormat.format(calendar.time)
                val formatterInput = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                val formatterOutput = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val localDate = LocalDate.parse(dateKey, formatterInput)
                val formattedDateKey = localDate.format(formatterOutput)
                completionStatus[formattedDateKey] = false
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val newHabit = habit.copy(id = newHabitId, completionStatus = completionStatus)

            db.collection("habits").document(newHabitId)
                .set(newHabit)
                .addOnSuccessListener {
                    Log.d("Firestore", "Привычка успешно добавлена")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Ошибка при добавлении привычки", e)
                }
        } else {
            db.collection("habits").document(habitId!!)
                .update(
                    "heading", habit.heading,
                    "subheading", selectedDays.joinToString(", "),
                    "startDate", habit.startDate,
                    "endDate", habit.endDate
                )
                .addOnSuccessListener {
                    Log.d("Firestore", "Привычка успешно обновлена")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Ошибка при обновлении привычки", e)
                }
        }
    }


    private fun addHabitToDatabase(habit: Habit) {
        val db = FirebaseFirestore.getInstance()

        val habitId = db.collection("habits").document().id

        db.collection("habits").document(habitId)
            .set(habit.copy(id = habitId))
            .addOnSuccessListener {
                Log.d("Firestore", "Привычка успешно добавлена")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Ошибка при добавлении привычки", e)
            }
    }
}
