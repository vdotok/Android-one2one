package com.vdotok.one2one.feature.dashBoard.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.vdotok.one2one.R
import com.vdotok.one2one.databinding.LayoutAudioFragmentBinding
import com.vdotok.one2one.extensions.hide
import com.vdotok.one2one.extensions.invisible
import com.vdotok.one2one.extensions.show
import com.vdotok.one2one.feature.dashBoard.ui.DashBoardActivity
import com.vdotok.one2one.fragments.CallMangerListenerFragment
import com.vdotok.one2one.interfaces.FragmentRefreshListener
import com.vdotok.one2one.models.UserModel
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.one2one.utils.ApplicationConstants.CALL_PARAMS
import com.vdotok.one2one.utils.TimeUtils.getTimeFromSeconds
import com.vdotok.one2one.utils.performSingleClick
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.models.CallParams
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import java.util.*
import java.util.concurrent.TimeUnit


class AudioVideoCallFragment : CallMangerListenerFragment(), FragmentRefreshListener {

    private var callParams: CallParams? = null
    private var isIncomingCall = false
    private lateinit var binding: LayoutAudioFragmentBinding

    private lateinit var prefs: Prefs

    private lateinit var callClient: CallClient
    private var userModel: UserModel? = null
    private var name: String? = null

    private var userName: ObservableField<String> = ObservableField<String>()
    private var isVideoCall = ObservableBoolean(false)

    private var isMuted = false
    private var isSpeakerOff = true
    private var isVideoResume = true

    private var callDuration = 0

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutAudioFragmentBinding.inflate(inflater, container, false)
        init()
        startTimer()
        return binding.root
    }


    private fun init() {
        binding.username = userName
        binding.isVideoCall = isVideoCall

        prefs = Prefs(activity)

        (activity as DashBoardActivity).mListener = this

        CallClient.getInstance(activity as Context)?.let {
            callClient = it
        }

        setArgumentsData()

        displayUi(isVideoCall.get())

        setListeners()
        addLocalCameraStream()
        addRemoteVideoStreamForEchoTesting()
//        addTouchEventListener()
        screenWidth = context?.resources?.displayMetrics?.widthPixels!!
        screenHeight = context?.resources?.displayMetrics?.heightPixels!!
    }

    private fun addRemoteVideoStreamForEchoTesting() {
        Handler(Looper.getMainLooper()).postDelayed({
            (activity as DashBoardActivity).remoteStream?.let { onRemoteStreamReceived(it, "", "") }
        }, TimeUnit.SECONDS.toMillis(1))
    }

    private fun addLocalCameraStream() {
        Handler(Looper.getMainLooper()).postDelayed({
            (activity as DashBoardActivity).localStream?.let { onCameraStreamReceived(it) }
        }, TimeUnit.SECONDS.toMillis(1))
    }

    private fun setArgumentsData() {
        arguments?.get(UserModel.TAG)?.let {
            isVideoCall.set(arguments?.getBoolean(AllUserListFragment.IS_VIDEO_CALL) ?: false)
            userModel = it as UserModel?
            isIncomingCall = arguments?.get("isIncoming") as Boolean
            name = userModel?.fullName


        } ?: kotlin.run {
            isVideoCall.set(arguments?.getBoolean(AllUserListFragment.IS_VIDEO_CALL) ?: false)
            name = (arguments?.get("userName") as CharSequence?).toString()
            callParams = arguments?.getParcelable(CALL_PARAMS) as CallParams?
            isIncomingCall = true
        }
    }

    private fun setListeners() {
        binding.ivEndCall.performSingleClick {
            (activity as DashBoardActivity).endCall()
            //endCall()
        }

        binding.ivMute.setOnClickListener { muteButtonAction() }

        binding.ivSpeaker.setOnClickListener { speakerButtonAction() }

        binding.ivCamSwitch.setOnClickListener { cameraSwitchAction() }

        binding.ivCameraOnOff.setOnClickListener { if (isVideoCall.get()) cameraOnOffAction() }
    }

    private fun cameraOnOffAction() {

        (activity as DashBoardActivity).activeSessionId?.let { sessionId ->

            isVideoResume = isVideoResume.not()

            when {
                isVideoResume -> {
                    binding.localViewCard.show()
                    binding.localView.show()
                    callClient.resumeVideo(
                        sessionKey = sessionId,
                        refId = prefs.loginInfo?.refId!!
                    )
                    binding.ivCameraOnOff.setImageResource(R.drawable.ic_call_video_rounded)
                }
                else -> {
                    binding.localViewCard.hide()
                    binding.localView.hide()
                    callClient.pauseVideo(
                        sessionKey = sessionId,
                        refId = prefs.loginInfo?.refId!!
                    )
                    binding.ivCameraOnOff.setImageResource(R.drawable.ic_video_off)
                }
            }
        }


    }

    private fun cameraSwitchAction() {
        (activity as DashBoardActivity).activeSessionId?.let {
            callClient.switchCamera(it)
        }
    }

    private fun speakerButtonAction() {
        isSpeakerOff = isSpeakerOff.not()
        when {
            isSpeakerOff -> binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_off)
            else -> binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_on)
        }

        callClient.toggleSpeakerOnOff()
    }

    private fun muteButtonAction() {
        isMuted = !isMuted

        when {
            isMuted -> binding.ivMute.setImageResource(R.drawable.ic_mute_mic1)
            else -> binding.ivMute.setImageResource(R.drawable.ic_unmute_mic)
        }

        (activity as DashBoardActivity).activeSessionId?.let { sessionId ->
            callClient.muteUnMuteMic(
                sessionKey = sessionId,
                refId = prefs.loginInfo?.refId!!
            )
        }
    }


    private var screenWidth = 0
    private var screenHeight = 0

    private var rightDX = 0
    private var rightDY = 0

    private var xPoint = 0.0f
    private var yPoint = 0.0f
    private val THRESHOLD_VALUE = 70.0f

    @SuppressLint("ClickableViewAccessibility")
    private fun addTouchEventListener() {
        Handler(Looper.getMainLooper()).postDelayed({
            binding.localViewCard.setOnTouchListener(View.OnTouchListener { view, event ->
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> {
                        rightDX = (binding.localViewCard.x - event.rawX).toInt()
                        rightDY = (binding.localViewCard.y - event.rawY).toInt()
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val displacementX = event.rawX + rightDX
                        val displacementY = event.rawY + rightDY

                        binding.localViewCard.animate()
                            .x(displacementX)
                            .y(displacementY)
                            .setDuration(0)
                            .start()

                        Handler(Looper.getMainLooper()).postDelayed({

                            xPoint = view.x + view.width
                            yPoint = view.y + view.height

                            when {
                                xPoint > screenWidth / 2 && yPoint < screenHeight / 2 -> {
                                    //First Quadrant
                                    animateView(
                                        ((screenWidth - (view.width + THRESHOLD_VALUE))),
                                        (screenHeight / 2 - (view.height + THRESHOLD_VALUE))
                                    )
                                }
                                xPoint < screenWidth / 2 && yPoint < screenHeight / 2 -> {
                                    //Second Quadrant
                                    animateView(
                                        THRESHOLD_VALUE,
                                        (screenHeight / 2 - (view.height + THRESHOLD_VALUE))
                                    )
                                }
                                xPoint < screenWidth / 2 && yPoint > screenHeight / 2 -> {
                                    //Third Quadrant
                                    animateView(
                                        THRESHOLD_VALUE,
                                        (screenHeight / 2 + view.height / 2).toFloat()
                                    )
                                }
                                else -> {
                                    //Fourth Quadrant
                                    animateView(
                                        ((screenWidth - (view.width + THRESHOLD_VALUE))),
                                        (screenHeight / 2 + view.height / 2).toFloat()
                                    )
                                }
                            }

                        }, 100)

                    }
                    else -> { // Note the block
                        return@OnTouchListener false
                    }
                }
                true
            })
        }, 1500)
    }


    private fun animateView(xPoint: Float, yPoint: Float) {
        binding.localViewCard.animate()
            .x(xPoint)
            .y(yPoint)
            .setDuration(200)
            .start()
    }

    private fun endCall() {
        stopTimer()
        resetCallData()
        Navigation.findNavController(binding.root).navigateUp()
//        Navigation.findNavController(binding.root).navigate(R.id.action_open_userList)
    }

    private fun resetCallData() {
//        callClient.speakerOff()
    }

    private fun displayUi(videoCall: Boolean) {
        userName.set(name)
        if (!videoCall) {
            binding.tvCallType.text = getString(R.string.audio_calling)
            binding.ivCameraOnOff.setImageResource(R.drawable.ic_video_off)
            binding.remoteView.invisible()
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
        resetCallData()
        (activity as DashBoardActivity).endCall()
    }

    var isInit = false

    override fun onRemoteStreamReceived(stream: VideoTrack, refId: String, sessionID: String) {
        if (isInit)
            return

        Log.e("one-one", "onRemoteStreamReceived: stream: "+ stream.toString())

        isInit = true
        val mainHandler = activity?.mainLooper?.let { Handler(it) }
        val myRunnable = Runnable {

            try {
                val videoView = binding.remoteView
                videoView.setMirror(true)
                videoView.init(EglBase.create().eglBaseContext, null)
                stream.addSink(videoView)
                videoView.setZOrderMediaOverlay(false)
                videoView.setZOrderOnTop(false)

                refreshLocalCameraView()

                videoView.postDelayed({
                    isSpeakerOff = false
                    callClient.toggleSpeakerOnOff()
                }, 1000)
                binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_on)

            } catch (e: Exception) {
                Log.i("SocketLog", "onStreamAvailable: exception" + e.printStackTrace())
            }

        }
        mainHandler?.post(myRunnable)
    }

    override fun onCameraStreamReceived(stream: VideoTrack) {

        Log.e("one-one", "onRemoteStreamReceived: onCameraStreamReceived: "+ stream.toString())

        val mainHandler = activity?.let { Handler(it.mainLooper) }
        val myRunnable = Runnable {
            binding.remoteView.setZOrderMediaOverlay(false)

            val videoView = binding.localView
            videoView.setMirror(true)
            videoView.init(EglBase.create().eglBaseContext, null)
            videoView.setEnableHardwareScaler(true)
            videoView.setZOrderMediaOverlay(true)
            stream.addSink(videoView)
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
                binding.imgUserPhoto.hide()
                binding.remoteView.setZOrderMediaOverlay(false)
                binding.remoteView.setZOrderOnTop(false)

                binding.localView.setZOrderMediaOverlay(true)
                binding.localView.setZOrderOnTop(true)

                refreshLocalCameraView()
                binding.remoteView.show()

            } else {
                binding.imgUserPhoto.show()
                binding.remoteView.invisible()
            }
        }
    }


    private fun refreshLocalCameraView() {
        if (binding.localView.isVisible) {
            binding.localView.hide()
            binding.remoteView.setZOrderMediaOverlay(false)
            binding.localView.setZOrderMediaOverlay(true)
            binding.localView.setEnableHardwareScaler(true)
            binding.localView.requestFocus()
            binding.localView.show()
            animateView(
                    ((screenWidth - (binding.localView.width + THRESHOLD_VALUE))),
                    (screenHeight / 2 + binding.localView.height / 2).toFloat()
            )
//            ViewCompat.setElevation(binding.remoteView, -1f)
            ViewCompat.setElevation(binding.localViewCard, 11f)
            ViewCompat.setElevation(binding.localView, 11f)
        }
    }

    override fun onInternetConnectionLoss() {
        activity?.runOnUiThread { endCall() }
    }

    override fun onAcceptIncomingCall(callParams: CallParams) {
    }


}