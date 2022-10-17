package com.vdotok.one2one.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.vdotok.one2one.VdoTok

class OnClearFromRecentService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("ClearFromRecentService", "Service Started")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ClearFromRecentService", "Service Destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        Log.e("ClearFromRecentService", "END")
        val sessionList = ArrayList<String>().apply {
                add((application as VdoTok).callClient.recentSession().toString())
        }
        Log.e("ClearFromRecentService", "END$sessionList")
        (application as VdoTok).callClient.endCallSession(sessionList)
        (application as VdoTok).callClient.disConnectSocket()
        stopSelf()
    }
}