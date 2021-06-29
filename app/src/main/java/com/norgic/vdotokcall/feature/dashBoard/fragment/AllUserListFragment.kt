package com.norgic.vdotokcall.feature.dashBoard.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.navigation.Navigation
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.LayoutAllUserListBinding
import com.norgic.vdotokcall.extensions.*
import com.norgic.vdotokcall.feature.account.ui.AccountActivity
import com.norgic.vdotokcall.feature.dashBoard.adapter.AllUserListAdapter
import com.norgic.vdotokcall.feature.dashBoard.adapter.OnItemClickCallback
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity
import com.norgic.vdotokcall.fragments.CallMangerListenerFragment
import com.norgic.vdotokcall.models.AcceptCallModel
import com.norgic.vdotokcall.models.UserModel
import com.norgic.vdotokcall.network.ApiService
import com.norgic.vdotokcall.network.Result
import com.norgic.vdotokcall.network.RetrofitBuilder
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.utils.ApplicationConstants
import com.norgic.vdotokcall.utils.isInternetAvailable
import com.norgic.vdotokcall.utils.safeApiCall
import com.norgic.callsdk.enums.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.webrtc.VideoTrack
import retrofit2.HttpException

class AllUserListFragment: CallMangerListenerFragment(), OnItemClickCallback {

    lateinit var adapter: AllUserListAdapter
    private lateinit var binding: LayoutAllUserListBinding
    private lateinit var prefs: Prefs

    private var userReceiver: UserModel?= null
    private var isVideoCall: Boolean = false

    private var edtSearch = ObservableField<String>()
    private var userName = ObservableField<String>()

    private var userList: List<UserModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = LayoutAllUserListBinding.inflate(inflater, container, false)
        prefs = Prefs(activity)

        //(activity as DashBoardActivity).mListener = this

        init()
        textListenerForSearch()
        getAllUsers()
        addPullToRefresh()

        return binding.root
    }

    private fun init() {
        initUserListAdapter()

        binding.search = edtSearch
        binding.username = userName

        edtSearch.set("")

        binding.customToolbar.tvTitle.setText(R.string.contactList)
        binding.customToolbar.imgArrowBack.hide()

        userName.set(prefs.loginInfo?.fullName)

        binding.tvLogout.setOnClickListener {
            prefs.deleteKeyValuePair(ApplicationConstants.LOGIN_INFO)
            startActivity(context?.let { it1 -> AccountActivity.createAccountsActivity(it1) })
        }

    }
    private fun addPullToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getAllUsers()
        }
    }


    private fun initUserListAdapter() {
        adapter = AllUserListAdapter(ArrayList(), this)
        binding.rcvUserList.adapter = adapter
    }

    private fun getAllUsers() {
        binding.progressBar.toggleVisibility()
        binding.swipeRefreshLayout.isRefreshing = false

        activity?.let { activity ->
            val apiService: ApiService = RetrofitBuilder.makeRetrofitService(activity as Context)
            prefs.loginInfo?.authToken.let {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = safeApiCall { apiService.getAllUsers(auth_token = "Bearer $it") }
                    withContext(Dispatchers.Main) {
                        try {
                            when (response) {
                                is Result.Success -> {

                                    userList = response.data.users
                                    adapter.updateData(response.data.users)
                                }
                                is Result.Error -> {
                                    Log.e(API_ERROR, response.error.message)
                                    if (isInternetAvailable(activity as Context).not())
                                        binding.root.showSnackBar(getString(R.string.no_network_available))
                                    else
                                        binding.root.showSnackBar(response.error.message)
                                }
                            }

                        } catch (e: HttpException) {
                            Log.e(API_ERROR, "AllUserList: ${e.printStackTrace()}")
                        } catch (e: Throwable) {
                            Log.e(API_ERROR, "AllUserList: ${e.printStackTrace()}")
                        }
                        binding.progressBar.toggleVisibility()
                        binding.swipeRefreshLayout.isRefreshing = false

                    }
                }
            }
        }
    }

    private fun textListenerForSearch() {
            binding.searchEditText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                   adapter.filter?.filter(s)
                }
            })
        }

    private fun openCallFragment(toPeer: String, isVideo: Boolean) {
        userReceiver = userList.find { it.refId == toPeer }
        val bundle = Bundle()
        bundle.putParcelable(UserModel.TAG, userReceiver)
        bundle.putBoolean(IS_VIDEO_CALL, isVideo)
        bundle.putBoolean("isIncoming",false)
        Navigation.findNavController(binding.root).navigate(R.id.action_open_audio_fragment,bundle)
    }

    override fun audioCall(position: Int) {
        dialCall(position, false)
    }

    override fun videoCall(position: Int) {
        dialCall(position, true)
    }

    private fun dialCall(position: Int, isVideo: Boolean){

        val item = adapter.dataList[position]
        item.isSelected = item.isSelected.not()

        isVideoCall = isVideo

        (activity as DashBoardActivity).dialOneToOneCall(if(isVideo) MediaType.VIDEO else MediaType.AUDIO, item.refId!!)

        if(prefs.loginInfo?.mcToken != null) outGoingCall(item.refId!!)

    }

    override fun searchResult(position: Int) {
        edtSearch.get()?.isNotEmpty()?.let {
            if (position == 0 && it){
//                binding.check.show()
                binding.rcvUserList.hide()
            }else{
//                binding.check.hide()
                binding.rcvUserList.show()
            }
        }
    }

    companion object {

        const val TAG_FRAGMENT_USER_LIST = "TAG_FRAGMENT_USER_LIST"
        const val API_ERROR = "API_ERROR"
        const val IS_VIDEO_CALL = "IS_VIDEO_CALL"

    }

    override fun onIncomingCall(model: AcceptCallModel) {
        activity?.runOnUiThread {
            userReceiver = userList.find { it.refId == model.from }
            val bundle = Bundle()
            bundle.putString("userName",userReceiver?.fullName)
            bundle.putParcelable(AcceptCallModel.TAG, model)
            bundle.putBoolean("isIncoming",true)
            Navigation.findNavController(binding.root).navigate(R.id.action_open_audio_fragment, bundle)
        }
    }


    override fun onStartCalling() {
        Log.d(TAG_FRAGMENT_USER_LIST,"")
    }

    override fun outGoingCall(toPeer: String) {
        activity?.let {
            it.runOnUiThread {
                openCallFragment(toPeer,isVideoCall)
            }
        }
    }

    override fun onRemoteStreamReceived(stream: VideoTrack, refId: String, sessionID: String) {
        Log.d(TAG_FRAGMENT_USER_LIST,"")
    }

    override fun onCameraStreamReceived(stream: VideoTrack) {
        Log.d(TAG_FRAGMENT_USER_LIST,"")
    }

    override fun onCallMissed() {}

    override fun onCallRejected(reason: String) {}

    override fun endOngoingCall(sessionId: String) {}

    override fun onAudioVideoStateChanged(audioState: Int, videoState: Int) {}

    override fun onInternetConnectionLoss() {}

}




