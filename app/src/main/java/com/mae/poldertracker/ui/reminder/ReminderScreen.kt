package com.mae.poldertracker.ui.reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Warning
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
import com.mae.poldertracker.reminder.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    onNavigateUp: () -> Unit,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var snackbarMsg by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Check both permissions on every recomposition (user may have returned from Settings)
    val hasNotifPerm = remember {
        derivedStateOf {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) true
            else ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    val hasExactAlarm = remember {
        derivedStateOf { ReminderScheduler.canScheduleExact(context) }
    }
    val isBatteryOptimized = remember {
        val pm = context.getSystemService(PowerManager::class.java)
        !pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) snackbarMsg = "Verleen meldingentoestemming in de instellingen"
        else viewModel.openAddDialog()
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { snackbarHostState.showSnackbar(it); snackbarMsg = null }
    }

    fun tryAdd() {
        if (!hasNotifPerm.value) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        viewModel.openAddDialog()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Herinneringen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Terug")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = ::tryAdd) {
                Icon(Icons.Default.Add, "Herinnering toevoegen")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // ── Exact alarm permission warning ──────────────────────────────
            if (!hasExactAlarm.value) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Precieze alarmen vereist",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Schakel 'Alarmen en herinneringen' in voor exacte meldingen.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                            } else {
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:${context.packageName}"))
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 4.dp)
                    ) {
                        Text("Instellingen openen")
                    }
                }
            }

            // ── Battery optimization warning ────────────────────────────────
            if (isBatteryOptimized) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Batterijoptimalisatie actief",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Herinneringen kunnen worden gemist. Schakel optimalisatie uit voor betrouwbare meldingen.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            val intent = try {
                                Intent(
                                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                    Uri.parse("package:${context.packageName}")
                                )
                            } catch (_: Exception) {
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End).padding(end = 8.dp, bottom = 4.dp)
                    ) {
                        Text("Uitschakelen")
                    }
                }
            }

            // ── Reminder list ───────────────────────────────────────────────
            if (state.reminders.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Geen herinneringen", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Tik op + om er een toe te voegen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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

            // ── Test button ─────────────────────────────────────────────────
            TextButton(
                onClick = { viewModel.testNotification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Melding testen", color = MaterialTheme.colorScheme.secondary)
            }
        }
    }

    if (state.showAddDialog) {
        ReminderTimeDialog(
            title = "Nieuwe herinnering",
            initialHour = 9, initialMinute = 0,
            onConfirm = { h, m -> viewModel.addReminder(h, m) },
            onDismiss = { viewModel.dismissAddDialog() }
        )
    }

    state.editingReminder?.let { editing ->
        ReminderTimeDialog(
            title = "Herinnering bewerken",
            initialHour = editing.hour, initialMinute = editing.minute,
            onConfirm = { h, m -> viewModel.updateReminder(editing.copy(hour = h, minute = m)) },
            onDismiss = { viewModel.dismissEditDialog() }
        )
    }
}

@Composable
private fun ReminderCard(reminder: Reminder, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                    Icon(Icons.Default.Edit, "Bewerken", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Verwijderen", tint = MaterialTheme.colorScheme.error)
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
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("Opslaan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuleren") }
        }
    )
}
