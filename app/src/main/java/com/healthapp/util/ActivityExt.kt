package com.healthapp.util

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View


fun Activity.setTransparentStatusBar() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.statusBarColor = Color.TRANSPARENT
    }
}
