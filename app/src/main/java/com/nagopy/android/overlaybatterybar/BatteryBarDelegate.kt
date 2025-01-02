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
import kotlin.math.max

@SuppressLint("ALLOW_VIEW_TO_EXTEND_OUTSIDE_SCREEN")
class BatteryBarDelegate(
    val context: Context,
    val powerManager: PowerManager,
    val batteryManager: BatteryManager,
    overlayViewManager: OverlayViewManager,
    val displaySize: DisplaySize,
    val userSettings: UserSettings
) {

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

    data class BatteryBarPositionAndWidth(
        val position: Int,
        val width: Int,
    )

    fun calculateBatteryBarPositionAndWidth(): BatteryBarPositionAndWidth {
        val calcResult = displaySize.calc()
        var leftPosition = 0

        var maxBarWidth = calcResult.displayWidth

        if (userSettings.showOnStatusBar()) {
            // ステータスバーの上に表示する場合、allowViewToExtendOutsideScreen(true)になる
            // このとき、角丸やノッチを考慮しない左上の座標が原点になるので、左右にノッチや角がある場合を考慮する必要がある

            if (calcResult.isCutoutLeft) {
                // 左側にノッチがあるので、【ノッチ or 左上角丸の大きい方】の幅分だけ開始位置を左にずらす
                leftPosition = max(calcResult.cutoutWidth, calcResult.roundedCornerRadius)
                // 最大幅も、開始位置を左にずらした分だけ減らす
                maxBarWidth -= leftPosition
                // さらに、右上角丸分だけ減らす
                maxBarWidth -= calcResult.roundedCornerRadius
            } else {
                // 右側にノッチがある場合

                // 左上の角丸分だけ開始位置を左にずらす
                leftPosition = calcResult.roundedCornerRadius
                // 最大幅も、開始位置を左にずらした分だけ減らす
                maxBarWidth -= leftPosition
                // さらに、【ノッチ or 右上角丸の大きい方】の幅分だけ最大幅を減らす
                maxBarWidth -= max(calcResult.cutoutWidth, calcResult.roundedCornerRadius)
            }
        } else {
            // ノッチ幅が考慮されていないため、ノッチの幅分だけ最大幅を減らす
            maxBarWidth -= calcResult.cutoutWidth
        }
        val batteryLevel = getCurrentBatteryLevel()
        val chargeLimit = userSettings.getBatteryChargeLimit()
        val newWidth = maxBarWidth * batteryLevel / chargeLimit
        val result = BatteryBarPositionAndWidth(
            position = leftPosition,
            width = newWidth.toInt(),
        )

        Timber.d(
            "displayWidth:%s, roundedCornerRadius:%s, cutoutWidth:%s, isCutoutLeft:%s, maxBarWidth:%s, leftPosition:%s, batteryLevel:%s, chargeLimit:%s, newWidth:%s, result:%s",
            calcResult.displayWidth,
            calcResult.roundedCornerRadius,
            calcResult.cutoutWidth,
            calcResult.isCutoutLeft,
            maxBarWidth,
            leftPosition,
            batteryLevel,
            chargeLimit,
            newWidth,
            result
        )
        return result
    }

    fun updateBatteryLevel() {
        val bar = calculateBatteryBarPositionAndWidth()

        barView.apply {
            setX(bar.position)
            setWidth(bar.width)
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
