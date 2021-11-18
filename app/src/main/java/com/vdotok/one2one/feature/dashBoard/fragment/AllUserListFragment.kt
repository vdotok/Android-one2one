package com.vdotok.one2one.feature.dashBoard.fragment

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.fragment.app.viewModels
import com.vdotok.network.models.UserModel
import com.vdotok.network.network.Result
import com.vdotok.one2one.R
import com.vdotok.one2one.base.BaseActivity
import com.vdotok.one2one.base.BaseFragment
import com.vdotok.one2one.databinding.LayoutAllUserListBinding
import com.vdotok.one2one.extensions.hide
import com.vdotok.one2one.extensions.show
import com.vdotok.one2one.extensions.showSnackBar
import com.vdotok.one2one.extensions.toggleVisibility
import com.vdotok.one2one.feature.account.ui.AccountActivity
import com.vdotok.one2one.feature.call.activity.CallActivity
import com.vdotok.one2one.feature.dashBoard.activity.DashBoardActivity
import com.vdotok.one2one.feature.dashBoard.adapter.AllUserListAdapter
import com.vdotok.one2one.feature.dashBoard.adapter.OnItemClickCallback
import com.vdotok.one2one.feature.dashBoard.viewmodel.AllUserViewModel
import com.vdotok.one2one.models.AcceptCallModel
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.one2one.utils.ApplicationConstants
import com.vdotok.one2one.utils.isInternetAvailable
import com.vdotok.streaming.CallClient
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.models.CallParams
import retrofit2.HttpException

class AllUserListFragment: BaseFragment(), OnItemClickCallback {

    lateinit var adapter: AllUserListAdapter
    private lateinit var binding: LayoutAllUserListBinding
    private lateinit var prefs: Prefs

    private var userReceiver: UserModel?= null
    private var isVideoCall: Boolean = false

    private var edtSearch = ObservableField<String>()
    private var userName = ObservableField<String>()

    private var userList: List<UserModel> = ArrayList()
    private lateinit var callClient: CallClient

    private var viewLayout: View? = null
    private val viewModel : AllUserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        if (viewLayout == null) {
            binding = LayoutAllUserListBinding.inflate(inflater, container, false)
            prefs = Prefs(activity)
            init()
            textListenerForSearch()
            getAllUsers()
            addPullToRefresh()
            viewLayout = binding.root
        }
        return viewLayout!!
    }

    private fun init() {
        initUserListAdapter()

        binding.search = edtSearch
        binding.username = userName

        CallClient.getInstance(activity as Context)?.let {
            callClient = it
        }

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
            (activity as DashBoardActivity).connectClient()
        }
    }


    private fun initUserListAdapter() {
        adapter = AllUserListAdapter(ArrayList(), this)
        binding.rcvUserList.adapter = adapter
    }

    private fun getAllUsers() {


        activity?.let { activity ->

            prefs.loginInfo?.authToken.let {

                viewModel.getAllUsers(this.requireContext(), "Bearer $it").observe(viewLifecycleOwner, {
                    try {
                        when (it) {
                            is Result.Loading -> {

                                binding.swipeRefreshLayout.isRefreshing = false
                                binding.progressBar.toggleVisibility()

                            }
                            is Result.Success -> {
                                binding.progressBar.toggleVisibility()
                                userList = it.data.users
                                adapter.updateData(it.data.users)
                            }
                            is Result.Failure -> {
                                binding.swipeRefreshLayout.isRefreshing = false
                                binding.progressBar.toggleVisibility()
                                Log.e(API_ERROR, it.exception.message ?: "")
                                if (isInternetAvailable(activity as Context).not())
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                else
                                    binding.root.showSnackBar(it.exception.message)
                            }
                        }

                    } catch (e: HttpException) {
                        Log.e(API_ERROR, "AllUserList: ${e.printStackTrace()}")
                    } catch (e: Throwable) {
                        Log.e(API_ERROR, "AllUserList: ${e.printStackTrace()}")
                    }
                })
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
                ) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable) {
                   adapter.filter?.filter(s)
                }
            })
        }

    private fun openCallFragment(toPeer: String, isVideo: Boolean) {
        userReceiver = userList.find { it.refId == toPeer }
        startActivity(CallActivity.createIntent(requireContext(),
            userReceiver, isVideo, false, null, null,
            (requireActivity() as BaseActivity).activeSessionId))

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

    override fun onResume() {
        super.onResume()
        if (callClient.isConnected() == true) {
            binding.tvLed.setImageResource(R.drawable.led_connected)
        } else {
            binding.tvLed.setImageResource(R.drawable.led_error)
        }
        binding.tvLed.postDelayed({
            BaseActivity.mListener = this@AllUserListFragment
        }, 1000)
    }

    override fun searchResult(position: Int) {
        edtSearch.get()?.isNotEmpty()?.let {
            if (position == 0 && it){
                binding.rcvUserList.hide()
            } else {
                binding.rcvUserList.show()
            }
        }
    }

    companion object {
        const val API_ERROR = "API_ERROR"
    }

    override fun onIncomingCall(model: AcceptCallModel) {
        activity?.runOnUiThread {
            userReceiver = userList.find { it.refId == model.from }

            startActivity(CallActivity.createIntent(requireContext(),
                userReceiver, false, false, model, null, (requireActivity() as BaseActivity).activeSessionId))


        }
    }

    override fun outGoingCall(toPeer: String) {
        activity?.let {
            it.runOnUiThread {
                openCallFragment(toPeer, isVideoCall)
            }
        }
    }

    override fun onAcceptIncomingCall(callParams: CallParams) {

        activity?.runOnUiThread {
            callParams.mcToken = prefs.loginInfo?.mcToken!!
            userReceiver = userList.find { it.refId == callParams.refId }

            startActivity(CallActivity.createIntent(requireContext(),
                userReceiver, false, true, null, callParams, (requireActivity() as BaseActivity).activeSessionId))

        }
    }

    override fun onCallMissed() {}

    override fun onCallRejected() {}

    override fun endOngoingCall(sessionId: String) {}

    override fun onAudioVideoStateChanged(audioState: Int, videoState: Int) {}

    override fun onInternetConnectionLoss() {}
    override fun onConnectionSuccess() {
        binding.tvLed.setImageResource(R.drawable.led_connected)
    }

    override fun onConnectionFail() {
        binding.tvLed.setImageResource(R.drawable.led_error)
    }

}




