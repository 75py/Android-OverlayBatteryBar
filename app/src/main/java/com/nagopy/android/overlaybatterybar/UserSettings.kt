package com.nagopy.android.overlaybatterybar

import android.annotation.SuppressLint
import android.content.SharedPreferences

class UserSettings(val sharedPreferences: SharedPreferences) {

    fun isBatteryBarEnabled(): Boolean
            = sharedPreferences.getBoolean("isBatteryBarEnabled", false)

    @SuppressLint("ApplySharedPref")
    fun setBatteryBarEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("isBatteryBarEnabled", enabled).commit()
    }

    fun getBatteryBarWidth(): Int
            = sharedPreferences.getInt("batteryBarWidth", 6)

    @SuppressLint("ApplySharedPref")
    fun setBatteryBarWidth(newWidth: Int) {
        sharedPreferences.edit().putInt("batteryBarWidth", newWidth).commit()
    }

    fun showOnStatusBar(): Boolean
            = sharedPreferences.getBoolean("showOnStatusBar", true)

    @SuppressLint("ApplySharedPref")
    fun setShowOnStatusBar(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("showOnStatusBar", enabled).commit()
    }
}