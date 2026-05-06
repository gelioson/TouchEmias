package ru.touchemiasapp.ui.specialities

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
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
import ru.touchemiasapp.domain.model.Speciality

@Composable
fun SpecialitiesScreen(
    viewModel: SpecialitiesViewModel,
    onSpecialitySelected: (Long, String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.specialities_title)) }) }
    ) { padding ->
        when (val s = state) {
            is SpecialitiesUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is SpecialitiesUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(s.message, color = MaterialTheme.colorScheme.error)
            }

            is SpecialitiesUiState.Success -> LazyColumn(
                Modifier.fillMaxSize().padding(padding)
            ) {
                if (s.items.isEmpty()) {
                    item { Text(stringResource(R.string.specialities_empty), Modifier.padding(16.dp)) }
                } else {
                    items(s.items, key = { it.id }) { speciality ->
                        SpecialityItem(speciality) { onSpecialitySelected(speciality.id, speciality.name) }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialityItem(speciality: Speciality, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(speciality.name) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    )
}
