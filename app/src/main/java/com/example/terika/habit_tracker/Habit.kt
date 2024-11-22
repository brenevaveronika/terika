package com.example.terika.habit_tracker

import android.os.Parcelable
import com.example.terika.R
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

@Parcelize
data class Habit(
    var id: String = "",
    val heading: String = "",
    val subheading: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val imageResId: Int = R.drawable.eyes,
    var checkboxLineId: Int = R.drawable.habit_checkbox_circle_line,
    var checkboxFillId: Int = R.drawable.habit_checkbox_circle_fill
) : Parcelable