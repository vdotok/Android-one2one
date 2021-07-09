package com.norgic.vdotokcall.fragments

import androidx.fragment.app.Fragment
import com.norgic.callsdks.models.CallParams
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity
import com.norgic.vdotokcall.interfaces.FragmentRefreshListener
import com.norgic.vdotokcall.models.AcceptCallModel
import org.webrtc.VideoTrack


/**
 * Created By: Norgic
 * Date & Time: On 5/26/21 At 3:21 PM in 2021
 */
open class CallMangerListenerFragment: Fragment(), FragmentRefreshListener {

    override fun onStart() {
        (activity as DashBoardActivity).mListener = this
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