package com.mae.poldertracker.ui.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mae.poldertracker.reminder.Reminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateUp: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.openAddDialog()
    }

    fun requestAddOrPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val ok = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!ok) { notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS); return }
        }
        viewModel.openAddDialog()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recordatorios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = ::requestAddOrPermission) {
                Icon(Icons.Default.Add, "Agregar recordatorio")
            }
        }
    ) { padding ->
        if (state.reminders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Sin recordatorios", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Toca + para agregar uno",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        onEdit = { viewModel.openEditDialog(reminder) },
                        onDelete = { viewModel.deleteReminder(reminder) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (state.showAddDialog) {
        ReminderTimeDialog(
            title = "Nuevo recordatorio",
            initialHour = 9,
            initialMinute = 0,
            onConfirm = { h, m -> viewModel.addReminder(h, m) },
            onDismiss = { viewModel.dismissAddDialog() }
        )
    }

    state.editingReminder?.let { editing ->
        ReminderTimeDialog(
            title = "Editar recordatorio",
            initialHour = editing.hour,
            initialMinute = editing.minute,
            onConfirm = { h, m -> viewModel.updateReminder(editing.copy(hour = h, minute = m)) },
            onDismiss = { viewModel.dismissEditDialog() }
        )
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "%02d:%02d".format(reminder.hour, reminder.minute),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(initialHour = initialHour, initialMinute = initialMinute)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
