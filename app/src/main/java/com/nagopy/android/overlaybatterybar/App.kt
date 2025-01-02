package com.nagopy.android.overlaybatterybar

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.preference.PreferenceManager
import android.view.WindowManager
import com.nagopy.android.overlayviewmanager.OverlayViewManager
import org.kodein.di.*
import timber.log.Timber

class App : Application(), DIAware {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        OverlayViewManager.init(this)
    }

    override var di = DI {
        bind<Context>() with singleton { this@App }
        bind<Application>() with singleton { this@App }
        bind<OverlayViewManager>() with singleton { OverlayViewManager.getInstance() }
        bind<SharedPreferences>() with singleton { PreferenceManager.getDefaultSharedPreferences(instance()) }
        bind<PowerManager>() with singleton { instance<Application>().getSystemService(POWER_SERVICE) as PowerManager }
        bind<NotificationManager>() with singleton { instance<Context>().getSystemService(NOTIFICATION_SERVICE) as NotificationManager }
        bind<Handler>() with singleton { Handler(Looper.getMainLooper()) }
        bind<BatteryManager>() with singleton { instance<Context>().getSystemService(BATTERY_SERVICE) as BatteryManager }
        bind<UserSettings>() with singleton {
            UserSettings(instance())
        }
        bind<MainService.Handler>() with singleton { MainService.Handler(instance()) }
        bind<BatteryBarDelegate>() with singleton {
            BatteryBarDelegate(instance(), instance(), instance(), instance(), instance(), instance())
        }
        bind<WindowManager>() with singleton { instance<Context>().getSystemService(WINDOW_SERVICE) as WindowManager }
        bind<DisplayManager>() with singleton { instance<Context>().getSystemService(DISPLAY_SERVICE) as DisplayManager }
        bind<DisplaySize>() with singleton {
            DisplaySize(instance(), instance())
        }
    }
}

fun Context.asApp() = this.applicationContext as App
