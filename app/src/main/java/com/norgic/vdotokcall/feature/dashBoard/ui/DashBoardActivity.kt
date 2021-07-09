package com.norgic.vdotokcall.feature.dashBoard.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.norgic.callsdks.CallClient
import com.norgic.callsdks.commands.CallInfoResponse
import com.norgic.callsdks.commands.RegisterResponse
import com.norgic.callsdks.enums.*
import com.norgic.callsdks.interfaces.CallSDKListener
import com.norgic.callsdks.interfaces.StreamCallback
import com.norgic.callsdks.models.CallParams
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.ActivityDashBoardBinding
import com.norgic.vdotokcall.extensions.showSnackBar
import com.norgic.vdotokcall.interfaces.FragmentRefreshListener
import com.norgic.vdotokcall.models.LoginResponse
import com.norgic.vdotokcall.models.MediaServerMap
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.utils.ApplicationConstants
import com.norgic.vdotokcall.utils.NetworkStatusLiveData
import org.webrtc.VideoTrack


class DashBoardActivity: AppCompatActivity(), CallSDKListener, StreamCallback {

    private lateinit var binding: ActivityDashBoardBinding

    private lateinit var callClient: CallClient
    private lateinit var prefs: Prefs

    var localStream: VideoTrack? = null
    var remoteStream: VideoTrack? = null
    var activeSessionId: String? = null
    var mListener: FragmentRefreshListener? = null

    private var internetConnectionRestored = false
    private lateinit var myLiveData: NetworkStatusLiveData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dash_board)
        askForPermissions()
        prefs = Prefs(this)

        initCallClient()

        addInternetConnectionObserver()

    }

    private fun addInternetConnectionObserver() {
        myLiveData = NetworkStatusLiveData(this.application)

        myLiveData.observe(this, { isInternetConnected ->
            when {
                isInternetConnected == true && internetConnectionRestored -> {connectClient()
                    mListener?.onConnectionSuccess()
                }
                isInternetConnected == false -> {
                    internetConnectionRestored = true
                    mListener?.onInternetConnectionLoss()
                    mListener?.onConnectionFail()
                }
                else -> {}
            }
        })
    }

    private fun askForPermissions() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                    ApplicationConstants.MY_PERMISSIONS_REQUEST
            )
        } else if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.RECORD_AUDIO),
                    ApplicationConstants.MY_PERMISSIONS_REQUEST_RECORD_AUDIO
            )
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.CAMERA),
                    ApplicationConstants.MY_PERMISSIONS_REQUEST_CAMERA
            )
        }
    }


    private fun initCallClient() {

        CallClient.getInstance(this)?.setConstants(ApplicationConstants.SDK_PROJECT_ID)
        CallClient.getInstance(this)?.let {
            callClient = it
            callClient.setListener(this, this)
        }
        
        connectClient()

    }


    fun connectClient() {
        prefs.sdkAuthResponse?.let {
            if (callClient.isConnected() == null || callClient.isConnected() == false)
                callClient.connect(getMediaServerAddress(it.mediaServerMap), it.mediaServerMap.endPoint)
        }
    }


    private fun getMediaServerAddress(mediaServer: MediaServerMap): String {
        return "https://${mediaServer.host}:${mediaServer.port}"
    }

    companion object{

        fun createDashBoardActivity(context: Context) = Intent(
                context,
                DashBoardActivity::class.java
        ).apply {
            addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
    }


    override fun onConnect() {
        runOnUiThread { binding.root.showSnackBar("Connected!") }
    }

    override fun onError(cause: String) {
//        connectClient()
    }

    override fun onSessionReady(mediaProjection: MediaProjection?) {
    }

    override fun audioVideoState(audioState: Int, videoState: Int, refId: String) {
        mListener?.onAudioVideoStateChanged(audioState, videoState)
    }

    override fun callStatus(callInfoResponse: CallInfoResponse) {
        runOnUiThread {
            when (callInfoResponse.callStatus) {
                CallStatus.CALL_CONNECTED -> {
                    mListener?.onStartCalling()
                }
                CallStatus.OUTGOING_CALL_ENDED,
                CallStatus.CALL_ENDED_SUCCESS-> {
                    activeSessionId?.let { mListener?.endOngoingCall(it) }
                }
                CallStatus.CALL_REJECTED -> {
                    mListener?.onCallRejected()
                }
                CallStatus.CALL_MISSED -> {
                    mListener?.onCallMissed()
                }
                CallStatus.NO_ANSWER_FROM_TARGET -> {
                    mListener?.onCallNoAnswer()
                }
                CallStatus.TARGET_IS_BUSY -> {
                    mListener?.onCallBusy()
                }
                CallStatus.SESSION_TIMEOUT -> {
                    mListener?.onCallTimeout()
                }
                else -> {}

            }
        }
    }

    override fun connectionStatus(enumConnectionStatus: EnumConnectionStatus) {
        when (enumConnectionStatus) {
            EnumConnectionStatus.CONNECTED -> {
                mListener?.onConnectionSuccess()
                runOnUiThread {
                    callClient.register(
                            authToken = prefs.loginInfo?.authorizationToken!!,
                            refId = prefs.loginInfo?.refId!!
                    )
                }
            }
            EnumConnectionStatus.NOT_CONNECTED -> {
                mListener?.onConnectionFail()
                connectClient()

                runOnUiThread {
                    Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show()
                }
            }
            EnumConnectionStatus.ERROR -> {
                mListener?.onConnectionFail()
                connectClient()
                runOnUiThread {
                    Toast.makeText(this, "Connection Error!", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

//    override fun onClose(reason: String) {
//        mListener?.onCallRejected(reason)
//    }

    //    while dialing one2one call we need to save the sessionID based on the receiver's refID
    fun dialOneToOneCall(mediaType: MediaType, refIdUser: String) {

        prefs.loginInfo?.let {
            it.mcToken?.let { mcToken ->
                activeSessionId = callClient.dialOne2OneCall(
                    callParams = CallParams(
                        refId = it.refId!!,
                        toRefIds = arrayListOf(refIdUser),
                        mcToken = mcToken,
                        mediaType = mediaType,
                        callType = CallType.ONE_TO_ONE,
                        sessionType = SessionType.CALL
                    )
                )
            } ?: kotlin.run {
                val snackbar: Snackbar = Snackbar
                        .make(binding.root, "Client is not connected! Please try reconnecting client", Snackbar.LENGTH_LONG)
                        .setAction("RECONNECT") { connectClient() }
                snackbar.show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        internetConnectionRestored = false
    }

    override fun onDestroy() {
        callClient.disConnectSocket()
        myLiveData.removeObservers(this)
        super.onDestroy()
    }

    fun acceptIncomingCall(callParams: CallParams) {
        prefs.loginInfo?.let {
            activeSessionId = callClient.acceptIncomingCall(
                    it.refId!!, callParams
            )
        }
    }

    fun endCall() {
        localStream = null
        remoteStream = null
        activeSessionId?.let {
            callClient.endCallSession(it)
        }
    }

    override fun invalidResponse(message: String) {}

    // when socket is disconnected
    override fun onClose(reason: String) {
        connectClient()
    }

    override fun responseMessages(message: String) {
        runOnUiThread { Toast.makeText(this, "Response: $message", Toast.LENGTH_SHORT).show() }
    }

    override fun incomingCall(callParams: CallParams) {
        if(activeSessionId?.let { callClient.getActiveSessionClient(it) != null } == true){
            callClient.sessionBusy(prefs.loginInfo?.refId!!, callParams.sessionUUID)
            return
        }
        mListener?.onAcceptIncomingCall(callParams)
    }

    override fun registrationStatus(registerResponse: RegisterResponse) {

        when (registerResponse.registrationStatus) {
            RegistrationStatus.REGISTER_SUCCESS -> {
                val userModel: LoginResponse? = prefs.loginInfo
                userModel?.mcToken = registerResponse.mcToken.toString()
                runOnUiThread {
                    userModel?.let {
                        prefs.loginInfo = it
                    }
                    binding.root.showSnackBar("Socket Connected!")
                }
            }
            RegistrationStatus.UN_REGISTER,
            RegistrationStatus.REGISTER_FAILURE,
            RegistrationStatus.INVALID_REGISTRATION -> {
                Handler(Looper.getMainLooper()).post {
                    Log.e("register", "message: ${registerResponse.responseMessage}")
                }
            }
        }
    }
    
    
    // Stream callbacks
    override fun onRemoteStream(stream: VideoTrack, refId: String, sessionID: String) {
        remoteStream = stream
        mListener?.onRemoteStreamReceived(stream, refId, sessionID)
    }

    override fun onCameraStream(stream: VideoTrack) {
        localStream = stream
        mListener?.onCameraStreamReceived(stream)
    }

    override fun onRemoteStream(refId: String, sessionID: String) {
//        mListener?.onRemoteStreamReceived()
    }

}