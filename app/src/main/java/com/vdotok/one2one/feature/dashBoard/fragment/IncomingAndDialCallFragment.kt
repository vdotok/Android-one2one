package com.vdotok.one2one.feature.dashBoard.fragment

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.vdotok.one2one.R
import com.vdotok.one2one.databinding.LayoutCallRecieverBinding
import com.vdotok.one2one.extensions.hide
import com.vdotok.one2one.feature.dashBoard.ui.DashBoardActivity
import com.vdotok.one2one.fragments.CallMangerListenerFragment
import com.vdotok.one2one.interfaces.FragmentRefreshListener
import com.vdotok.one2one.models.AcceptCallModel
import com.vdotok.one2one.models.UserModel
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
class IncomingAndDialCallFragment : CallMangerListenerFragment(), FragmentRefreshListener {


    private var countDownTimer: CountDownTimer? = null
    private var isFragmentOpen = true
    private lateinit var binding: LayoutCallRecieverBinding
    private var userModel : UserModel? = null
    var username : String? = null

    private var callParams : CallParams? = null
    private var isVideoCall: Boolean = false

    private var userName : ObservableField<String> = ObservableField<String>()
    private var incomingCallTitle : ObservableField<String> = ObservableField<String>()

    private val isIncomingCall = ObservableBoolean(false)



    private lateinit var callClient: CallClient
    private lateinit var prefs: Prefs

    private var ringtone: Ringtone? = null

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

        arguments?.get(UserModel.TAG)?.let {
            isVideoCall = arguments?.getBoolean(AllUserListFragment.IS_VIDEO_CALL) ?: false
            userModel = it as UserModel?
            isIncomingCall.set(arguments?.get("isIncoming") as Boolean)
        } ?: kotlin.run{
            username = arguments?.get("userName") as String?
            callParams = arguments?.get(CALL_PARAMS) as CallParams?
            isIncomingCall.set(arguments?.get("isIncoming") as Boolean)
        }
    }

    private fun setDataForDialCall() {
        userName.set(userModel?.fullName)

        binding.imgCallAccept.hide()
        incomingCallTitle.set(getString(R.string.calling))

        binding.imgCallReject.performSingleClick {
            (activity as DashBoardActivity).endCall()
            activity?.onBackPressed()
        }


    }

    private fun setDataForIncomingCall() {
        startCountDownForIncomingCall()

        playTone()

        userName.set(username)


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
                callParams?.let { it1 -> callClient.rejectIncomingCall(
                    it.refId!!,
                    it1.sessionUUID
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


    private fun startCountDownForIncomingCall(){
        countDownTimer = object : CountDownTimer(TimeUnit.SECONDS.toMillis(20), TimeUnit.SECONDS.toMillis(10)) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                sendCallTimeOut()
            }
        }.start()
    }

    private fun sendCallTimeOut() {
        callClient.callTimeout(prefs.loginInfo?.refId!!, callParams?.sessionUUID!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposeCountDownTimer()
        stopTune()
    }

    private fun playTone(){
        ringtone = getDefaultRingtone(activity as Context)
        if (!ringtone?.isPlaying!!){
            ringtone?.play()
        }
    }

    private fun stopTune(){
        if (ringtone?.isPlaying == true){
            ringtone?.stop()
        }
        ringtone = null
    }

    private fun openAudioCallFragment() {

        callParams?.let {
            (activity as DashBoardActivity).acceptIncomingCall(it)
            openCallFragment()
        }
    }


     private fun getDefaultRingtone(context: Context) :Ringtone {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            return RingtoneManager.getRingtone(context, uri)
        }


    private fun openCallFragment(){
        val bundle = Bundle()
        bundle.putString("userName", userName.get())
        bundle.putBoolean(
            AllUserListFragment.IS_VIDEO_CALL,
            callParams?.mediaType == MediaType.VIDEO
        )
        bundle.putParcelable(AcceptCallModel.TAG, callParams)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_call_fragment, bundle)
    }

    override fun onStartCalling() {
        activity?.let {
            it.runOnUiThread {
                val bundle = Bundle()
                bundle.putParcelable(UserModel.TAG, userModel)
                bundle.putBoolean(AllUserListFragment.IS_VIDEO_CALL, isVideoCall)
                bundle.putBoolean("isIncoming", false)
                Navigation.findNavController(binding.root).navigate(
                    R.id.action_open_call_fragment,
                    bundle
                )
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

    override fun onCallBusy() {
        closeFragmentWithMessage("User busy")
    }

    override fun onCallTimeout() {
        closeFragmentWithMessage("Call timeout")
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