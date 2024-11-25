package com.example.terika.diary

import java.text.SimpleDateFormat
import java.util.*

data class DiaryEntry(
    var mood: String = "",
    var notes: String = "",
    var id: String = "",
    var timestamp: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}

