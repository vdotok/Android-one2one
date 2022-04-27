package com.vdotok.one2one.feature.call.activity

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.vdotok.one2one.R
import com.vdotok.one2one.base.BaseActivity
import com.vdotok.one2one.databinding.ActivityCallBinding
import com.vdotok.one2one.models.AcceptCallModel
import com.vdotok.network.models.UserModel
import com.vdotok.one2one.utils.ApplicationConstants
import com.vdotok.streaming.models.CallParams


class CallActivity : BaseActivity() {

    private lateinit var binding: ActivityCallBinding
    override fun getRootView() = binding.root

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_call)
        addInternetConnectionObserver()

        activeSessionId = intent.extras?.getString(Active_Session_ID)

        findNavController(R.id.nav_host_fragment)
            .setGraph(
                R.navigation.call_navigation,
                intent.extras
            )
    }

    fun acceptIncomingCall(callParams: CallParams) {
        prefs.loginInfo?.let {
            activeSessionId = callClient.acceptIncomingCall(
                it.refId!!, callParams
            )
        }
    }

    fun endCall() {
        turnSpeakerOff()
        localStream = null
        remoteStream = null
        activeSessionId?.let {
            callClient.endCallSession(arrayListOf(it))
        }
        finish()
    }

    override fun incomingCall(callParams: CallParams) {
        if (activeSessionId?.let { callClient.getActiveSessionClient(it) != null } == true) {
            callClient.sessionBusy(prefs.loginInfo?.refId!!, callParams.sessionUUID)
            return
        }
        mListener?.onAcceptIncomingCall(callParams)
    }

    override fun multiSessionCreated(sessionIds: Pair<String, String>) {

    }

    companion object {

        const val VIDEO_CALL = "video_call"
        const val IN_COMING_CALL = "incoming_call"
        const val Active_Session_ID = "active_session_id"

        fun createIntent(context: Context, userReceiver: UserModel?, isVideo: Boolean, isIncoming: Boolean, model: AcceptCallModel?,
                         callParams: CallParams?, activeSessionId: String?)  = Intent(
            context,
            CallActivity::class.java
        ).apply {
            this.putExtras(Bundle().apply {
                putParcelable(UserModel.TAG, userReceiver)
                putBoolean(VIDEO_CALL, isVideo)
                putParcelable(AcceptCallModel.TAG, model)
                putBoolean(IN_COMING_CALL, isIncoming)

                callParams?.let {
                    putParcelable(ApplicationConstants.CALL_PARAMS, it)
                    putBoolean(IN_COMING_CALL, true)
                }
                putString(Active_Session_ID, activeSessionId)
            })
        }
    }

}