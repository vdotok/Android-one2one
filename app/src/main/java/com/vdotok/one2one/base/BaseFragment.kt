package com.vdotok.one2one.base

import androidx.fragment.app.Fragment
import com.vdotok.one2one.callback.FragmentCallback
import com.vdotok.one2one.models.AcceptCallModel
import com.vdotok.streaming.models.CallParams
import org.webrtc.VideoTrack


/**
 * Created By: VdoTok
 * Date & Time: On 5/26/21 At 3:21 PM in 2021
 */
open class BaseFragment: Fragment(), FragmentCallback {

    override fun onStart() {
        BaseActivity.mListener = this
        super.onStart()
    }

    override fun onIncomingCall(model: AcceptCallModel) {}

    override fun onStartCalling() {}

    override fun outGoingCall(toPeer: String) {}

    override fun onRemoteStreamReceived(stream: VideoTrack, refId: String, sessionID: String) {}

    override fun onCameraStreamReceived(stream: VideoTrack) {}

    override fun onCallMissed() {}

    override fun onCallRejected() {}

    override fun endOngoingCall(sessionId: String) {}

    override fun onAudioVideoStateChanged(audioState: Int, videoState: Int) {}

    override fun onInternetConnectionLoss() {}

    override fun onAcceptIncomingCall(callParams: CallParams) {}

    override fun onCallNoAnswer() {}

    override fun onCallBusy() {}
}