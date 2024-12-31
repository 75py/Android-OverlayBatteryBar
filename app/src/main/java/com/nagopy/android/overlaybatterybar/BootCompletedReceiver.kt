package com.nagopy.android.overlaybatterybar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class BootCompletedReceiver : BroadcastReceiver(), DIAware {

    override lateinit var di: DI

    val userSettings: UserSettings by instance()
    val serviceHandler: MainService.Handler by instance()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        di = context.asApp().di

        execute(userSettings, serviceHandler)
    }

    fun execute(userSettings: UserSettings, serviceHandler: MainService.Handler) {
        if (userSettings.isBatteryBarEnabled()) {
            serviceHandler.startService()
        }
    }
}