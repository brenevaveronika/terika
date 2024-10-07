package com.example.terika.habit_tracker

data class Habit (
    val imageResId: Int,
    val heading: String,
    val subheading: String,
    var checkboxLineId: Int,
    var checkboxFillId: Int
)