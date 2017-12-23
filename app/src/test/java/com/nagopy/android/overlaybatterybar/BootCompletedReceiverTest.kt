package com.nagopy.android.overlaybatterybar

import android.content.Intent
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
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
@Config(constants = BuildConfig::class)
class BootCompletedReceiverTest {

    lateinit var app: App

    lateinit var bootCompletedReceiver: BootCompletedReceiver

    @Mock
    lateinit var userSettings: UserSettings

    @Mock
    lateinit var serviceHandler: MainService.Handler

    @Mock
    lateinit var intent: Intent

    val testModule = Kodein.Module(allowSilentOverride = true) {
        bind<UserSettings>() with singleton { userSettings }
        bind<MainService.Handler>() with singleton { serviceHandler }
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        app = RuntimeEnvironment.application.asApp()
        app.kodein.addImport(testModule, true)

        bootCompletedReceiver = BootCompletedReceiver()
    }

    @Test
    fun onBroadcastReceived_disabled() {
        `when`(userSettings.isBatteryBarEnabled()).thenReturn(false)
        bootCompletedReceiver.onReceive(app, intent)
        verify(serviceHandler, never()).startService()
    }

    @Test
    fun onBroadcastReceived_enabled() {
        `when`(userSettings.isBatteryBarEnabled()).thenReturn(true)
        bootCompletedReceiver.onReceive(app, intent)
        verify(serviceHandler, times(1)).startService()

    }

}