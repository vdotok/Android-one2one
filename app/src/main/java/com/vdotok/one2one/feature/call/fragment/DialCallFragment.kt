package com.vdotok.one2one.feature.call.fragment

import android.content.Context
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.vdotok.network.models.UserModel
import com.vdotok.one2one.R
import com.vdotok.one2one.base.BaseActivity
import com.vdotok.one2one.base.BaseFragment
import com.vdotok.one2one.callback.FragmentCallback
import com.vdotok.one2one.databinding.LayoutCallRecieverBinding
import com.vdotok.one2one.extensions.hide
import com.vdotok.one2one.feature.call.activity.CallActivity
import com.vdotok.one2one.models.AcceptCallModel
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.one2one.utils.ApplicationConstants.CALL_PARAMS
import com.vdotok.one2one.utils.performSingleClick
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.models.CallParams
import java.util.concurrent.TimeUnit


/**
 * Created By: VdoTok
 * Date & Time: On 2/25/21 At 12:14 PM in 2021
 */
class DialCallFragment : BaseFragment(), FragmentCallback {


    private var countDownTimer: CountDownTimer? = null
    private var isFragmentOpen = true
    private lateinit var binding: LayoutCallRecieverBinding
    private var userModel: UserModel? = null
    var username: String? = null

    private var callParams: CallParams? = null
    private var isVideoCall: Boolean = false

    private var userName: ObservableField<String> = ObservableField<String>()
    private var incomingCallTitle: ObservableField<String> = ObservableField<String>()

    private val isIncomingCall = ObservableBoolean(false)


    private lateinit var callClient: CallClient
    private lateinit var prefs: Prefs
    var player: MediaPlayer? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutCallRecieverBinding.inflate(inflater, container, false)
        prefs = Prefs(activity)
        CallClient.getInstance(activity as Context)?.let {
            callClient = it
        }


        setBindingData()
        setArgumentsData()
        BaseActivity.mListener = this

        when {
            isIncomingCall.get() -> setDataForIncomingCall()
            else -> setDataForDialCall()
        }

        return binding.root
    }

    private fun setBindingData() {
        binding.username = userName
        binding.incomingCallTitle = incomingCallTitle
    }

    private fun setArgumentsData() {
        binding.isIncomingCall = isIncomingCall
        isIncomingCall.set(arguments?.get(CallActivity.IN_COMING_CALL) as Boolean)
        isVideoCall = arguments?.getBoolean(CallActivity.VIDEO_CALL) ?: false

        callParams = arguments?.get(CALL_PARAMS) as CallParams?

        arguments?.get(UserModel.TAG)?.let {
            userModel = it as UserModel?
        }
    }

    private fun setDataForDialCall() {
        userName.set(userModel?.fullName)

        binding.imgCallAccept.hide()
        incomingCallTitle.set(getString(R.string.calling))

        binding.imgCallReject.performSingleClick {
            (activity as CallActivity).endCall()
        }


    }

    private fun setDataForIncomingCall() {
        player = MediaPlayer.create(this.requireContext(), Settings.System.DEFAULT_RINGTONE_URI)
        startCountDownForIncomingCall()

        playTone()
        userName.set(userModel?.userName)
        when (callParams?.mediaType) {
            MediaType.AUDIO -> {
                incomingCallTitle.set(getString(R.string.incoming_call))
            }
            else -> {
                incomingCallTitle.set(getString(R.string.incoming_video_call))
            }
        }

        binding.imgCallAccept.performSingleClick {
            openAudioCallFragment()
            disposeCountDownTimer()
        }

        binding.imgCallReject.performSingleClick {
            disposeCountDownTimer()
            prefs.loginInfo?.let {
                callParams?.let { it1 ->
                    callClient.rejectIncomingCall(
                        it.refId!!,
                        it1.sessionUuid
                    )
                }
            }
            activity?.onBackPressed()
        }
    }

    private fun disposeCountDownTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }


    private fun startCountDownForIncomingCall() {
        countDownTimer =
            object : CountDownTimer(TimeUnit.SECONDS.toMillis(20), TimeUnit.SECONDS.toMillis(10)) {
                override fun onTick(millisUntilFinished: Long) {}
                override fun onFinish() {
                    sendCallTimeOut()
                }
            }.start()
    }

    private fun sendCallTimeOut() {
        callClient.callTimeout(prefs.loginInfo?.refId!!, callParams?.sessionUuid!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposeCountDownTimer()
        stopTune()
    }

    override fun onResume() {
        super.onResume()
        if ((activity as BaseActivity).callMissed) {
            activity?.finish()
        }
    }

    private fun playTone() {
        player?.let {
            if (!it.isPlaying)
                player?.start()
        }
    }

    private fun stopTune() {
        player?.let {
            if (it.isPlaying)
                player?.stop()
        }
        player = null
    }

    private fun openAudioCallFragment() {

        callParams?.let {
            (activity as CallActivity).acceptIncomingCall(it)
            Handler(Looper.getMainLooper()).postDelayed({
                openCallFragment()
            }, 1000)
        }
    }


    private fun openCallFragment() {
        val bundle = Bundle()
        bundle.putString("userName", userName.get())
        bundle.putParcelable(UserModel.TAG, userModel)
        bundle.putBoolean(
            CallActivity.VIDEO_CALL,
            callParams?.mediaType == MediaType.VIDEO
        )
        bundle.putParcelable(AcceptCallModel.TAG, callParams)
        bundle.putBoolean(CallActivity.IN_COMING_CALL, true)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_call_fragment, bundle)
    }

    override fun onStartCalling() {
        activity?.let {
            it.runOnUiThread {
                val bundle = Bundle()
                bundle.putParcelable(UserModel.TAG, userModel)
                bundle.putBoolean(CallActivity.VIDEO_CALL, isVideoCall)
                bundle.putBoolean(CallActivity.IN_COMING_CALL, false)
                bundle.putParcelable(AcceptCallModel.TAG, callParams)
                try {
                    Navigation.findNavController(binding.root).navigate(
                        R.id.action_open_call_fragment,
                        bundle
                    )
                } catch (ex: Exception) {
                    Log.e("NavigationIssue", "onStartCalling: ${ex.printStackTrace()}")
                }
            }
        }
    }

    override fun onCallRejected() {
        closeFragmentWithMessage("Call rejected")
    }

    override fun onCallMissed() {
        closeFragmentWithMessage("Call missed")
    }

    override fun endOngoingCall(sessionId: String) {
        closeFragmentWithMessage("Call ended")
    }

    override fun onCallNoAnswer() {
        closeFragmentWithMessage("No answer")
    }

    override fun onUserNotAvailable() {
        closeFragmentWithMessage("User Not Available!")
    }

    override fun onCallBusy() {
        closeFragmentWithMessage("User busy")
    }

    override fun onCallTimeout() {
        closeFragmentWithMessage("Call timeout")
    }

    override fun onInsufficientBalance() {
        closeFragmentWithMessage("Insufficient Balance!")
    }

    private fun closeFragmentWithMessage(message: String) {
        if (isFragmentOpen) {
            activity?.runOnUiThread {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                activity?.onBackPressed()
            }
            isFragmentOpen = false
        }
    }
}