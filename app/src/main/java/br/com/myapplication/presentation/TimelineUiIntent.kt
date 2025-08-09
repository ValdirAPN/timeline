package br.com.myapplication.presentation

sealed interface TimelineUiIntent {
    /**
     * Intent to load a new batch of weeks.
     * @param direction The direction to load weeks, 1 for future, -1 for past.
     */
    data class LoadNewWeeks(val direction: Int) : TimelineUiIntent
}