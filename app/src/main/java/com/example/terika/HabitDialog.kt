package com.example.terika

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.terika.habit_tracker.Habit
import com.google.firebase.firestore.FirebaseFirestore

class HabitDialog(context: Context) : Dialog(context) {

    private var selectedDays = mutableListOf<String>()

    init {
        setContentView(R.layout.dialog_add_habit)

        val etHeading = findViewById<EditText>(R.id.et_heading)
        val etStartDate = findViewById<EditText>(R.id.et_start_date)
        val etEndDate = findViewById<EditText>(R.id.et_end_date)

        // Обработка нажатий на кнопки дней недели
        setupDayButtons()

        findViewById<Button>(R.id.btn_add_habit).setOnClickListener {
            val heading = etHeading.text.toString()
            val startDate = etStartDate.text.toString()
            val endDate = etEndDate.text.toString()

            if (heading.isNotEmpty() && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                addHabitToDatabase(Habit(heading = heading, subheading = selectedDays.joinToString(", "), startDate = startDate, endDate = endDate))
                dismiss()
            } else {
                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupDayButtons() {
        val days = listOf(
            Pair(R.id.btn_monday, "Пн"),
            Pair(R.id.btn_tuesday, "Вт"),
            Pair(R.id.btn_wednesday, "Ср"),
            Pair(R.id.btn_thursday, "Чт"),
            Pair(R.id.btn_friday, "Пт"),
            Pair(R.id.btn_saturday, "Сб"),
            Pair(R.id.btn_sunday, "Вс")
        )

        days.forEach { (buttonId, day) ->
            findViewById<Button>(buttonId).setOnClickListener {
                if (selectedDays.contains(day)) {
                    selectedDays.remove(day)
                    it.setBackgroundColor(Color.LTGRAY) // Сбрасываем цвет кнопки
                } else {
                    selectedDays.add(day)
                    it.setBackgroundColor(Color.GREEN) // Подсвечиваем выбранный день
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
