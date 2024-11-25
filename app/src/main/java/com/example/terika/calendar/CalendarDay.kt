package com.example.terika.calendar

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate,
    var isToday: Boolean = false
)

