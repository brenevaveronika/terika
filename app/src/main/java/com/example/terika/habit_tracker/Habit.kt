package com.example.terika.habit_tracker

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Habit(
    val imageResId: Int,
    val heading: String,
    val subheading: String,
    var checkboxLineId: Int,
    var checkboxFillId: Int
) : Parcelable