package com.nagopy.android.overlaybatterybar

import android.widget.Switch
import com.github.salomonbrys.kodein.Kodein
import com.github.salomonbrys.kodein.bind
import com.github.salomonbrys.kodein.singleton
import com.nagopy.android.overlayviewmanager.OverlayViewManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    lateinit var app: App

    lateinit var mainActivityController: ActivityController<MainActivity>

    @Mock
    lateinit var overlayViewManager: OverlayViewManager

    @Mock
    lateinit var userSettings: UserSettings

    @Mock
    lateinit var serviceHandler: MainService.Handler

    @Mock
    lateinit var switchView: Switch

    val testModule = Kodein.Module(allowSilentOverride = true) {
        bind<OverlayViewManager>() with singleton { overlayViewManager }
        bind<UserSettings>() with singleton { userSettings }
        bind<MainService.Handler>() with singleton { serviceHandler }
    }

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        app = RuntimeEnvironment.application.asApp()
        app.kodein.addImport(testModule, true)

        mainActivityController = Robolectric.buildActivity(MainActivity::class.java).create()
    }

    @Test
    fun onCreate() {
        verify(serviceHandler, times(1)).startService()
    }

    @Test
    fun onClick() {
        verify(serviceHandler, times(1)).startService()

        `when`(switchView.id).thenReturn(R.id.switch_battery_bar)
        `when`(switchView.isChecked).thenReturn(true)

        mainActivityController.get().onClick(switchView)

        verify(userSettings, times(1)).setBatteryBarEnabled(true)
        verify(serviceHandler, times(2)).startService()
    }

}