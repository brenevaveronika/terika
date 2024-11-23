package com.example.terika

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CalendarView // НОВЫЙ ИМПОРТ
import android.widget.EditText
import android.widget.Toast
import com.example.terika.habit_tracker.Habit
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat // НОВЫЙ ИМПОРТ
import java.util.* // НОВЫЙ ИМПОРТ

class HabitDialog(context: Context) : Dialog(context) {

    private var selectedDays = mutableListOf<String>()
    private var selectedDateType: String = "" // НОВАЯ ПЕРЕМЕННАЯ
    private lateinit var calendarView: CalendarView // НОВАЯ ПЕРЕМЕННАЯ

    init {
        setContentView(R.layout.dialog_add_habit)

        val etHeading = findViewById<EditText>(R.id.et_heading)
        val etStartDate = findViewById<EditText>(R.id.et_start_date)
        val etEndDate = findViewById<EditText>(R.id.et_end_date)

        // Инициализация CalendarView
        calendarView = findViewById(R.id.calendar_view) // НОВАЯ СТРОКА
        calendarView.visibility = View.GONE // Скрываем CalendarView по умолчанию

        // Обработка нажатий на кнопки дней недели
        setupDayButtons()

        // НОВЫЕ СТРОКИ ДЛЯ ОБРАБОТКИ НАЖАТИЙ НА EditText
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

        // НОВАЯ СТРОКА ДЛЯ УСТАНОВКИ СЛУШАТЕЛЯ ДАТЫ
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)

            if (selectedDateType == "start") {
                etStartDate.setText(formattedDate)
                Log.d("HabitDialog", "Start date set: $formattedDate")
            } else if (selectedDateType == "end") {
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
                addHabitToDatabase(Habit(heading = heading, subheading = selectedDays.joinToString(", "), startDate = startDate, endDate = endDate, isCompleted = false))
                dismiss()
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDayButtons() {
        val days = listOf(
            Pair(R.id.btn_monday, "MONDAY"),
            Pair(R.id.btn_tuesday, "TUESDAY"),
            Pair(R.id.btn_wednesday, "WEDNESDAY"),
            Pair(R.id.btn_thursday, "THURSDAY"),
            Pair(R.id.btn_friday, "FRIDAY"),
            Pair(R.id.btn_saturday, "SATURDAY"),
            Pair(R.id.btn_sunday, "SUNDAY")
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

    private fun addHabitToDatabase(habit: Habit) {
        val db = FirebaseFirestore.getInstance()

        // Генерируем уникальный ID для привычки
        val habitId = db.collection("habits").document().id

        db.collection("habits").document(habitId)
            .set(habit.copy(id = habitId))
            .addOnSuccessListener {
                Log.d("Firestore", "Привычка успешно добавлена")
                // Здесь можно обновить RecyclerView или уведомить об изменениях
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Ошибка при добавлении привычки", e)
            }
    }
}
