package com.mae.poldertracker.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.poldertracker.ui.components.BarChart
import com.mae.poldertracker.ui.components.FeelingRatingDisplay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistieken") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = uiState.period == StatsPeriod.WEEK,
                    onClick = { viewModel.setPeriod(StatsPeriod.WEEK) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2)
                ) { Text("Week") }
                SegmentedButton(
                    selected = uiState.period == StatsPeriod.MONTH,
                    onClick = { viewModel.setPeriod(StatsPeriod.MONTH) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2)
                ) { Text("Maand") }
            }

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Minuten per ${if (uiState.period == StatsPeriod.WEEK) "dag" else "week"}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(Modifier.height(12.dp))
                    BarChart(entries = uiState.barData, modifier = Modifier.fillMaxWidth())
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(label = "Sessies", value = "${uiState.sessionCount}", modifier = Modifier.weight(1f))
                SummaryCard(
                    label = "Gemiddelde duur",
                    value = "${uiState.avgDurationMinutes.roundToInt()} min",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    label = "Langste reeks",
                    value = "${uiState.maxStreak} dagen",
                    modifier = Modifier.weight(1f)
                )
                ElevatedCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Gemiddeld gevoel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("%.1f".format(uiState.avgRating), style = MaterialTheme.typography.headlineMedium)
                            TrendIcon(uiState.ratingTrend)
                        }
                        FeelingRatingDisplay(rating = uiState.avgRating.roundToInt().coerceIn(0, 5))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium)
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun TrendIcon(trend: Float) {
    when {
        trend > 0.1f -> Icon(Icons.Default.ArrowUpward, null,
            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        trend < -0.1f -> Icon(Icons.Default.ArrowDownward, null,
            tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
        else -> Icon(Icons.Default.Remove, null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
    }
}
