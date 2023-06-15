package com.vdotok.one2one

import android.app.Application
import androidx.lifecycle.*
import com.vdotok.network.utils.Constants.BASE_URL
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.one2one.utils.ApplicationConstants.SDK_PROJECT_ID
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.enums.MediaType
import org.webrtc.EglBase

/**
 * Created By: VdoTok
 * Date & Time: On 10/11/2021 At 1:21 PM in 2021
 */
class VdoTok : Application() {

    lateinit var callClient: CallClient
    private lateinit var prefs :Prefs
    var mediaTypeCheck: MediaType? = null
    var camView: Boolean = true
    private val rootEglBase: EglBase = EglBase.create()
    val rootEglBaseContext: EglBase.Context = rootEglBase.eglBaseContext
    private var lifecycleEventObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                if (mediaTypeCheck == MediaType.VIDEO) {
                    if (camView) {
                        callClient.recentSession()?.let {
                            callClient.resumeVideo(prefs.loginInfo?.refId.toString(), it)
                        }
                    }else{
                        callClient.recentSession()?.let {
                            callClient.pauseVideo(prefs.loginInfo?.refId.toString(), it)
                        }
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
            Lifecycle.Event.ON_DESTROY -> {
                rootEglBase.release()
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
        setVariables()
    }

    private fun setVariables() {
        if (!prefs.userProjectId.isNullOrEmpty() && !prefs.userBaseUrl.isNullOrEmpty()) {
            BASE_URL = prefs.userBaseUrl.toString()
            SDK_PROJECT_ID = prefs.userProjectId.toString()
        }
    }

    companion object {
        private var vdotok: VdoTok? = null

        fun getVdotok(): VdoTok? {
            return vdotok
        }
    }
}