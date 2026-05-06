package ru.touchemiasapp.ui.logs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.touchemiasapp.R
import ru.touchemiasapp.domain.model.LogEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen(viewModel: LogsViewModel, onBack: () -> Unit) {
    val logs by viewModel.logs.collectAsState()
    val context = LocalContext.current
    val shareLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.logs_title)) },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { shareLauncher.launch(viewModel.shareLogs()) }) {
                        Icon(Icons.Filled.Share, stringResource(R.string.logs_share_btn))
                    }
                    IconButton(onClick = viewModel::clearLogs) {
                        Icon(Icons.Filled.Delete, stringResource(R.string.logs_clear_btn))
                    }
                }
            )
        }
    ) { padding ->
        if (logs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(stringResource(R.string.logs_empty))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(logs, key = { it.id }) { entry ->
                    LogEntryItem(entry)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(entry: LogEntry) {
    val sdf = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault())
    val isError = entry.errorMessage != null
    val hasSlots = entry.slotsFound > 0

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                entry.bookedSlot != null -> MaterialTheme.colorScheme.primaryContainer
                hasSlots -> MaterialTheme.colorScheme.secondaryContainer
                isError -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                sdf.format(Date(entry.timestamp)),
                style = MaterialTheme.typography.labelSmall
            )
            Text(entry.doctorName, style = MaterialTheme.typography.bodyMedium)

            when {
                entry.bookedSlot != null ->
                    Text(stringResource(R.string.logs_booked, entry.bookedSlot), style = MaterialTheme.typography.bodySmall)
                hasSlots ->
                    Text(stringResource(R.string.logs_slots_found, entry.slotsFound), style = MaterialTheme.typography.bodySmall)
                isError ->
                    Text(entry.errorMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                else ->
                    Text("Свободных слотов нет", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
