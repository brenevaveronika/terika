package com.example.terika.habit_tracker

import android.os.Parcelable
import com.example.terika.R
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Parcelize
data class Habit(
    var id: String = "",
    val heading: String = "",
    val subheading: String = "",
    val startDate: String = "",
    val endDate: String = "",
    var isCompleted: Boolean = false,
    val imageResId: Int = R.drawable.eyes,
    var checkboxLineId: Int = R.drawable.habit_checkbox_circle_line,
    var checkboxFillId: Int = R.drawable.habit_checkbox_circle_fill

) : Parcelable {
    fun getStartDateAsLocalDate(): LocalDate {
        return LocalDate.parse(startDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }
    fun getEndDateAsLocalDate(): LocalDate {
        return LocalDate.parse(endDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    }
}


