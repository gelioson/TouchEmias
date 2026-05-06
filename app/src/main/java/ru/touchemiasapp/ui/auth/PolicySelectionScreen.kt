package ru.touchemiasapp.ui.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.touchemiasapp.data.api.auth.OmsPolicy

@Composable
fun PolicySelectionScreen(viewModel: PolicySelectionViewModel, onSelected: () -> Unit) {
    val done by viewModel.done.collectAsState()

    LaunchedEffect(done) { if (done) onSelected() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Выберите полис") }) }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            items(viewModel.policies) { policy ->
                PolicyItem(policy = policy, onClick = { viewModel.selectPolicy(policy) })
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun PolicyItem(policy: OmsPolicy, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Text(
            text = policy.policyName ?: policy.omsNumber,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = "Полис: ${policy.omsNumber}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
