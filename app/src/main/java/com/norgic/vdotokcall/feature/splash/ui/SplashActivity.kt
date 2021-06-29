package com.norgic.vdotokcall.feature.splash.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.ActivitySplashBinding
import com.norgic.vdotokcall.extensions.showSnackBar
import com.norgic.vdotokcall.extensions.toggleVisibility
import com.norgic.vdotokcall.models.AuthenticationRequest
import com.norgic.vdotokcall.network.RetrofitBuilder
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.network.Result
import com.norgic.vdotokcall.feature.account.ui.AccountActivity.Companion.createAccountsActivity
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity.Companion.createDashBoardActivity
import com.norgic.vdotokcall.network.HttpResponseCodes.*
import com.norgic.vdotokcall.utils.*
import com.norgic.vdotokcall.utils.ApplicationConstants.SDK_API_KEY
import com.norgic.vdotokcall.utils.ApplicationConstants.SDK_PROJECT_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    private fun init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        prefs = Prefs(this)

        initSdkAuth()
    }

    private fun initSdkAuth() {
        binding.progressBar.toggleVisibility()
        val service = RetrofitBuilder.makeSdkAuthRetrofitService(this)
        CoroutineScope(Dispatchers.IO).launch {
            val response = safeApiCall { service.authSDK (
                AuthenticationRequest(
                    SDK_API_KEY,
                    SDK_PROJECT_ID)
            ) }

            withContext(Dispatchers.Main) {
                try {
                    when{
                        response is Result.Success && response.data.status == SUCCESS.valueInInt -> {
                            prefs.sdkAuthResponse = response.data
                            manageServerUrls()
                            performAuthOperations()
                        }
                        response is Result.Error -> {
                            when {
                                isInternetAvailable(this@SplashActivity).not() -> {
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                }
                                else -> binding.root.showSnackBar(response.error.message)
                            }
                            finishAfterDelay()
                        }

                        else -> binding.root.showSnackBar(response.getDataOrNull()?.message)
                    }
                } catch (e: HttpException) {
                    Log.e(API_ERROR, "signUpUser: ${e.printStackTrace()}")
                } catch (e: Throwable) {
                    Log.e(API_ERROR, "signUpUser: ${e.printStackTrace()}")
                }
                binding.progressBar.toggleVisibility()
            }
        }
    }

    private fun finishAfterDelay(){
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 2000)
    }

    private fun manageServerUrls() {
        val value = prefs.sdkAuthResponse
        if (value?.mediaServerMap?.completeAddress?.contains("wss") == true)
            value.mediaServerMap.apply {
                completeAddress = completeAddress.replace("wss", "ssl")
            }
        if (value?.messagingServerMap?.completeAddress?.contains("ws") == true) {
            value.messagingServerMap.apply {
                completeAddress = completeAddress.replace("ws", "tcp")
            }
        }
        prefs.sdkAuthResponse = value
    }

    private fun performAuthOperations() {
        prefs.loginInfo?.let {
            startActivity(createDashBoardActivity(this))
//            startActivity(UserListCallActivity.createUserListActivity(this))
            finish()
        }?: kotlin.run {
            moveToAccountsActivity()
        }
    }

    private fun moveToAccountsActivity() {
        startActivity(createAccountsActivity(this))
        finish()
    }

    companion object {

        const val API_ERROR = "API_ERROR"

    }
}
