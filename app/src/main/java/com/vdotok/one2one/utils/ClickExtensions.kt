package com.vdotok.one2one.utils

import android.view.View
import java.util.concurrent.TimeUnit

fun View.disableDoubleClick() {
    isClickable = false
    postDelayed({ isClickable = true }, TimeUnit.SECONDS.toMillis(2))
}
fun View.disableDoubleClickForOneSec() {
    isClickable = false
    postDelayed({ isClickable = true }, TimeUnit.SECONDS.toMillis(1))
}

fun View.clicks(method:  () -> Unit) {
    setOnClickListener {
        method.invoke()
    }
}

fun View.performSingleClick(method: () -> Unit) {
    setOnClickListener {
        disableDoubleClick()
        method.invoke()
    }
}

fun View.performSingleClickShortDelay(method: () -> Unit) {
    setOnClickListener {
        disableDoubleClickForOneSec()
        method.invoke()
    }
}

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}