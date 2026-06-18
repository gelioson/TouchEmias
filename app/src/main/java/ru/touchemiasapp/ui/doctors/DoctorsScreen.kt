package ru.touchemiasapp.ui.doctors

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.touchemiasapp.R
import ru.touchemiasapp.domain.model.TimeSlot

@Composable
fun DoctorsScreen(viewModel: DoctorsViewModel, onNext: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.doctors_title)) },
                navigationIcon = {
                    IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                }
            )
        },
        bottomBar = {
            if (state.selectedCount > 0) {
                Button(
                    onClick = { viewModel.saveSelectionDraft(onNext) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(stringResource(R.string.doctors_next_btn, state.selectedCount))
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
            }
            state.doctors.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(stringResource(R.string.doctors_empty))
            }
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(state.doctors, key = { it.doctor.availableResourceId }) { item ->
                    DoctorItem(
                        item = item,
                        onCheckToggle = { viewModel.toggleSelection(item.doctor.availableResourceId) },
                        onExpandToggle = { viewModel.toggleSlots(item.doctor.availableResourceId) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DoctorItem(
    item: DoctorUiItem,
    onCheckToggle: () -> Unit,
    onExpandToggle: () -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Checkbox(checked = item.isSelected, onCheckedChange = { onCheckToggle() })
            Column(Modifier.weight(1f).padding(start = 4.dp)) {
                Text(item.doctor.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${item.doctor.clinicName} • уч. ${item.doctor.ariaNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                item.doctor.nearestDate?.let {
                    Text("Ближайшая дата: $it", style = MaterialTheme.typography.bodySmall)
                }
            }
            IconButton(onExpandToggle) {
                Icon(
                    if (item.isSlotsExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "Расписание"
                )
            }
        }

        if (item.isSlotsExpanded) {
            when {
                item.isSlotsLoading -> CircularProgressIndicator(
                    Modifier.align(Alignment.CenterHorizontally).padding(8.dp)
                )
                item.slots.isEmpty() -> Text(
                    stringResource(R.string.doctors_slots_empty),
                    Modifier.padding(start = 56.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                else -> SlotsGrid(item.slots, Modifier.padding(start = 56.dp, end = 8.dp, bottom = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SlotsGrid(slots: List<TimeSlot>, modifier: Modifier = Modifier) {
    val grouped = slots.groupBy { it.date }
    Column(modifier) {
        grouped.forEach { (date, daySlots) ->
            Text(formatSlotDate(date), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
            FlowRow(modifier = Modifier.fillMaxWidth()) {
                daySlots.forEach { slot ->
                    Card(
                        modifier = Modifier.padding(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            slot.startTime,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

private val INPUT_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
private val OUTPUT_DATE_FMT = DateTimeFormatter.ofPattern("EE, d MMMM", Locale("ru"))

private fun formatSlotDate(date: String): String = runCatching {
    LocalDate.parse(date, INPUT_DATE_FMT).format(OUTPUT_DATE_FMT)
        .replaceFirstChar { it.uppercaseChar() }
}.getOrDefault(date)
