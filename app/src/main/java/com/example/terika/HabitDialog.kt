package com.example.terika

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.KeyEvent
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
import java.util.* //

class HabitDialog(context: Context) : Dialog(context) {

    private var selectedDays = mutableListOf<String>()
    private var selectedDateType: String = ""
    private var calendarView: CalendarView

    init {
        setContentView(R.layout.dialog_add_habit)
        val etHeading = findViewById<EditText>(R.id.et_heading)
        val etStartDate = findViewById<EditText>(R.id.et_start_date)
        val etEndDate = findViewById<EditText>(R.id.et_end_date)
        etHeading.requestFocus()
        // Инициализация CalendarView
        calendarView = findViewById(R.id.calendar_view)
        calendarView.visibility = View.GONE

        etHeading.setOnEditorActionListener { v, actionId, event ->
            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                // Закрытие клавиатуры
                Log.d("HabitDialog", "Action ID: $actionId, Key Code: ${event?.keyCode}")
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                etHeading.clearFocus()
                etHeading.isFocusable = false
                etHeading.isFocusableInTouchMode = false

                etStartDate.requestFocus()

                true
            } else {
                false
            }
        }

        etHeading.setOnClickListener {

            etHeading.isFocusable = true
            etHeading.isFocusableInTouchMode = true
            etHeading.requestFocus()


            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etHeading, InputMethodManager.SHOW_IMPLICIT)
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
                addHabitToDatabase(Habit(heading = heading, subheading = selectedDays.joinToString(", "), startDate = startDate, endDate = endDate, isCompleted = false))
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
