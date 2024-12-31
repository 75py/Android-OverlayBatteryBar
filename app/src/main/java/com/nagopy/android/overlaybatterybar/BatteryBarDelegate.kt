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
        , val batteryManager: BatteryManager
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

    fun updateBatteryLevel() {
        val batteryLevel = getCurrentBatteryLevel()
        val chargeLimit = userSettings.getBatteryChargeLimit()
        val newWidth = overlayViewManager.displayWidth * batteryLevel / chargeLimit
        Timber.d("batteryLevel=%d, chargeLimit=%d, width=%d", batteryLevel, chargeLimit, newWidth)

        barView.apply {
            setWidth(newWidth.toInt())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                allowViewToExtendOutsideScreen(userSettings.showOnStatusBar() && !powerManager.isPowerSaveMode)
            } else {
                allowViewToExtendOutsideScreen(userSettings.showOnStatusBar())
            }
        }

        barView.update()
    }

    fun getCurrentBatteryLevel(): Int {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_BATTERY_CHANGED -> {
                    updateBatteryLevel()

                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                    batteryChangedCallback?.invoke(getCurrentBatteryLevel(), isCharging)
                }
                PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                    updateBatteryLevel()
                }
            }

        }
    }

    var batteryChangedCallback: ((level: Int, isCharging: Boolean) -> Unit)? = null

}
