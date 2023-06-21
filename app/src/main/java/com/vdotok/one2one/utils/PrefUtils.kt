package com.vdotok.one2one.utils

import com.vdotok.network.models.LoginResponse
import com.vdotok.one2one.prefs.Prefs

fun saveResponseToPrefs(prefs: Prefs, response: LoginResponse?) {
//    response?.mediaServer?.host = "r-stun2.vdotok.dev"
    prefs.loginInfo = response
}
