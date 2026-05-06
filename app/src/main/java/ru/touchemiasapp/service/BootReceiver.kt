package ru.touchemiasapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.touchemiasapp.data.repository.WatchJobRepository
import javax.inject.Inject

// Restarts monitoring after device reboot if a job was active
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var watchJobRepository: WatchJobRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        CoroutineScope(Dispatchers.IO).launch {
            if (watchJobRepository.getActive() != null) {
                val serviceIntent = Intent(context, MonitorService::class.java)
                context.startForegroundService(serviceIntent)
            }
        }
    }
}
