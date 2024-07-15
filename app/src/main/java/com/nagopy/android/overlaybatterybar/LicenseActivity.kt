package com.nagopy.android.overlaybatterybar

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity

class LicenseActivity : AppCompatActivity() {

    lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.loadUrl("file:///android_asset/licenses.html")
        setContentView(webView)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}