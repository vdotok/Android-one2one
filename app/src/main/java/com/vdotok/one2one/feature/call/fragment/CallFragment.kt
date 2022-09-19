package com.vdotok.one2one.feature.call.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import com.vdotok.network.models.UserModel
import com.vdotok.one2one.R
import com.vdotok.one2one.VdoTok
import com.vdotok.one2one.base.BaseActivity
import com.vdotok.one2one.base.BaseFragment
import com.vdotok.one2one.callback.FragmentCallback
import com.vdotok.one2one.databinding.CallFragmentBinding
import com.vdotok.one2one.extensions.hide
import com.vdotok.one2one.extensions.invisible
import com.vdotok.one2one.feature.call.activity.CallActivity
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.one2one.utils.ApplicationConstants.CALL_PARAMS
import com.vdotok.one2one.utils.TimeUtils.getTimeFromSeconds
import com.vdotok.one2one.utils.performSingleClick
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.models.CallParams
import com.vdotok.streaming.views.CallViewRenderer
import org.webrtc.VideoTrack
import java.util.*
import java.util.concurrent.TimeUnit


class CallFragment : BaseFragment(), FragmentCallback {

    private var callParams: CallParams? = null
    private var isIncomingCall = false
    private var isFragmentOpen = true
    private lateinit var binding: CallFragmentBinding

    private lateinit var prefs: Prefs

    private lateinit var callClient: CallClient
    private var userModel: UserModel? = null

    private var userName: ObservableField<String> = ObservableField<String>()
    private var isVideoCall = ObservableBoolean(false)

    private var isMuted = false
    private var isSpeakerOff = true
    private var isVideoResume = true

    private var callDuration = 0

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    private lateinit var ownViewReference: CallViewRenderer
    private lateinit var remoteViewReference: CallViewRenderer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CallFragmentBinding.inflate(inflater, container, false)
        init()
        startTimer()
        return binding.root
    }


    private fun init() {
        binding.username = userName
        binding.isVideoCall = isVideoCall
        ownViewReference = binding.localView
        remoteViewReference = binding.remoteView

        prefs = Prefs(activity)

        BaseActivity.mListener = this

        CallClient.getInstance(activity as Context)?.let {
            callClient = it
        }

        setArgumentsData()
        displayUi(isVideoCall.get())
        setListeners()
        addLocalCameraStream()
        addRemoteVideoStreamForEchoTesting()
    }

    private fun addRemoteVideoStreamForEchoTesting() {
        Handler(Looper.getMainLooper()).postDelayed({
            BaseActivity.remoteStream?.let { onRemoteStreamReceived(it, "", "") }
        }, TimeUnit.SECONDS.toMillis(1))
    }

    private fun addLocalCameraStream() {
        Handler(Looper.getMainLooper()).postDelayed({
            BaseActivity.localStream?.let { onCameraStreamReceived(it) }
        }, TimeUnit.SECONDS.toMillis(1))
    }

    private fun setArgumentsData() {
        isVideoCall.set(arguments?.getBoolean(CallActivity.VIDEO_CALL) ?: false)
        isIncomingCall =
            if (arguments?.containsKey(CallActivity.IN_COMING_CALL) == true) arguments?.get(
                CallActivity.IN_COMING_CALL
            ) as Boolean else false

        userModel = arguments?.get(UserModel.TAG) as UserModel?

        arguments?.get(CALL_PARAMS)?.let {
            callParams = arguments?.getParcelable(CALL_PARAMS) as CallParams?
            isIncomingCall = true
        } ?: kotlin.run {
        }
    }

    private fun setListeners() {
        binding.ivEndCall.performSingleClick {
            (activity as CallActivity).endCall()
        }

        binding.ivMute.setOnClickListener { muteButtonAction() }

        binding.ivSpeaker.setOnClickListener { speakerButtonAction() }

        binding.ivCamSwitch.setOnClickListener { cameraSwitchAction() }

        binding.ivCameraOnOff.setOnClickListener { if (isVideoCall.get()) cameraOnOffToggle() }

        ownViewReference.setOnClickListener {
            activity?.let { activity ->
                ownViewReference.swapViews(
                    activity,
                    remoteViewReference,
                    ownViewReference
                )
            }
        }

    }

    private fun cameraOnOffToggle() {

        (activity as CallActivity).activeSessionId?.let { sessionId ->
            isVideoResume = isVideoResume.not()
            if (isVideoResume) {
                //resume video
                callClient.resumeVideo(
                    sessionKey = sessionId,
                    refId = prefs.loginInfo?.refId!!
                )
                binding.ivCameraOnOff.setImageResource(R.drawable.ic_call_video_rounded)
                ownViewReference.showHideAvatar(false)
                (activity?.application as VdoTok).camView = true

            } else {
                //pause video
                ownViewReference.showHideAvatar(true)
                callClient.pauseVideo(
                    sessionKey = sessionId,
                    refId = prefs.loginInfo?.refId!!
                )
                binding.ivCameraOnOff.setImageResource(R.drawable.ic_video_off)
                (activity?.application as VdoTok).camView = false
            }
        }
    }

    private fun cameraSwitchAction() {
        (activity as CallActivity).activeSessionId?.let {
            callClient.switchCamera(it)
        }
    }

    private fun speakerButtonAction() {
        isSpeakerOff = isSpeakerOff.not()
        when {
            isSpeakerOff -> binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_off)
            else -> binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_on)
        }

        if (callClient.isSpeakerEnabled()) {
           callClient.setSpeakerEnable(false)
        } else {
            callClient.setSpeakerEnable(true)
        }
    }

    private fun muteButtonAction() {
        isMuted = !isMuted

        when {
            isMuted -> binding.ivMute.setImageResource(R.drawable.ic_mute_mic1)
            else -> binding.ivMute.setImageResource(R.drawable.ic_unmute_mic)
        }

        (activity as CallActivity).activeSessionId?.let { sessionId ->
            callClient.muteUnMuteMic(
                sessionKey = sessionId,
                refId = prefs.loginInfo?.refId!!
            )
        }
    }

    private fun endCall() {
        stopTimer()
        this.requireActivity().finish()
    }

    private fun displayUi(videoCall: Boolean) {
        userName.set(userModel?.userName)

        if (!videoCall) {
            binding.tvCallType.text = getString(R.string.audio_calling)
            binding.ivCameraOnOff.setImageResource(R.drawable.ic_video_off)
            binding.remoteView.invisible()
            binding.localView.hide()
            binding.ivCamSwitch.hide()
        } else {
            binding.tvCallType.text = getString(R.string.video_calling)
            binding.imgUserPhoto.hide()
        }

    }

    private fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    callDuration = callDuration.plus(1)
                    binding.tvTime.text = getTimeFromSeconds(callDuration)
                }
            }
        }
        timer?.scheduleAtFixedRate(timerTask, 1000, 1000)
    }

    private fun stopTimer() {
        callDuration = 0
        timerTask?.cancel()
        timer?.purge()
        timer?.cancel()
        timer = null
        binding.tvTime.text = getTimeFromSeconds(callDuration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopTimer()
        (activity as CallActivity).endCall()
    }

    override fun onRemoteStreamReceived(stream: VideoTrack, refId: String, sessionID: String) {
        val mainHandler = activity?.mainLooper?.let { Handler(it) }
        val myRunnable = Runnable {

            try {
                stream.addSink(binding.remoteView.setView())
                remoteViewReference.getPreview().setMirror(false)
                binding.remoteView.postDelayed({
                    isSpeakerOff = false
                    callClient.setSpeakerEnable(true)
                }, 1000)
                binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_on)

            } catch (e: Exception) {
                Log.i("SocketLog", "onStreamAvailable: exception" + e.printStackTrace())
            }

        }
        mainHandler?.post(myRunnable)
    }

    override fun onCameraStreamReceived(stream: VideoTrack) {

        val mainHandler = activity?.let { Handler(it.mainLooper) }
        val myRunnable = Runnable {
            stream.addSink(binding.localView.setView())
            ownViewReference.getPreview().setMirror(false)
        }
        mainHandler?.post(myRunnable)
    }

    override fun endOngoingCall(sessionId: String) {
        activity?.runOnUiThread {
            endCall()
        }
    }

    override fun onAudioVideoStateChanged(audioState: Int, videoState: Int) {
        activity?.runOnUiThread {
            if (videoState == 1) {
                remoteViewReference.showHideAvatar(false)
            } else {
                remoteViewReference.showHideAvatar(true)
            }
        }
    }

    override fun onInsufficientBalance() {
        closeFragmentWithMessage("Insufficient Balance!")
    }

    override fun onInternetConnectionLoss() {
        activity?.runOnUiThread { endCall() }
    }

    override fun onAcceptIncomingCall(callParams: CallParams) {
    }

    private fun closeFragmentWithMessage(message: String){
        if(isFragmentOpen) {
            activity?.runOnUiThread {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                activity?.onBackPressed()
            }
            isFragmentOpen = false
        }
    }
}