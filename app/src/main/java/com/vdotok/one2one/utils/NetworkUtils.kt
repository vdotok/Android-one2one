package com.vdotok.one2one.utils

import android.content.Context
import android.net.ConnectivityManager
import android.view.View
import com.vdotok.network.models.LoginResponse
import com.vdotok.one2one.extensions.showSnackBar
import com.vdotok.one2one.feature.dashBoard.activity.DashBoardActivity
import com.vdotok.one2one.network.HttpResponseCodes
import com.vdotok.one2one.prefs.Prefs


fun hasNetworkAvailable(context: Context): Boolean {
    val service = Context.CONNECTIVITY_SERVICE
    val manager = context.getSystemService(service) as ConnectivityManager?
    val network = manager?.activeNetworkInfo
    return (network != null)
}

fun isInternetAvailable(context: Context): Boolean {
    return ConnectivityStatus(context).isConnected()
}

fun handleLoginResponse(context: Context, response: LoginResponse, prefs: Prefs, view: View) {
    when(response.status) {
        HttpResponseCodes.SUCCESS.value -> {
            saveResponseToPrefs(prefs, response)
            context.startActivity(DashBoardActivity.createDashBoardActivity(context))
        }
        else -> {
            view.showSnackBar(response.message)
        }
    }
}
