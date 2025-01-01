package com.nagopy.android.overlaybatterybar

import android.graphics.Insets
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.RoundedCorner
import android.view.WindowInsets
import android.view.WindowManager
import android.view.WindowMetrics
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class DisplaySizeTest {

    @Mock
    lateinit var windowManager: WindowManager
    @Mock
    lateinit var displayManager: DisplayManager

    lateinit var displaySize: DisplaySize

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        displaySize = DisplaySize(windowManager, displayManager)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun test_getRoundedCornerApi30() {
        assertThat(displaySize.getRoundedCorner(), `is`(0))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.S])
    fun test_getRoundedCornerApi31() {
        val display = mock(Display::class.java)
        val roundedCorner = mock(RoundedCorner::class.java)
        `when`(displayManager.getDisplay(Display.DEFAULT_DISPLAY)).thenReturn(display)
        `when`(display.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT)).thenReturn(roundedCorner)
        `when`(roundedCorner.radius).thenReturn(10)
        assertThat(displaySize.getRoundedCorner(), `is`(10))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.R])
    fun test_getDisplayWidthApi30() {
        val metrics = mock(WindowMetrics::class.java)
        `when`(windowManager.currentWindowMetrics).thenReturn(metrics)
        val windowInsets = mock(WindowInsets::class.java)
        `when`(metrics.windowInsets).thenReturn(windowInsets)
        val bounds = mock(Rect::class.java)
        `when`(metrics.bounds).thenReturn(bounds)
        `when`(bounds.width()).thenReturn(1000)
        assertThat(displaySize.getDisplayWidth(), `is`(1000))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.Q])
    fun test_getDisplayWidthApi29() {
        val display = mock(Display::class.java)
        `when`(windowManager.defaultDisplay).thenReturn(display)
        `when`(display.getMetrics(any())).thenAnswer {
            val displayMetrics = it.arguments[0] as DisplayMetrics
            displayMetrics.widthPixels = 100
        }
        assertThat(displaySize.getDisplayWidth(), `is`(100))
        verify(display, times(1)).getMetrics(any())
    }
}