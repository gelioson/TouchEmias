package ru.touchemiasapp

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.webkit.WebView
import dagger.hilt.android.HiltAndroidApp
import ru.touchemiasapp.BuildConfig

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_MONITOR,
                getString(R.string.notif_channel_monitor_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.notif_channel_monitor_desc) }
        )

        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERT,
                getString(R.string.notif_channel_alert_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = getString(R.string.notif_channel_alert_desc) }
        )
    }

    companion object {
        const val CHANNEL_MONITOR = "monitor"
        const val CHANNEL_ALERT = "alert"
    }
}
