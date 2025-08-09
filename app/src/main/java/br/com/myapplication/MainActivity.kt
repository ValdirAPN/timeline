package br.com.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.myapplication.domain.Day
import br.com.myapplication.domain.Event
import br.com.myapplication.presentation.TimelineState
import br.com.myapplication.presentation.TimelineUiIntent
import br.com.myapplication.presentation.TimelineViewModel
import br.com.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge()
            MyApplicationTheme {
                val viewModel by viewModels<TimelineViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        TimelineScreen(
                            state = state,
                            processIntent = viewModel::processIntent,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    state: TimelineState,
    processIntent: (TimelineUiIntent) -> Unit,
) = with(state) {
    // Use a mutableStateOf to hold the list of weeks so we can update it
    val initialPage = remember { 50 } // The initial page is the center of our initial list
    val coroutineScope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { weeks.size }
    )

    // Remember the month and year to display, updating when the pager state changes
    val displayedMonthAndYear = remember(pagerState.currentPage, weeks) {
        val firstDayOfTheWeek = weeks[pagerState.currentPage].first().date
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        firstDayOfTheWeek.format(formatter)
    }

    LaunchedEffect(state.initialPage) {
        coroutineScope.launch {
            pagerState.scrollToPage(state.initialPage)
        }
    }

    // This LaunchedEffect will monitor the pager state and load more weeks when needed
    LaunchedEffect(pagerState.currentPage) {
        val threshold = 10

        // Check if the user is scrolling towards the end
        if (pagerState.currentPage > weeks.size - threshold) {
            processIntent(TimelineUiIntent.LoadNewWeeks(1))
        }
        // Check if the user is scrolling towards the beginning
        else if (pagerState.currentPage < threshold) {
            processIntent(TimelineUiIntent.LoadNewWeeks(-1))
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the month and year
        Text(
            text = displayedMonthAndYear,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { page ->
            val week = weeks[page]
            WeekView(
                week = week,
                weekEvents = state.events
            )
        }
    }
}

/**
 * A composable function to display a single week.
 * @param week A list of Day objects representing the week.
 */
@Composable
fun WeekView(week: List<Day>, weekEvents: List<Event>) {
    val hours = 1..23
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier
                .background(Color.White)
                .weight(1f))
            week.forEach { day ->
                DayItem(day = day)
            }
        }
        Column(Modifier.verticalScroll(rememberScrollState())) {
            hours.forEach { hour ->
                Row {
                    Box(
                        Modifier
                            .background(Color.White)
                            .weight(1f)
                            .height(80.dp)
                            .border(1.dp, Color.Gray)
                    ) {
                        Text(hour.toString())
                    }
                    week.forEach { weekItem ->
                        Box(
                            Modifier
                                .background(Color.White)
                                .weight(1f)
                                .height(80.dp)
                                .border(1.dp, Color.Gray)
                        ) {
                            weekEvents.firstOrNull { event -> (event.date == weekItem.date && hour in event.startTime.hour until event.endTime.hour) }
                                ?.let { event ->
                                    Text(event.title, modifier = Modifier.background(event.color.copy(alpha = 0.5f)))
                                }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A composable function to display a single day.
 * @param day The Day object to display.
 */
@Composable
fun RowScope.DayItem(day: Day) {
    val dayOfMonth = day.date.dayOfMonth.toString()
    val dayOfWeek =
        day.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
    val backgroundColor = if (day.isToday) MaterialTheme.colorScheme.primary else Color.LightGray
    val contentColor = if (day.isToday) MaterialTheme.colorScheme.onPrimary else Color.Black

    Column(
        modifier = Modifier
            .height(56.dp)
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayOfWeek.uppercase(Locale.getDefault()),
            color = contentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = dayOfMonth,
            color = contentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimelineScreenPreview() {
    MyApplicationTheme {
        TimelineScreen(
            state = TimelineState(),
            processIntent = {}
        )
    }
}