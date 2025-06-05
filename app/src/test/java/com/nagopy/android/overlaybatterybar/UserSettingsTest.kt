package com.nagopy.android.overlaybatterybar

import android.content.SharedPreferences
import android.preference.PreferenceManager
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class UserSettingsTest {

    lateinit var app: App

    lateinit var userSettings: UserSettings

    lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
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

    @Test
    fun testBatteryChargeLimit() {
        assertThat(userSettings.getBatteryChargeLimit(), `is`(100))

        userSettings.setBatteryChargeLimit(50)
        assertThat(userSettings.getBatteryChargeLimit(), `is`(50))

        userSettings.setBatteryChargeLimit(0)
        assertThat(userSettings.getBatteryChargeLimit(), `is`(1))

        userSettings.setBatteryChargeLimit(101)
        assertThat(userSettings.getBatteryChargeLimit(), `is`(100))
    }

    @Test
    fun normalBatteryColor() {
        assertThat(userSettings.getNormalBatteryColor(), `is`(android.graphics.Color.WHITE))

        userSettings.setNormalBatteryColor(android.graphics.Color.BLUE)
        assertThat(userSettings.getNormalBatteryColor(), `is`(android.graphics.Color.BLUE))
    }

    @Test
    fun chargingBatteryColor() {
        assertThat(userSettings.getChargingBatteryColor(), `is`(android.graphics.Color.GREEN))

        userSettings.setChargingBatteryColor(android.graphics.Color.YELLOW)
        assertThat(userSettings.getChargingBatteryColor(), `is`(android.graphics.Color.YELLOW))
    }

    @Test
    fun chargingAnimationEnabled() {
        assertThat(userSettings.isChargingAnimationEnabled(), `is`(true))

        userSettings.setChargingAnimationEnabled(false)
        assertThat(userSettings.isChargingAnimationEnabled(), `is`(false))

        userSettings.setChargingAnimationEnabled(true)
        assertThat(userSettings.isChargingAnimationEnabled(), `is`(true))
    }

    @Test
    fun gradientColorEnabled() {
        assertThat(userSettings.isGradientColorEnabled(), `is`(false))

        userSettings.setGradientColorEnabled(true)
        assertThat(userSettings.isGradientColorEnabled(), `is`(true))

        userSettings.setGradientColorEnabled(false)
        assertThat(userSettings.isGradientColorEnabled(), `is`(false))
    }
    }

}