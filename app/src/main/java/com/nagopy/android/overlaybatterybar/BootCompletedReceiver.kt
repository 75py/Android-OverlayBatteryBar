package com.nagopy.android.overlaybatterybar

import android.content.Context
import android.content.Intent
import com.github.salomonbrys.kodein.android.KodeinBroadcastReceiver
import com.github.salomonbrys.kodein.instance

class BootCompletedReceiver : KodeinBroadcastReceiver() {

    val userSettings: UserSettings by instance()
    val serviceHandler: MainService.Handler by instance()

    override fun onBroadcastReceived(context: Context, intent: Intent) {
        if (userSettings.isBatteryBarEnabled()) {
            serviceHandler.startService()
        }
    }

}