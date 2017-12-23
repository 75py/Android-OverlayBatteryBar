package com.nagopy.android.overlaybatterybar

import android.content.Context
import android.os.Build
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class HandlerTest {

    lateinit var handler: MainService.Handler

    @Mock
    lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        handler = MainService.Handler(context)
    }

    @Config(sdk = [Build.VERSION_CODES.O])
    @Test
    fun startService_API26() {
        handler.startService()
        verify(context, times(1)).startForegroundService(any())
    }

    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    @Test
    fun startService_API25() {
        handler.startService()
        verify(context, times(1)).startService(any())
    }

}