package com.nagopy.android.overlaybatterybar

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import com.nagopy.android.overlayviewmanager.OverlayViewManager
import timber.log.Timber

@SuppressLint("ALLOW_VIEW_TO_EXTEND_OUTSIDE_SCREEN")
class BatteryBarDelegate(
        val context: Context
        , val powerManager: PowerManager
        , val overlayViewManager: OverlayViewManager
        , val userSettings: UserSettings) {

    val barView = overlayViewManager.newOverlayView(View(context).apply {
        setBackgroundColor(Color.WHITE)
        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
    }).apply {
        setHeight(6)
        allowViewToExtendOutsideScreen(true)
        setAlpha(0.8f)
    }

    val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
        }
    }

    var batteryLevel: Int = 0
    var batteryScale: Int = 0
    var isStarted: Boolean = false

    fun start() {
        Timber.d("startService isStarted:%s", isStarted)
        if (!isStarted) {
            Timber.d("start")
            context.registerReceiver(receiver, intentFilter)
            barView.apply {
                setHeight(userSettings.getBatteryBarWidth())
            }.show()
        }
        isStarted = true
    }

    fun stop() {
        Timber.d("stopService isStarted:%s", isStarted)
        if (isStarted) {
            Timber.d("stop")
            context.unregisterReceiver(receiver)
            barView.hide()
        }
        isStarted = false
    }

    fun isEnabled(): Boolean = userSettings.isBatteryBarEnabled()

    fun updateBatteryLevel(level: Int, scale: Int) {
        batteryLevel = level
        batteryScale = scale
        val newWidth = overlayViewManager.displayWidth * level / scale.toFloat()
        barView.apply {
            setWidth(newWidth.toInt())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                allowViewToExtendOutsideScreen(userSettings.showOnStatusBar() && !powerManager.isPowerSaveMode)
            } else {
                allowViewToExtendOutsideScreen(userSettings.showOnStatusBar())
            }
        }
        Timber.d("level = %d, scale = %d, width = %f", level, scale, newWidth)

        barView.update()
    }

    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    updateBatteryLevel(level, scale)

                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                    batteryChangedCallback?.invoke(level, scale, isCharging)
                }
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    updateBatteryLevel(batteryLevel, batteryScale)
                }
            }

        }
    }

    var batteryChangedCallback: ((level: Int, scale: Int, isCharging: Boolean) -> Unit)? = null

}
