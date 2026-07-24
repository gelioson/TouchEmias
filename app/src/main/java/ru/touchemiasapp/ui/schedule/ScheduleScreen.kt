package ru.touchemiasapp.ui.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.touchemiasapp.R
import ru.touchemiasapp.domain.model.MonitorMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel, onStartMonitoring: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    val today = LocalDate.now()
    val nextDays = (0..29).map { today.plusDays(it.toLong()) }
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFmt = DateTimeFormatter.ofPattern("EE, d MMM", Locale("ru"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.schedule_title)) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SectionLabel(stringResource(R.string.schedule_dates_label))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                nextDays.forEach { date ->
                    val key = date.format(fmt)
                    FilterChip(
                        selected = key in state.selectedDates,
                        onClick = { viewModel.toggleDate(key) },
                        label = { Text(date.format(displayFmt).replaceFirstChar { it.uppercaseChar() }) }
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            SectionLabel(stringResource(R.string.schedule_time_from))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.timeFrom,
                    onValueChange = viewModel::setTimeFrom,
                    label = { Text(stringResource(R.string.schedule_time_from)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.timeTo,
                    onValueChange = viewModel::setTimeTo,
                    label = { Text(stringResource(R.string.schedule_time_to)) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            SectionLabel(stringResource(R.string.schedule_mode_label))
            MonitorMode.entries.forEach { mode ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = state.mode == mode, onClick = { viewModel.setMode(mode) })
                    Text(
                        if (mode == MonitorMode.NOTIFY_ONLY)
                            stringResource(R.string.schedule_mode_notify)
                        else
                            stringResource(R.string.schedule_mode_book)
                    )
                }
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            SectionLabel(stringResource(R.string.schedule_interval_label))
            val intervalLabels = listOf(
                R.string.interval_30s, R.string.interval_1m, R.string.interval_5m,
                R.string.interval_10m, R.string.interval_30m, R.string.interval_1h
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                viewModel.availableIntervals.forEachIndexed { idx, seconds ->
                    FilterChip(
                        selected = state.intervalSeconds == seconds,
                        onClick = { viewModel.setInterval(seconds) },
                        label = { Text(stringResource(intervalLabels[idx])) }
                    )
                }
            }

            Button(
                onClick = { viewModel.save(onStartMonitoring) },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                enabled = state.selectedDates.isNotEmpty() && !state.isSaving
            ) {
                Text(stringResource(R.string.schedule_start_btn))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}
