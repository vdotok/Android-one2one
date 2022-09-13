package com.vdotok.one2one

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.enums.MediaType


/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 1:21 PM in 2021
 */
class VdoTok : Application() {

    private lateinit var callClient: CallClient
    private lateinit var prefs :Prefs
    var mediaTypeCheck: MediaType? = null
    private var lifecycleEventObserver = LifecycleEventObserver { _, event ->
       when (event) {
          Lifecycle.Event.ON_RESUME -> {
              if (mediaTypeCheck == MediaType.VIDEO) {
                  callClient.recentSession()?.let {
                      callClient.resumeVideo(prefs.loginInfo?.refId.toString(), it)
                  }
              }
            }
           Lifecycle.Event.ON_PAUSE -> {
               if (mediaTypeCheck == MediaType.VIDEO) {
                   callClient.recentSession()?.let {
                       callClient.pauseVideo(prefs.loginInfo?.refId.toString(), it)
                   }
               }
              }
            else -> {}
         }
     }

   override fun onCreate() {
       super.onCreate()
       vdotok = this
       callClient = CallClient.getInstance(this)!!
       prefs = Prefs(this)
       ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleEventObserver)
   }

    companion object {
        private var vdotok: VdoTok? = null

        fun getVdotok(): VdoTok? {
            return vdotok
        }
    }
}