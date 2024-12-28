package com.nagopy.android.overlaybatterybar

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.SeekBarBindingAdapter
import org.kodein.di.android.closestDI
import org.kodein.di.instance
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.nagopy.android.overlaybatterybar.databinding.ActivityMainBinding
import com.nagopy.android.overlayviewmanager.OverlayViewManager
import org.kodein.di.DIAware
import timber.log.Timber


class MainActivity : AppCompatActivity(), SeekBarBindingAdapter.OnProgressChanged, DIAware {

    override val di by closestDI()

    val overlayViewManager: OverlayViewManager by instance()
    val userSettings: UserSettings by instance()
    val serviceHandler: MainService.Handler by instance()

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.canDrawOverlays = overlayViewManager.canDrawOverlays()
        binding.onProgressChanged = this
        binding.isBatteryBarEnabled = userSettings.isBatteryBarEnabled()
        binding.batteryBarWidth = userSettings.getBatteryBarWidth()
        binding.showOnStatusBar = userSettings.showOnStatusBar()

        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        binding.statusBarHeight =
                if (resourceId > 0) {
                    resources.getDimensionPixelSize(resourceId)
                } else {
                    32
                }

        serviceHandler.startService()

        if (savedInstanceState == null) {
            if (!overlayViewManager.canDrawOverlays()) {
                overlayViewManager.showPermissionRequestDialog(supportFragmentManager, R.string.app_name);
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("canDrawOverlays:%s", overlayViewManager.canDrawOverlays())
        binding.canDrawOverlays = overlayViewManager.canDrawOverlays()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.button_request_overlay_permisson -> overlayViewManager.requestOverlayPermission()
            R.id.switch_battery_bar -> switchBatteryBar((view as CompoundButton).isChecked)
            R.id.checkbox_show_on_status_bar -> switchShowOnStatusBar((view as CompoundButton).isChecked)
        }
    }

    fun switchBatteryBar(enabled: Boolean) {
        Timber.d("switchBatteryBar %s", enabled)
        userSettings.setBatteryBarEnabled(enabled)
        binding.isBatteryBarEnabled = enabled
        serviceHandler.startService()
    }

    fun switchShowOnStatusBar(enabled: Boolean) {
        Timber.d("switchShowOnStatusBar %s", enabled)
        userSettings.setShowOnStatusBar(enabled)
        binding.showOnStatusBar = enabled
        serviceHandler.startService()
    }

    @SuppressLint("RestrictedApi")
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar?.id) {
            R.id.seekbar_battery_bar_width -> {
                userSettings.setBatteryBarWidth(progress)
                binding.batteryBarWidth = progress
                serviceHandler.startService()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_license -> {
                startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
