package com.nagopy.android.overlaybatterybar

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.preference.PreferenceManager
import com.github.salomonbrys.kodein.*
import com.github.salomonbrys.kodein.android.androidServiceScope
import com.github.salomonbrys.kodein.conf.ConfigurableKodein
import com.nagopy.android.overlayviewmanager.OverlayViewManager
import timber.log.Timber

class App : Application(), KodeinAware {

    override fun onCreate() {
        super.onCreate()

        resetInjection()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        OverlayViewManager.init(this)
    }

    override val kodein = ConfigurableKodein(mutable = true)

    fun resetInjection() {
        kodein.clear()
        kodein.addImport(appDependencies(), true)
    }

    private fun appDependencies(): Kodein.Module {
        return Kodein.Module(allowSilentOverride = true) {
            bind<OverlayViewManager>() with singleton { OverlayViewManager.getInstance() }
            bind<SharedPreferences>() with singleton { PreferenceManager.getDefaultSharedPreferences(this@App) }
            bind<PowerManager>() with singleton { getSystemService(Context.POWER_SERVICE) as PowerManager }
            bind<NotificationManager>() with singleton { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
            bind<Handler>() with singleton { Handler(Looper.getMainLooper()) }
            bind<UserSettings>() with singleton {
                UserSettings(instance())
            }
            bind<MainService.Handler>() with singleton { MainService.Handler(instance()) }

            bind<BatteryBarDelegate>() with scopedSingleton(androidServiceScope) {
                BatteryBarDelegate(instance(), instance(), instance(), instance())
            }
        }
    }

}

fun Context.asApp() = this.applicationContext as App
