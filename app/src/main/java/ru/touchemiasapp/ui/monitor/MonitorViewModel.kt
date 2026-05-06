package ru.touchemiasapp.ui.monitor

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.repository.WatchJobRepository
import ru.touchemiasapp.domain.model.WatchConfig
import ru.touchemiasapp.service.MonitorService
import javax.inject.Inject

data class MonitorUiState(
    val config: WatchConfig? = null,
    val isActive: Boolean = false
)

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val watchJobRepository: WatchJobRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val state: StateFlow<MonitorUiState> = watchJobRepository.observeLatest()
        .map { config -> MonitorUiState(config = config, isActive = config?.isActive == true) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonitorUiState())

    fun startMonitoring() {
        viewModelScope.launch {
            val config = watchJobRepository.getActive()
                ?: watchJobRepository.observeLatest().map { it }.stateIn(viewModelScope).value
                ?: return@launch
            watchJobRepository.setActive(config.id, true)
            context.startForegroundService(Intent(context, MonitorService::class.java))
        }
    }

    fun stopMonitoring() {
        context.startService(
            Intent(context, MonitorService::class.java).apply { action = MonitorService.ACTION_STOP }
        )
    }
}
