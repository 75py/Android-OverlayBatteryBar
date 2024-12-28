package com.nagopy.android.overlaybatterybar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.view.View
import com.nagopy.android.overlayviewmanager.OverlayView
import com.nagopy.android.overlayviewmanager.OverlayViewManager
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class BatteryBarDelegateTest {

    lateinit var batteryBarDelegate: BatteryBarDelegate

    lateinit var context: Context
    @Mock
    lateinit var powerManager: PowerManager
    @Mock
    lateinit var overlayViewManager: OverlayViewManager
    @Mock
    lateinit var userSettings: UserSettings
    @Mock
    lateinit var overlayView: OverlayView<View>

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        context = spy(RuntimeEnvironment.application)
        doNothing().`when`(context).unregisterReceiver(any(BroadcastReceiver::class.java))
        `when`(overlayViewManager.newOverlayView(any(View::class.java))).thenReturn(overlayView)

        batteryBarDelegate = BatteryBarDelegate(context, powerManager, overlayViewManager, userSettings)

        reset(powerManager)
        reset(overlayViewManager)
        reset(userSettings)
        reset(overlayView)
    }

    @Config(sdk = [(Build.VERSION_CODES.LOLLIPOP)])
    @Test
    fun getIntentFilter_API21() {
        val actionsIterator = batteryBarDelegate.intentFilter.actionsIterator()
        assertThat(actionsIterator.next(),
                `is`(Intent.ACTION_BATTERY_CHANGED))
        assertThat(actionsIterator.next(),
                `is`(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))
        assertThat(actionsIterator.hasNext(), `is`(false))
    }

    @Test
    fun start() {
        batteryBarDelegate.isStarted = false
        `when`(userSettings.getBatteryBarWidth()).thenReturn(10)

        batteryBarDelegate.start()

        verify(context, times(1))
                .registerReceiver(batteryBarDelegate.receiver, batteryBarDelegate.intentFilter)

        assertThat(batteryBarDelegate.isStarted, `is`(true))
        verify(batteryBarDelegate.barView, times(1)).setHeight(10)
        verify(batteryBarDelegate.barView, times(1)).show()
    }

    @Test
    fun start_alreadyStarted() {
        batteryBarDelegate.isStarted = true
        `when`(userSettings.getBatteryBarWidth()).thenReturn(10)

        batteryBarDelegate.start()

        verify(context, never())
                .registerReceiver(batteryBarDelegate.receiver, batteryBarDelegate.intentFilter)
        verify(batteryBarDelegate.barView, never()).setHeight(10)
        verify(batteryBarDelegate.barView, never()).show()
        assertThat(batteryBarDelegate.isStarted, `is`(true))
    }

    @Test
    fun stop() {
        batteryBarDelegate.isStarted = true

        batteryBarDelegate.stop()

        verify(context, times(1))
                .unregisterReceiver(batteryBarDelegate.receiver)
        verify(batteryBarDelegate.barView, times(1)).hide()
        assertThat(batteryBarDelegate.isStarted, `is`(false))
    }

    @Test
    fun stop_alreadyStopped() {
        batteryBarDelegate.isStarted = false

        batteryBarDelegate.stop()

        verify(context, never()).unregisterReceiver(batteryBarDelegate.receiver)
        verify(batteryBarDelegate.barView, never()).hide()
        assertThat(batteryBarDelegate.isStarted, `is`(false))
    }

    @Test
    fun isEnabled() {
        `when`(userSettings.isBatteryBarEnabled()).thenReturn(true)

        val isEnabled = batteryBarDelegate.isEnabled()

        assertThat(isEnabled, `is`(true))
        verify(userSettings, times(1)).isBatteryBarEnabled()
    }

    @Config(sdk = [(Build.VERSION_CODES.O)])
    @Test
    fun updateBatteryLevel_API26() {
        val level = 80
        val scale = 100
        `when`(overlayViewManager.displayWidth).thenReturn(1000)
        `when`(userSettings.showOnStatusBar()).thenReturn(true)
        `when`(powerManager.isPowerSaveMode).thenReturn(false)

        batteryBarDelegate.updateBatteryLevel(level, scale)

        assertThat(batteryBarDelegate.batteryLevel, `is`(level))
        assertThat(batteryBarDelegate.batteryScale, `is`(scale))
        verify(overlayView, times(1)).setWidth(800)
        verify(overlayView, times(1)).allowViewToExtendOutsideScreen(true)
        verify(userSettings, times(1)).showOnStatusBar()
        verify(overlayView, times(1)).update()
    }

    @Config(sdk = [(Build.VERSION_CODES.O)])
    @Test
    fun updateBatteryLevel_API26_powerSaverOn() {
        val level = 80
        val scale = 100
        `when`(overlayViewManager.displayWidth).thenReturn(1000)
        `when`(userSettings.showOnStatusBar()).thenReturn(true)
        `when`(powerManager.isPowerSaveMode).thenReturn(true)

        batteryBarDelegate.updateBatteryLevel(level, scale)

        assertThat(batteryBarDelegate.batteryLevel, `is`(level))
        assertThat(batteryBarDelegate.batteryScale, `is`(scale))
        verify(overlayView, times(1)).setWidth(800)
        verify(overlayView, times(1)).allowViewToExtendOutsideScreen(false)
        verify(userSettings, times(1)).showOnStatusBar()
        verify(overlayView, times(1)).update()
    }

    @Config(sdk = [(Build.VERSION_CODES.N_MR1)])
    @Test
    fun updateBatteryLevel_API25() {
        val level = 80
        val scale = 100
        `when`(overlayViewManager.displayWidth).thenReturn(1000)
        `when`(userSettings.showOnStatusBar()).thenReturn(true)

        batteryBarDelegate.updateBatteryLevel(level, scale)

        assertThat(batteryBarDelegate.batteryLevel, `is`(level))
        assertThat(batteryBarDelegate.batteryScale, `is`(scale))
        verify(overlayView, times(1)).setWidth(800)
        verify(overlayView, times(1)).allowViewToExtendOutsideScreen(true)
        verify(userSettings, times(1)).showOnStatusBar()
        verify(overlayView, times(1)).update()
    }

}