package com.nagopy.android.overlaybatterybar

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.github.salomonbrys.kodein.android.KodeinService
import com.github.salomonbrys.kodein.android.appKodein
import com.github.salomonbrys.kodein.instance
import com.github.salomonbrys.kodein.with
import timber.log.Timber

class MainService : KodeinService() {

    val batteryBarDelegate: BatteryBarDelegate by with(this).instance()

    val notificationManager: NotificationManager by instance()

    lateinit var n: NotificationCompat.Builder

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")

        inject(appKodein())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        n = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                .setContentTitle(getText(R.string.msg_running_title))
                .setContentText(getText(R.string.msg_running_text))
                .setSmallIcon(R.drawable.ic_stat_battery_unknown)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        Intent(this, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP),
                        PendingIntent.FLAG_UPDATE_CURRENT))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            n = n.setCategory(Notification.CATEGORY_SERVICE)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
        }
        startForeground(1, n.build())

        batteryBarDelegate.batteryChangedCallback = { level: Int, _: Int, isCharging: Boolean ->
            val iconLevel = if (isCharging) {
                level + 1000
            } else {
                level
            }
            n.setSmallIcon(R.drawable.ic_stat_battery_level, iconLevel)
            startForeground(1, n.build())
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun createNotificationChannel() {
        val channel = NotificationChannel(NOTIFICATION_CHANNEL, getText(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT).apply {
            enableLights(false)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            importance = NotificationManager.IMPORTANCE_LOW
        }
        notificationManager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")

        if (batteryBarDelegate.isEnabled()) {
            batteryBarDelegate.stop()
            batteryBarDelegate.start()
        } else {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Timber.d("onDestroy")
        batteryBarDelegate.stop()
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        batteryBarDelegate.stop()
        batteryBarDelegate.start()
    }

    override fun onBind(intent: Intent?): IBinder {
        throw RuntimeException("not implemented")
    }

    class Handler(val context: Context) {
        fun startService() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, MainService::class.java))
            } else {
                context.startService(Intent(context, MainService::class.java))
            }
        }
    }

    companion object {
        val NOTIFICATION_CHANNEL = "notificationChannel"
    }

}