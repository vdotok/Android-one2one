package com.norgic.vdotokcall.feature.dashBoard.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.ActivityDashBoardBinding
import com.norgic.vdotokcall.extensions.showSnackBar
import com.norgic.vdotokcall.interfaces.FragmentRefreshListener
import com.norgic.vdotokcall.models.AcceptCallModel
import com.norgic.vdotokcall.models.LoginResponse
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.utils.ApplicationConstants
import com.norgic.vdotokcall.utils.NetworkStatusLiveData
import com.norgic.callsdk.CallClient
import com.norgic.callsdk.enums.CallType
import com.norgic.callsdk.enums.MediaType
import com.norgic.callsdk.enums.SessionType
import com.norgic.callsdk.interfaces.CallSDKListener
import com.norgic.callsdk.interfaces.StreamCallback
import com.norgic.callsdk.models.EnumConnectionStatus
import org.webrtc.VideoTrack


class DashBoardActivity : AppCompatActivity(), CallSDKListener, StreamCallback {

    private lateinit var binding: ActivityDashBoardBinding

    private lateinit var callClient: CallClient
    private lateinit var prefs: Prefs

    var localStream: VideoTrack? = null
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
                isInternetConnected == true && internetConnectionRestored -> connectClient()
                isInternetConnected == false -> {
                    internetConnectionRestored = true
                    mListener?.onInternetConnectionLoss()
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


    private fun connectClient() {
        prefs.sdkAuthResponse?.let {
            callClient.connect(it.mediaServerMap.completeAddress)
        }
    }


    companion object{

        const val TAG = "DASHBOARD_ACTIVITY"

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

    override fun connectionStatus(enumConnectionStatus: EnumConnectionStatus) {
        when (enumConnectionStatus) {
            EnumConnectionStatus.CONNECTED -> {
                runOnUiThread {
                    callClient.register(
                            authToken = prefs.loginInfo?.authorizationToken!!,
                            refId = prefs.loginInfo?.refId!!
                    )
                }
            }
            EnumConnectionStatus.NOT_CONNECTED -> {

                prefs.sdkAuthResponse?.let {
                    callClient.connect(it.mediaServerMap.completeAddress)
                }

                runOnUiThread {
                    Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show()
                }
            }
            EnumConnectionStatus.ERROR -> {

                prefs.sdkAuthResponse?.let {

                    callClient.connect(it.mediaServerMap.completeAddress)
                }
                runOnUiThread {
                    Toast.makeText(this, "Connection Error!", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {}
        }
    }

    override fun onClose(reason: String, isAccepted: Boolean) {
        mListener?.onCallRejected(reason)
    }

    override fun registerStatus(isRegister: Boolean, response: String) {
        runOnUiThread {
            Log.d(TAG, "registerStatus : $isRegister  -- response: $response")
        }
    }

    override fun audioVideoState(audioState: Int, videoState: Int) {
        mListener?.onAudioVideoStateChanged(audioState, videoState)
    }

    override fun incomingCall(
        from: String,
        sessionUUID: String,
        requestID: String,
        callType: CallType,
        mediaType: MediaType,
        sessionType: SessionType
    ) {
        val model = AcceptCallModel(from, sessionUUID, requestID, callType, mediaType, sessionType)
        mListener?.onIncomingCall(model)
    }

    override fun callConnected(callStatus: Boolean) {
        runOnUiThread {
            if (callStatus) {
                mListener?.onStartCalling()
            }
        }
    }

    //    while dialing one2one call we need to save the sessionID based on the receiver's refID
    fun dialOneToOneCall(mediaType: MediaType, refIdUser: String) {

        prefs.loginInfo?.let {

            it.mcToken?.let { mcToken ->
                activeSessionId = callClient.dialOne2OneCall(
                        it.refId!!,
                        arrayListOf(refIdUser),
                        mcToken,
                        mediaType
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


    override fun tokenRequestSuccess(mcToken: String) {
        val userModel: LoginResponse? = prefs.loginInfo
        userModel?.mcToken = mcToken
        userModel?.let {
            prefs.loginInfo = it
        }

        runOnUiThread {
            binding.root.showSnackBar("Socket Connected!")
        }
    }

    fun acceptIncomingCall(
        from: String,
        sessionUUID: String,
        requestID: String,
        callType: CallType,
        mediaType: MediaType,
        sessionType: SessionType
    ) {
        prefs.loginInfo?.let {
            activeSessionId = callClient.acceptIncomingCall(
                    it.refId!!,
                    sessionUUID,
                    requestID,
                    arrayListOf(from),
                    it.mcToken!!,
                    callType,
                    mediaType,
                    sessionType
            )
        }

//        startTimer()

    }

    fun endCall() {
        localStream = null
        activeSessionId?.let {
            callClient.endCallSession(it)
        }
    }

    override fun invalidResponse(message: String) {}

    override fun responseMessages(message: String) {
        runOnUiThread { Toast.makeText(this, "Response: $message", Toast.LENGTH_SHORT).show() }
    }

    override fun outgoingCall(toPeer: String) {
        mListener?.outGoingCall(toPeer)
    }

    override fun endOutgoingCall(sessionId: String) {
        mListener?.endOngoingCall(sessionId)
    }

    override fun onSessionReady() {
        //Use for Screen Sharing
    }

    override fun callMissed() {
        mListener?.onCallMissed()
    }
    
    
    // Stream callbacks
    override fun onRemoteStream(stream: VideoTrack, refId: String, sessionID: String) {
        mListener?.onRemoteStreamReceived(stream, refId, sessionID)
    }

    override fun onCameraStream(stream: VideoTrack) {
        localStream = stream
        mListener?.onCameraStreamReceived(stream)
    }

}