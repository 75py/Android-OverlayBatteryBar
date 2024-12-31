package com.nagopy.android.overlaybatterybar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
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
    lateinit var batteryManager: BatteryManager
    @Mock
    lateinit var overlayViewManager: OverlayViewManager
    @Mock
    lateinit var userSettings: UserSettings
    @Mock
    lateinit var overlayView: OverlayView<View>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        context = spy(RuntimeEnvironment.application)
        doNothing().`when`(context).unregisterReceiver(any(BroadcastReceiver::class.java))
        `when`(overlayViewManager.newOverlayView(any(View::class.java))).thenReturn(overlayView)

        batteryBarDelegate = BatteryBarDelegate(context, powerManager, batteryManager, overlayViewManager, userSettings)

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

    @Test
    fun getCurrentBatteryLevel() {
        `when`(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).thenReturn(80)

        val batteryLevel = batteryBarDelegate.getCurrentBatteryLevel()
        verify(batteryManager, times(1)).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        assertThat(batteryLevel, `is`(80))
    }

    @Config(sdk = [(Build.VERSION_CODES.O)])
    @Test
    fun updateBatteryLevel_API26() {
        `when`(overlayViewManager.displayWidth).thenReturn(1000)
        `when`(userSettings.showOnStatusBar()).thenReturn(true)
        `when`(userSettings.getBatteryChargeLimit()).thenReturn(80)
        `when`(powerManager.isPowerSaveMode).thenReturn(false)
        `when`(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).thenReturn(80)

        batteryBarDelegate.updateBatteryLevel()

        verify(overlayView, times(1)).setWidth(1000)
        verify(overlayView, times(1)).allowViewToExtendOutsideScreen(true)
        verify(userSettings, times(1)).showOnStatusBar()
        verify(overlayView, times(1)).update()
    }

    @Config(sdk = [(Build.VERSION_CODES.O)])
    @Test
    fun updateBatteryLevel_API26_powerSaverOn() {
        `when`(overlayViewManager.displayWidth).thenReturn(1000)
        `when`(userSettings.showOnStatusBar()).thenReturn(true)
        `when`(userSettings.getBatteryChargeLimit()).thenReturn(100)
        `when`(powerManager.isPowerSaveMode).thenReturn(true)
        `when`(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).thenReturn(80)

        batteryBarDelegate.updateBatteryLevel()

        verify(overlayView, times(1)).setWidth(800)
        verify(overlayView, times(1)).allowViewToExtendOutsideScreen(false)
        verify(userSettings, times(1)).showOnStatusBar()
        verify(overlayView, times(1)).update()
    }

    @Config(sdk = [(Build.VERSION_CODES.N_MR1)])
    @Test
    fun updateBatteryLevel_API25() {
        `when`(overlayViewManager.displayWidth).thenReturn(1000)
        `when`(userSettings.showOnStatusBar()).thenReturn(true)
        `when`(userSettings.getBatteryChargeLimit()).thenReturn(80)
        `when`(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).thenReturn(40)

        batteryBarDelegate.updateBatteryLevel()

        verify(overlayView, times(1)).setWidth(500)
        verify(overlayView, times(1)).allowViewToExtendOutsideScreen(true)
        verify(userSettings, times(1)).showOnStatusBar()
        verify(overlayView, times(1)).update()
    }

}