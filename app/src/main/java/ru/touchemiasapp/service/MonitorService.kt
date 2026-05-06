package ru.touchemiasapp.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ru.touchemiasapp.App
import ru.touchemiasapp.MainActivity
import ru.touchemiasapp.R
import ru.touchemiasapp.data.preferences.UserPreferencesDataStore
import ru.touchemiasapp.data.repository.LogRepository
import ru.touchemiasapp.data.repository.WatchJobRepository
import ru.touchemiasapp.domain.model.LogEntry
import ru.touchemiasapp.domain.model.MonitorMode
import ru.touchemiasapp.domain.model.TimeSlot
import ru.touchemiasapp.domain.model.WatchConfig
import ru.touchemiasapp.domain.repository.EmiasRepository
import javax.inject.Inject

@AndroidEntryPoint
class MonitorService : Service() {

    @Inject lateinit var emiasRepository: EmiasRepository
    @Inject lateinit var watchJobRepository: WatchJobRepository
    @Inject lateinit var logRepository: LogRepository
    @Inject lateinit var userPrefs: UserPreferencesDataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopMonitor()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID_ONGOING, buildOngoingNotification())
        scope.launch { runMonitorLoop() }
        return START_STICKY
    }

    private suspend fun runMonitorLoop() {
        val prefs = userPrefs.userPreferences.first()
        if (!prefs.isComplete) { stopSelf(); return }

        val config = watchJobRepository.getActive()
        if (config == null) { stopSelf(); return }

        while (scope.isActive) {
            checkDoctors(config, prefs.omsNumber, prefs.birthDate)
            delay(config.intervalSeconds * 1000L)
        }
    }

    private suspend fun checkDoctors(config: WatchConfig, omsNumber: String, birthDate: String) {
        for (doctor in config.doctors) {
            val result = emiasRepository.getAvailableSlots(omsNumber, birthDate, doctor.availableResourceId, doctor.complexResourceId)

            val (slots, error) = if (result.isSuccess) {
                result.getOrDefault(emptyList()) to null
            } else {
                emptyList<TimeSlot>() to result.exceptionOrNull()?.message
            }

            val matching = slots.filter { slot ->
                slot.date in config.selectedDates && isTimeInRange(slot.startTime, config.timeFrom, config.timeTo)
            }

            var bookedSlot: String? = null
            if (matching.isNotEmpty()) {
                val first = matching.first()
                if (config.mode == MonitorMode.AUTO_BOOK) {
                    val bookResult = emiasRepository.createAppointment(omsNumber, birthDate, first)
                    if (bookResult.isSuccess) {
                        bookedSlot = "${first.date} ${first.startTime}"
                        showBookedNotification(doctor.name, first)
                        stopMonitor()
                    } else {
                        showSlotFoundNotification(doctor.name, first)
                    }
                } else {
                    showSlotFoundNotification(doctor.name, first)
                    stopMonitor()
                }
            }

            logRepository.add(
                LogEntry(
                    timestamp = System.currentTimeMillis(),
                    doctorId = doctor.availableResourceId,
                    doctorName = doctor.name,
                    slotsFound = matching.size,
                    bookedSlot = bookedSlot,
                    errorMessage = error,
                    rawResponse = result.fold(
                        onSuccess = { "slots: ${it.size}" },
                        onFailure = { it.message ?: "error" }
                    )
                )
            )

            if (bookedSlot != null) return
        }
    }

    private fun isTimeInRange(slotTime: String, from: String, to: String): Boolean {
        return slotTime >= from && slotTime <= to
    }

    private fun showSlotFoundNotification(doctorName: String, slot: TimeSlot) {
        val nm = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, App.CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_slot_found_title))
            .setContentText(getString(R.string.notif_slot_found_text, doctorName, slot.date, slot.startTime))
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(NOTIF_ID_ALERT, notification)
    }

    private fun showBookedNotification(doctorName: String, slot: TimeSlot) {
        val nm = getSystemService(NotificationManager::class.java)
        val notification = NotificationCompat.Builder(this, App.CHANNEL_ALERT)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_slot_booked_title))
            .setContentText(getString(R.string.notif_slot_booked_text, doctorName, slot.date, slot.startTime))
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        nm.notify(NOTIF_ID_ALERT, notification)
    }

    private fun buildOngoingNotification(): Notification =
        NotificationCompat.Builder(this, App.CHANNEL_MONITOR)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notif_monitor_title))
            .setContentText(getString(R.string.notif_monitor_text))
            .setOngoing(true)
            .setContentIntent(mainPendingIntent())
            .addAction(
                R.drawable.ic_notification,
                getString(R.string.monitor_stop_btn),
                stopPendingIntent()
            )
            .build()

    private fun mainPendingIntent(): PendingIntent =
        PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun stopPendingIntent(): PendingIntent =
        PendingIntent.getService(
            this, 1,
            Intent(this, MonitorService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun stopMonitor() {
        scope.launch {
            watchJobRepository.deactivateAll()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "ru.touchemiasapp.STOP_MONITOR"
        private const val NOTIF_ID_ONGOING = 1001
        private const val NOTIF_ID_ALERT = 1002
    }
}
