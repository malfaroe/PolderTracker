package com.mae.poldertracker.ui.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.poldertracker.ui.components.FeelingRatingSelector
import com.mae.poldertracker.ui.home.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseSessionScreen(
    durationSeconds: Int,
    startTimestamp: Long,
    onSaved: () -> Unit,
    viewModel: CloseSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(durationSeconds, startTimestamp) {
        viewModel.init(durationSeconds, startTimestamp)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onSaved()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sessie opslaan") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Duur",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        formatDuration(durationSeconds),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Hoe voelde je je?", style = MaterialTheme.typography.titleLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Angstig",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "Ontspannen",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                FeelingRatingSelector(
                    selected = uiState.feelingRating,
                    onSelect = viewModel::setRating
                )
            }

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::setNotes,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Notities (optioneel)") },
                placeholder = { Text("Iets wat je wilt onthouden...") },
                minLines = 3,
                maxLines = 6
            )

            Button(
                onClick = viewModel::save,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = uiState.feelingRating > 0
            ) {
                Text("Sessie opslaan")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
