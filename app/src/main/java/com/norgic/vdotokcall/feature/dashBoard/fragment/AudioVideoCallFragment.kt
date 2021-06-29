package com.norgic.vdotokcall.feature.dashBoard.fragment

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
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.LayoutAudioFragmentBinding
import com.norgic.vdotokcall.extensions.hide
import com.norgic.vdotokcall.extensions.invisible
import com.norgic.vdotokcall.extensions.show
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity
import com.norgic.vdotokcall.fragments.CallMangerListenerFragment
import com.norgic.vdotokcall.interfaces.FragmentRefreshListener
import com.norgic.vdotokcall.models.AcceptCallModel
import com.norgic.vdotokcall.models.UserModel
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.utils.TimeUtils.getTimeFromSeconds
import com.norgic.vdotokcall.utils.performSingleClick
import com.norgic.callsdk.CallClient
import org.webrtc.EglBase
import org.webrtc.VideoTrack
import java.util.*
import java.util.concurrent.TimeUnit


class AudioVideoCallFragment : CallMangerListenerFragment(), FragmentRefreshListener {

    private var acceptCallModel: AcceptCallModel? = null
    private var isIncomingCall = false
    private lateinit var binding: LayoutAudioFragmentBinding

    private lateinit var prefs: Prefs

    private lateinit var callClient: CallClient
    private var userModel : UserModel? = null
    private var name : String? = null

    private var userName : ObservableField<String> = ObservableField<String>()
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


        addTouchEventListener()


        screenWidth = context?.resources?.displayMetrics?.widthPixels!!
        screenHeight = context?.resources?.displayMetrics?.heightPixels!!
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




        } ?: kotlin.run{
            isVideoCall.set(arguments?.getBoolean(AllUserListFragment.IS_VIDEO_CALL) ?: false)
            name = (arguments?.get("userName") as CharSequence?).toString()
            acceptCallModel = arguments?.getParcelable(AcceptCallModel.TAG) as AcceptCallModel?
            isIncomingCall = true
        }
    }

    private fun setListeners(){
        binding.ivEndCall.performSingleClick { endCall()}

        binding.ivMute.setOnClickListener { muteButtonAction() }

        binding.ivSpeaker.setOnClickListener { speakerButtonAction() }

        binding.ivCamSwitch.setOnClickListener { cameraSwitchAction() }

        binding.ivCameraOnOff.setOnClickListener { if(isVideoCall.get())cameraOnOffAction() }
    }

    private fun cameraOnOffAction() {

        (activity as DashBoardActivity).activeSessionId?.let { sessionId ->

            isVideoResume = isVideoResume.not()

            when {
                isVideoResume -> {
                    binding.localViewCard.show()
                    binding.localView.show()
                    callClient.resumeVideo(sessionId)
                    binding.ivCameraOnOff.setImageResource(R.drawable.ic_call_video_rounded)
                }
                else -> {
                    binding.localViewCard.hide()
                    binding.localView.hide()
                    callClient.pauseVideo(sessionId)
                    binding.ivCameraOnOff.setImageResource(R.drawable.ic_video_off)
                }
            }

            callClient.switchCallType(sessionId, mcToken = prefs.loginInfo?.mcToken!!, prefs.loginInfo?.refId!!, 1, if (isVideoResume) 1 else 0)
        }


    }

    private fun cameraSwitchAction() {
        (activity as DashBoardActivity).activeSessionId?.let {
            callClient.switchCamera(it)
        }
    }

    private fun speakerButtonAction() {
        when {
            isSpeakerOff -> binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_on)
            else -> binding.ivSpeaker.setImageResource(R.drawable.ic_speaker_off)
        }

        isSpeakerOff = isSpeakerOff.not()
        callClient.toggleSpeakerOnOff()
    }

    private fun muteButtonAction() {
        isMuted = !isMuted

        when {
            isMuted -> binding.ivMute.setImageResource(R.drawable.ic_mute_mic1)
            else -> binding.ivMute.setImageResource(R.drawable.ic_unmute_mic)
        }

        (activity as DashBoardActivity).activeSessionId?.let {
                sessionId -> callClient.muteUnMuteMic(sessionId)
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
                                xPoint > screenWidth/2 && yPoint < screenHeight/2 -> {
                                    //First Quadrant
                                    animateView(((screenWidth - (view.width + THRESHOLD_VALUE))), (screenHeight/2 - (view.height + THRESHOLD_VALUE)))
                                }
                                xPoint < screenWidth/2 && yPoint < screenHeight/2 -> {
                                    //Second Quadrant
                                    animateView(THRESHOLD_VALUE, (screenHeight/2 - (view.height + THRESHOLD_VALUE)))
                                }
                                xPoint < screenWidth/2 && yPoint > screenHeight/2 -> {
                                    //Third Quadrant
                                    animateView(THRESHOLD_VALUE, (screenHeight/2 + view.height/2).toFloat())
                                }
                                else -> {
                                    //Fourth Quadrant
                                    animateView(((screenWidth - (view.width + THRESHOLD_VALUE))), (screenHeight/2 + view.height/2).toFloat())
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


    private fun animateView(xPoint: Float, yPoint: Float){
        binding.localViewCard.animate()
                .x(xPoint)
                .y(yPoint)
                .setDuration(200)
                .start()
    }

    private fun endCall() {
        stopTimer()
        resetCallData()
        (activity as DashBoardActivity).endCall()
        Navigation.findNavController(binding.root).navigate(R.id.action_open_userList)
    }

    private fun resetCallData(){
        callClient.speakerOff()
    }

    private fun displayUi(videoCall: Boolean) {
        userName.set(name)
        if (!videoCall){
            binding.tvCallType.text = getString(R.string.audio_calling)
            binding.ivCameraOnOff.setImageResource(R.drawable.ic_video_off)
            binding.remoteView.invisible()
            binding.ivCamSwitch.hide()
        }else{
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

    override fun onIncomingCall(model: AcceptCallModel) {}

    override fun onStartCalling() {}

    override fun outGoingCall(toPeer: String) {}

    override fun onRemoteStreamReceived(stream: VideoTrack, refId: String, sessionID: String) {
        val mainHandler = activity?.mainLooper?.let { Handler(it) }
        val myRunnable = Runnable {

            try {
                val videoView = binding.remoteView
                videoView.setMirror(true)
                videoView.init(EglBase.create().eglBaseContext, null)
                stream.addSink(videoView)
                videoView.setZOrderMediaOverlay(false)
                videoView.setZOrderOnTop(false)

            } catch (e: Exception) {
                Log.i("SocketLog", "onStreamAvailable: exception" + e.printStackTrace())
            }

        }
        mainHandler?.post(myRunnable)
    }

    override fun onCameraStreamReceived(stream: VideoTrack) {

        val mainHandler = activity?.let { Handler(it.mainLooper) }
        val myRunnable = Runnable {

            val videoView = binding.localView
            videoView.setMirror(true)
            videoView.init(EglBase.create().eglBaseContext, null)
            videoView.setEnableHardwareScaler(true)
            videoView.setZOrderMediaOverlay(true)
            stream.addSink(videoView)
            callClient.setView(videoView)

            binding.remoteView.setZOrderMediaOverlay(false)

        }
        mainHandler?.post(myRunnable)
    }

    override fun onCallMissed() {}

    override fun onCallRejected(reason: String) {}

    override fun endOngoingCall(sessionId: String) {
        activity?.runOnUiThread{
            endCall()
        }
    }

    override fun onAudioVideoStateChanged(audioState: Int, videoState: Int) {

        activity?.runOnUiThread {
            if (videoState == 1) {
                binding.imgUserPhoto.hide()
                binding.remoteView.show()
                binding.remoteView.setZOrderMediaOverlay(false)
                binding.remoteView.setZOrderOnTop(false)

                binding.localView.setZOrderMediaOverlay(true)
                binding.localView.setZOrderOnTop(true)

//                binding.localView.setEnableHardwareScaler(true)
//                binding.localView.setZOrderMediaOverlay(true)
//                // This we are doing to bring our local view to front
//                animateView(((screenWidth - (binding.localView.width + THRESHOLD_VALUE))), (screenHeight/2 + binding.localView.height/2).toFloat())
//                binding.localView.elevation = 10.0f


                refreshLocalCameraView()

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
//            animateView(
//                    ((screenWidth - (binding.localView.width + THRESHOLD_VALUE))),
//                    (screenHeight / 2 + binding.localView.height / 2).toFloat()
//            )
            //ViewCompat.setElevation(binding.remoteView, -1f)
            ViewCompat.setElevation(binding.localViewCard, 11f)
            ViewCompat.setElevation(binding.localView, 11f)
        }
    }


    override fun onInternetConnectionLoss() {
        activity?.runOnUiThread { endCall() }
    }

}