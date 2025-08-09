package br.com.myapplication.presentation

import androidx.compose.ui.graphics.Color
import br.com.myapplication.domain.Event
import br.com.myapplication.domain.Week
import java.time.LocalDate
import java.time.LocalTime

data class TimelineState(
    val initialPage: Int = 0,
    val currentPage: Int = initialPage,
    val weeks: List<Week> = emptyList(),
    val events: List<Event> = listOf(
        Event(
            title = "Oftalmologista",
            date = LocalDate.now(),
            startTime = LocalTime.of(13, 0),
            endTime = LocalTime.of(15, 30),
            color = Color.Blue
        )
    ),
)
