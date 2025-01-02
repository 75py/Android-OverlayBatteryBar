package com.nagopy.android.overlaybatterybar

import android.graphics.Insets
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.RoundedCorner
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.VisibleForTesting

class DisplaySize(
    private val windowManager: WindowManager
    , private val displayManager: DisplayManager
) {
    
    data class Result(
        val displayWidth: Int,
        val roundedCornerRadius: Int,
        val cutoutWidth: Int,
        val isCutoutLeft: Boolean,
    )
    
    fun calc(): Result {
        val displayWidth = getDisplayWidth()
        val roundedCornerRadius = getRoundedCorner()
        val cutoutInsets = getCutoutInsets()
        val cutoutWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (cutoutInsets?.right ?: 0) + (cutoutInsets?.left ?: 0)
        } else {
            0
        }
        val isCutoutLeft = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            (cutoutInsets?.left ?: 0) > 0
        } else {
            false
        }

        return Result(displayWidth, roundedCornerRadius, cutoutWidth, isCutoutLeft)
    }

    @VisibleForTesting
    fun getRoundedCorner(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            val roundedCorner = display?.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)
            return roundedCorner?.radius?.toInt() ?: 0
        } else {
            return 0
        }
    }

    @VisibleForTesting
    fun getCutoutInsets(): Insets? {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val metrics = windowManager.currentWindowMetrics

                // ノッチが上下にある場合はright, leftが0になる
                // ノッチが左右にある場合はrightかleftに幅が入る
                val insets = metrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.displayCutout())
                return insets
            }
            else -> {
                return null
            }
        }
    }

    @VisibleForTesting
    fun getDisplayWidth(): Int {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val metrics = windowManager.currentWindowMetrics
                return metrics.bounds.width()
            }
            else -> {
                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                return displayMetrics.widthPixels
            }
        }
    }

}
