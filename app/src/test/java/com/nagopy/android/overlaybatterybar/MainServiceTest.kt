package com.nagopy.android.overlaybatterybar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.android.androidServiceScope
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.scopedSingleton
import com.github.salomonbrys.kodein.singleton
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class MainServiceTest {

    lateinit var app: App

    lateinit var mainServiceController: ServiceController<MainService>

    @Mock
    lateinit var batteryBarDelegate: BatteryBarDelegate

    @Mock
    lateinit var notificationManager: NotificationManager

    val testModule = Kodein.Module(allowSilentOverride = true) {
        bind<BatteryBarDelegate>() with scopedSingleton(androidServiceScope) { batteryBarDelegate }
        bind<NotificationManager>() with singleton { notificationManager }
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        app = RuntimeEnvironment.application.asApp()
        app.kodein.addImport(testModule, true)

        mainServiceController = Robolectric.buildService(MainService::class.java).create()
    }

    @Config(sdk = [Build.VERSION_CODES.O])
    @Test
    fun onCreate_API26() {
        val service = mainServiceController.get()
        assertThat(service.batteryBarDelegate, `is`(notNullValue()))

        verify(notificationManager, times(1))
                .createNotificationChannel(ArgumentMatchers.any(NotificationChannel::class.java))
    }

    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    @Test
    fun onCreate() {
        val service = mainServiceController.get()
        assertThat(service.batteryBarDelegate, `is`(notNullValue()))
    }

    @Test
    fun onStartCommand_enabled() {
        `when`(batteryBarDelegate.isEnabled()).thenReturn(true)

        mainServiceController.startCommand(0, 0)

        verify(batteryBarDelegate, times(1)).start()
    }


    @Test
    fun onStartCommand_disabled() {
        `when`(batteryBarDelegate.isEnabled()).thenReturn(false)

        mainServiceController.startCommand(0, 0)

        verify(batteryBarDelegate, never()).start()
    }

    @Test
    fun onDestroy() {
        mainServiceController.destroy()
        verify(batteryBarDelegate, times(1)).stop()
    }

    @Test
    fun onConfigurationChanged() {
        mainServiceController.get().onConfigurationChanged(null)
        verify(batteryBarDelegate, times(1)).stop()
        verify(batteryBarDelegate, times(1)).start()
    }

}