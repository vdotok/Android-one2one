package com.vdotok.one2one.interfaces


import com.vdotok.one2one.models.AcceptCallModel
import com.vdotok.streaming.models.CallParams
import org.webrtc.VideoTrack

/**
 * Interface that are to be implemented in order provide callbacks to fragments
 * */
interface FragmentRefreshListener {
    fun onIncomingCall(model: AcceptCallModel)
    fun onStartCalling()
    fun outGoingCall(toPeer : String)
    fun onRemoteStreamReceived(stream: VideoTrack, refId: String, sessionID: String)
    fun onCameraStreamReceived(stream: VideoTrack)
    fun onCallMissed()
    fun onCallRejected()
    fun endOngoingCall(sessionId: String)
    fun onAudioVideoStateChanged(audioState: Int, videoState: Int)
    fun onInternetConnectionLoss()
    fun onAcceptIncomingCall(callParams: CallParams)
    fun onConnectionSuccess() {}
    fun onConnectionFail() {}
    fun onCallNoAnswer()
    fun onCallBusy()
    fun onCallTimeout() {}
}