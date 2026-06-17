package com.mae.poldertracker.ui.session

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveSessionScreen(
    onSessionFinished: (durationSeconds: Int, startTimestamp: Long) -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: SessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var displaySeconds by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Start automatically when screen opens
    LaunchedEffect(Unit) {
        if (!uiState.isStarted) viewModel.start()
    }

    // Ticker: recalculates elapsed from timestamps every 500ms
    LaunchedEffect(uiState.isRunning, uiState.isStarted) {
        while (true) {
            displaySeconds = viewModel.computeElapsedSeconds(System.currentTimeMillis())
            delay(500)
        }
    }

    BackHandler(enabled = uiState.isStarted) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("¿Salir de la sesión?") },
            text = { Text("Se perderá el tiempo registrado. ¿Deseas salir sin guardar?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.reset()
                    showExitDialog = false
                    onNavigateUp()
                }) { Text("Salir") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Continuar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sesión activa") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.Stop, contentDescription = "Detener")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = formatChrono(displaySeconds),
                fontSize = 72.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                // Pause / Resume
                FilledTonalIconButton(
                    onClick = { if (uiState.isRunning) viewModel.pause() else viewModel.resume() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isRunning) "Pausar" else "Reanudar",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Stop / finish
                FilledIconButton(
                    onClick = {
                        val elapsed = viewModel.computeElapsedSeconds(System.currentTimeMillis())
                        val start = uiState.startEpochMillis
                        viewModel.reset()
                        onSessionFinished(elapsed, start)
                    },
                    modifier = Modifier.size(64.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Finalizar",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = if (uiState.isRunning) "En curso" else "Pausado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

private fun formatChrono(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
