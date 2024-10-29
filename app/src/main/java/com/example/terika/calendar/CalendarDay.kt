package com.example.terika.calendar

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate, // Use LocalDate to hold the date
    var isToday: Boolean = false // Flag to check if it's today's date
)

