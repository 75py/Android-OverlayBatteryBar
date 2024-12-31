package com.nagopy.android.overlaybatterybar

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BootCompletedReceiverTest {

    lateinit var bootCompletedReceiver: BootCompletedReceiver

    @Mock
    lateinit var userSettings: UserSettings

    @Mock
    lateinit var serviceHandler: MainService.Handler

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        bootCompletedReceiver = BootCompletedReceiver()
    }

    @Test
    fun onBroadcastReceived_disabled() {
        `when`(userSettings.isBatteryBarEnabled()).thenReturn(false)
        bootCompletedReceiver.execute(userSettings, serviceHandler)
        verify(serviceHandler, never()).startService()
    }

    @Test
    fun onBroadcastReceived_enabled() {
        `when`(userSettings.isBatteryBarEnabled()).thenReturn(true)
        bootCompletedReceiver.execute(userSettings, serviceHandler)
        verify(serviceHandler, times(1)).startService()
    }

}