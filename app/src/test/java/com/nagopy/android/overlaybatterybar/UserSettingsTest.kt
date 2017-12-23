package com.nagopy.android.overlaybatterybar

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class UserSettingsTest {

    lateinit var app: App

    lateinit var userSettings: UserSettings

    lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application)
        sharedPreferences.edit().clear()
        userSettings = UserSettings(sharedPreferences)
    }

    @Test
    fun batteryBarEnabled() {
        assertThat(userSettings.isBatteryBarEnabled(), `is`(false))

        userSettings.setBatteryBarEnabled(true)
        assertThat(userSettings.isBatteryBarEnabled(), `is`(true))

        userSettings.setBatteryBarEnabled(false)
        assertThat(userSettings.isBatteryBarEnabled(), `is`(false))
    }

    @Test
    fun batteryBarWidth() {
        assertThat(userSettings.getBatteryBarWidth(), `is`(6))

        userSettings.setBatteryBarWidth(10)
        assertThat(userSettings.getBatteryBarWidth(), `is`(10))
    }

    @Test
    fun showOnStatusBar() {
        assertThat(userSettings.showOnStatusBar(), `is`(true))

        userSettings.setShowOnStatusBar(false)
        assertThat(userSettings.showOnStatusBar(), `is`(false))

        userSettings.setShowOnStatusBar(true)
        assertThat(userSettings.showOnStatusBar(), `is`(true))
    }

}