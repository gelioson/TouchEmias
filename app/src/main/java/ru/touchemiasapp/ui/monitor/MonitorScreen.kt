package ru.touchemiasapp.ui.monitor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.touchemiasapp.R
import ru.touchemiasapp.domain.model.WatchConfig

@Composable
fun MonitorScreen(
    viewModel: MonitorViewModel,
    onNavigateSpecialities: () -> Unit,
    onNavigateLogs: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.monitor_title)) },
                actions = {
                    IconButton(onNavigateLogs) {
                        Icon(Icons.Filled.History, contentDescription = "Логи")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusCard(isActive = state.isActive)

            if (state.config != null) {
                ConfigCard(config = state.config!!)
            }

            Spacer(Modifier.height(8.dp))

            if (state.isActive) {
                Button(
                    onClick = viewModel::stopMonitoring,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Stop, null, Modifier.padding(end = 8.dp))
                    Text(stringResource(R.string.monitor_stop_btn))
                }
            } else {
                if (state.config != null) {
                    Button(
                        onClick = viewModel::startMonitoring,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PlayArrow, null, Modifier.padding(end = 8.dp))
                        Text(stringResource(R.string.monitor_start_btn))
                    }
                }

                OutlinedButton(
                    onClick = onNavigateSpecialities,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isActive
                ) {
                    Text(if (state.config == null) "Настроить мониторинг" else "Изменить параметры")
                }
            }

            if (state.isActive) {
                Text(
                    stringResource(R.string.monitor_edit_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatusCard(isActive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1B5E20) else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.MonitorHeart,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    "Статус мониторинга",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) Color.White.copy(0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (isActive) stringResource(R.string.monitor_status_active)
                    else stringResource(R.string.monitor_status_inactive),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConfigCard(config: WatchConfig) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(config.specialityName, style = MaterialTheme.typography.titleSmall)
            Text("Врачей: ${config.doctors.size}", style = MaterialTheme.typography.bodySmall)
            Text("Дат: ${config.selectedDates.size}", style = MaterialTheme.typography.bodySmall)
            Text("Время: ${config.timeFrom} – ${config.timeTo}", style = MaterialTheme.typography.bodySmall)
            Text(
                "Интервал: ${intervalLabel(config.intervalSeconds)}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Режим: ${if (config.mode.name == "AUTO_BOOK") "Автозапись" else "Уведомление"}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun intervalLabel(seconds: Int) = when (seconds) {
    30 -> "30 сек"
    60 -> "1 мин"
    300 -> "5 мин"
    600 -> "10 мин"
    1800 -> "30 мин"
    3600 -> "1 час"
    else -> "${seconds}с"
}
