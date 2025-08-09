package br.com.myapplication.domain

import java.time.LocalDate

/**
 * A data class to represent a single day in the timeline.
 * @param date The LocalDate object for the day.
 * @param isToday A boolean indicating if this is the current day.
 */
data class Day(val date: LocalDate, val isToday: Boolean)

typealias Week = List<Day>