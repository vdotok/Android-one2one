package com.norgic.vdotokcall.feature.dashBoard.fragment

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.LayoutCallRecieverBinding
import com.norgic.vdotokcall.extensions.hide
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity
import com.norgic.vdotokcall.fragments.CallMangerListenerFragment
import com.norgic.vdotokcall.interfaces.FragmentRefreshListener
import com.norgic.vdotokcall.models.AcceptCallModel
import com.norgic.vdotokcall.models.UserModel
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.utils.performSingleClick
import com.norgic.callsdk.CallClient
import com.norgic.callsdk.enums.MediaType
import org.webrtc.VideoTrack


/**
 * Created By: Norgic
 * Date & Time: On 2/25/21 At 12:14 PM in 2021
 */
class IncomingAndDialCallFragment : CallMangerListenerFragment(), FragmentRefreshListener {


    private lateinit var binding: LayoutCallRecieverBinding
    private var userModel : UserModel? = null
    var username : String? = null

    private var acceptCallModel : AcceptCallModel? = null
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
            acceptCallModel = arguments?.get(AcceptCallModel.TAG) as AcceptCallModel?
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

        playTone()

        userName.set(username)


        when (acceptCallModel?.mediaType) {
            MediaType.AUDIO -> {

                incomingCallTitle.set(getString(R.string.incoming_call))
            }
            else -> {
                incomingCallTitle.set(getString(R.string.incoming_video_call))
            }
        }

        binding.imgCallAccept.performSingleClick {
           openAudioCallFragment()
        }

        binding.imgCallReject.performSingleClick {
            prefs.loginInfo?.let {
                acceptCallModel?.let { it1 -> callClient.rejectIncomingCall(
                    it.refId!!,
                    it1.sessionUUID
                )
                }
            }
            (activity as DashBoardActivity).endCall()
            activity?.onBackPressed()
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
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

        acceptCallModel?.let {

            (activity as DashBoardActivity).acceptIncomingCall(
                it.from,
                it.sessionUUID,
                it.requestID,
                it.deviceType,
                it.mediaType,
                it.sessionType
            )


            openCallFragment()
        }
    }


     private fun getDefaultRingtone(context: Context) :Ringtone {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            return RingtoneManager.getRingtone(context,uri)
        }


    private fun openCallFragment(){
        val bundle = Bundle()
        bundle.putString("userName", userName.get())
        bundle.putBoolean(
            AllUserListFragment.IS_VIDEO_CALL,
            acceptCallModel?.mediaType == MediaType.VIDEO
        )
        bundle.putParcelable(AcceptCallModel.TAG, acceptCallModel)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_call_fragment, bundle)
    }


    override fun onIncomingCall(model: AcceptCallModel) {

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

    override fun outGoingCall(toPeer: String) {

    }

    override fun onRemoteStreamReceived(stream: VideoTrack, refId: String, sessionID: String) {}

    override fun onCameraStreamReceived(stream: VideoTrack) {}

    override fun onCallRejected(reason: String) {
        closeFragmentWithMessage(reason)
    }

    override fun endOngoingCall(sessionId: String) {}

    override fun onAudioVideoStateChanged(audioState: Int, videoState: Int) {}

    override fun onInternetConnectionLoss() {}

    override fun onCallMissed() {
       closeFragmentWithMessage("call missed!")
    }


    private fun closeFragmentWithMessage(message: String){
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            activity?.onBackPressed()
        }
    }
}