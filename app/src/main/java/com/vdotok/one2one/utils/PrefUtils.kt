package com.vdotok.one2one.utils

import com.vdotok.network.models.LoginResponse
import com.vdotok.one2one.prefs.Prefs

fun saveResponseToPrefs(prefs: Prefs, response: LoginResponse?) {
    prefs.loginInfo = response
}
