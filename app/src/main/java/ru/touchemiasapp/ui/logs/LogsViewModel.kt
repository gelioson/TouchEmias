package ru.touchemiasapp.ui.logs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.repository.LogRepository
import ru.touchemiasapp.domain.model.LogEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val logRepository: LogRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val logs: StateFlow<List<LogEntry>> = logRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearLogs() {
        viewModelScope.launch { logRepository.clear() }
    }

    fun shareLogs(): Intent {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val content = logs.value.joinToString("\n\n") { entry ->
            buildString {
                appendLine("[${sdf.format(Date(entry.timestamp))}]")
                appendLine("Врач: ${entry.doctorName} (ID: ${entry.doctorId})")
                appendLine("Найдено слотов: ${entry.slotsFound}")
                entry.bookedSlot?.let { appendLine("Записано: $it") }
                entry.errorMessage?.let { appendLine("Ошибка: $it") }
                appendLine("Ответ: ${entry.rawResponse}")
            }
        }

        val logsDir = File(context.cacheDir, "logs").also { it.mkdirs() }
        val file = File(logsDir, "touchemias_logs.txt")
        file.writeText(content)

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, context.getString(ru.touchemiasapp.R.string.logs_share_subject))
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
