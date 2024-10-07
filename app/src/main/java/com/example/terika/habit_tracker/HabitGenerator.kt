package com.example.terika.habit_tracker

import com.example.terika.R

object HabitGenerator {
    private val imageIdList: List<Int> = listOf(
        R.drawable.sunlight_light,
        R.drawable.moon_alt_light,
        R.drawable.eyes
    )

    fun generateHabit (count: Int): List<Habit> =
        (0..count).map{ index ->
            Habit(
                imageResId = imageIdList.random(),
                heading = "Привычка $index",
                subheading = "Ежедневно",
                checkboxLineId = R.drawable.habit_checkbox_circle_line,
                checkboxFillId = R.drawable.habit_checkbox_circle_fill
            )
        }
}