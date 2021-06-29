package com.norgic.vdotokcall.interfaces

import com.norgic.vdotokcall.models.AcceptCallModel
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
    fun onCallRejected(reason: String)
    fun endOngoingCall(sessionId: String)
    fun onAudioVideoStateChanged(audioState: Int, videoState: Int)
    fun onInternetConnectionLoss()
}