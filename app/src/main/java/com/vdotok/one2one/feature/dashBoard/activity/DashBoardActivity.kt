package com.vdotok.one2one.feature.dashBoard.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.vdotok.one2one.R
import com.vdotok.one2one.VdoTok
import com.vdotok.one2one.base.BaseActivity
import com.vdotok.one2one.databinding.ActivityDashBoardBinding
import com.vdotok.one2one.utils.ApplicationConstants
import com.vdotok.streaming.enums.CallType
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import com.vdotok.streaming.models.CallParams


class DashBoardActivity : BaseActivity() {

    private lateinit var binding: ActivityDashBoardBinding
    override fun getRootView() = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dash_board)
        askForPermissions()
        addInternetConnectionObserver()
    }

    private fun askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                ApplicationConstants.MY_PERMISSIONS_REQUEST
            )
        } else if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO),
                ApplicationConstants.MY_PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                ApplicationConstants.MY_PERMISSIONS_REQUEST_CAMERA
            )
        }
    }



    companion object {

        fun createDashBoardActivity(context: Context) = Intent(
            context,
            DashBoardActivity::class.java
        ).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }

    //    while dialing one2one call we need to save the sessionID based on the receiver's refID
    fun dialOneToOneCall(mediaType: MediaType, refIdUser: String) {
        (application as VdoTok).mediaTypeCheck = mediaType
        prefs.loginInfo?.let {
            it.mcToken?.let { mcToken ->
                activeSessionId = callClient.dialOne2OneCall(
                    callParams = CallParams(
                        refId = it.refId!!,
                        toRefIds = arrayListOf(refIdUser),
                        mcToken = mcToken,
                        mediaType = mediaType,
                        callType = CallType.ONE_TO_ONE,
                        sessionType = SessionType.CALL
                    )
                )
            } ?: kotlin.run {
                val snackbar: Snackbar = Snackbar
                    .make(
                        binding.root,
                        "Client is not connected! Please try reconnecting client",
                        Snackbar.LENGTH_LONG
                    )
                    .setAction("RECONNECT") { connectClient() }
                snackbar.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        internetConnectionRestored = false
    }

    override fun sessionReconnecting(sessionID: String) {
//        TODO("Not yet implemented")
    }

    override fun onStart() {
        super.onStart()
        internetConnectionRestored = true
    }

    override fun onResume() {
        super.onResume()
        internetConnectionRestored = true
    }

    // when socket is disconnected
    override fun onClose(reason: String) {
        connectClient()
    }

    override fun incomingCall(callParams: CallParams) {
        callMissed = false
        (application as VdoTok).mediaTypeCheck = callParams.mediaType
        if (activeSessionId?.let { callClient.getActiveSessionClient(it) != null } == true) {
            callClient.sessionBusy(prefs.loginInfo?.refId!!, callParams.sessionUuid)
            return
        }
        mListener?.onAcceptIncomingCall(callParams)
    }


}