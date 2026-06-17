package com.mae.poldertracker.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.poldertracker.data.local.GroundingSession
import com.mae.poldertracker.ui.components.CalendarHeatmap
import com.mae.poldertracker.ui.components.FeelingRatingDisplay
import com.mae.poldertracker.ui.home.formatDuration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val zone = ZoneId.systemDefault()
    val monthFmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("nl"))

    Scaffold(
        topBar = { TopAppBar(title = { Text("Geschiedenis") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = viewModel::previousMonth) {
                    Icon(Icons.Default.ChevronLeft, "Vorige maand")
                }
                Text(
                    text = uiState.yearMonth.atDay(1).format(monthFmt).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = viewModel::nextMonth) {
                    Icon(Icons.Default.ChevronRight, "Volgende maand")
                }
            }

            Spacer(Modifier.height(8.dp))

            CalendarHeatmap(
                yearMonth = uiState.yearMonth,
                activeDays = uiState.activeDays,
                onDayClick = { date ->
                    val session = uiState.sessions.firstOrNull { s ->
                        Instant.ofEpochMilli(s.startTimestamp).atZone(zone).toLocalDate() == date
                    }
                    viewModel.selectSession(session)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Sessies",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.sessions, key = { it.id }) { session ->
                    SessionListItem(
                        session = session,
                        onClick = { viewModel.selectSession(session) }
                    )
                }
            }
        }
    }

    uiState.selectedSession?.let { session ->
        SessionDetailSheet(
            session = session,
            onDismiss = { viewModel.selectSession(null) },
            onDelete = { viewModel.deleteSession(session) }
        )
    }
}

@Composable
private fun SessionListItem(session: GroundingSession, onClick: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val fmt = DateTimeFormatter.ofPattern("d MMM, HH:mm", Locale("nl"))
    val dateStr = Instant.ofEpochMilli(session.startTimestamp).atZone(zone).format(fmt)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(dateStr, style = MaterialTheme.typography.titleLarge)
                Text(formatDuration(session.durationSeconds), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            FeelingRatingDisplay(rating = session.feelingRating)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionDetailSheet(
    session: GroundingSession,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }
    val zone = ZoneId.systemDefault()
    val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale("nl"))
    val dateStr = Instant.ofEpochMilli(session.startTimestamp).atZone(zone).format(fmt)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(dateStr, style = MaterialTheme.typography.titleLarge)
            Text("Duur: ${formatDuration(session.durationSeconds)}", style = MaterialTheme.typography.bodyLarge)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Gevoel:", style = MaterialTheme.typography.bodyLarge)
                FeelingRatingDisplay(rating = session.feelingRating)
                Text("(${session.feelingRating}/5)", style = MaterialTheme.typography.bodyMedium)
            }
            session.notes?.let {
                Text("Notities: $it", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Sessie verwijderen")
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Sessie verwijderen?") },
            text = { Text("Deze actie kan niet ongedaan worden gemaakt.") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete(); onDismiss() }) {
                    Text("Verwijderen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Annuleren") }
            }
        )
    }
}
