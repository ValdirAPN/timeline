package br.com.myapplication.presentation

import androidx.lifecycle.ViewModel
import br.com.myapplication.domain.Day
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

class TimelineViewModel : ViewModel() {

    private var _state = MutableStateFlow(TimelineState())
    val state = _state.asStateFlow()

    fun processIntent(intent: TimelineUiIntent) {
        when (intent) {
            is TimelineUiIntent.LoadNewWeeks -> loadNewWeeks(direction = intent.direction)
        }
    }

    companion object {
        const val WEEKS_TO_GENERATE = 50
    }

    init {
        val today = LocalDate.now()
        val weeks = generateInitialWeeks(today = today)
        _state.update { oldState ->
            oldState.copy(
                initialPage = WEEKS_TO_GENERATE,
                weeks = weeks,
            )
        }
    }

    /**
     * Generates an initial list of weeks, centered around today's date.
     * @param today The current date.
     * @param count The number of weeks to generate in each direction (past and future).
     */
    private fun generateInitialWeeks(today: LocalDate): List<List<Day>> {
        val weeks = mutableListOf<List<Day>>()
        val weekFields = WeekFields.of(Locale.getDefault())
        val startDay = today.minusWeeks(WEEKS_TO_GENERATE.toLong()).with(weekFields.dayOfWeek(), 1L)

        for (i in 0 until (WEEKS_TO_GENERATE * 2 + 1)) {
            val week = mutableListOf<Day>()
            var currentDay = startDay.plusWeeks(i.toLong())
            for (j in 0..6) {
                val day = currentDay.plusDays(j.toLong())
                week.add(Day(day, day.isEqual(today)))
            }
            weeks.add(week)
        }
        return weeks
    }

    /**
     * Loads a new batch of weeks.
     * @param direction The direction to load weeks, 1 for future, -1 for past.
     */
    fun loadNewWeeks(direction: Int) {
        val existingWeeks = _state.value.weeks
        val today = LocalDate.now()
        val newWeeks = mutableListOf<List<Day>>()
        val weekFields = WeekFields.of(Locale.getDefault())

        // Determine the start date for the new weeks based on the direction
        val startDay = if (direction == 1) {
            // Load future weeks: start from the day after the last day of the existing list
            existingWeeks.last().last().date.plusDays(1)
        } else {
            // Load past weeks: start from the day before the first day of the existing list
            existingWeeks.first().first().date.minusWeeks(WEEKS_TO_GENERATE.toLong())
        }

        var currentDay = startDay.with(weekFields.dayOfWeek(), 1L)

        for (i in 0 until WEEKS_TO_GENERATE) {
            val week = mutableListOf<Day>()
            for (j in 0..6) {
                val day = currentDay.plusDays(j.toLong())
                week.add(Day(day, day.isEqual(today)))
            }
            newWeeks.add(week)
            currentDay = currentDay.plusWeeks(1)
        }

        val result = if (direction == 1) {
            existingWeeks + newWeeks
        } else {
            newWeeks + existingWeeks
        }

        _state.update { oldState ->
            oldState.copy(
                weeks = result
            )
        }
        if (direction == -1) {
            _state.update { oldState ->
                oldState.copy(
                    initialPage = oldState.currentPage + WEEKS_TO_GENERATE
                )
            }
        }
    }
}