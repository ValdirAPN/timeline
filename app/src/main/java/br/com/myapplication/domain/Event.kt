package br.com.myapplication.domain

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalTime

data class Event(
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val color: Color,
)
