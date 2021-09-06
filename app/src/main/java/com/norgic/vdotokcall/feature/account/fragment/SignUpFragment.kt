package com.norgic.vdotokcall.feature.account.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.LayoutFragmentSignupBinding
import com.norgic.vdotokcall.extensions.*
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity
import com.norgic.vdotokcall.models.CheckUserModel
import com.norgic.vdotokcall.models.LoginResponse
import com.norgic.vdotokcall.models.SignUpModel
import com.norgic.vdotokcall.models.UtilsModel
import com.norgic.vdotokcall.network.HttpResponseCodes
import com.norgic.vdotokcall.network.Result
import com.norgic.vdotokcall.network.RetrofitBuilder
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.utils.disable
import com.norgic.vdotokcall.utils.enable
import com.norgic.vdotokcall.utils.isInternetAvailable
import com.norgic.vdotokcall.utils.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response


/**
 * Created By: Norgic
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class SignUpFragment: Fragment() {

    private lateinit var binding: LayoutFragmentSignupBinding
    private var email : ObservableField<String> = ObservableField<String>()
    private var username : ObservableField<String> = ObservableField<String>()
    private var password : ObservableField<String> = ObservableField<String>()

    private lateinit var prefs: Prefs


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutFragmentSignupBinding.inflate(inflater, container, false)

        binding.userEmail = email
        binding.username = username
        binding.password = password

        init()

        return binding.root
    }

    private fun init() {

        prefs = Prefs(activity)

        binding.btnSignUp.setOnClickListener {
            if (it.checkedUserName(username.get().toString(), true) &&
                it.checkedEmail(email.get().toString(), true) &&
                it.checkedPassword(password.get().toString())
            ) {
                checkUserEmail(email.get().toString())
                binding.btnSignUp.disable()
            }
        }

       binding.tvSignIn.setOnClickListener {
           moveToLogin(it)
        }

        configureBackPress()
    }

    private fun checkUserEmail(email: String) {
        activity?.let {
            binding.progressBar.toggleVisibility()
            val service = RetrofitBuilder.makeRetrofitService(it)
            CoroutineScope(Dispatchers.IO).launch {
                val response = safeApiCall { service.checkEmail(CheckUserModel(email))}
                withContext(Dispatchers.Main) {
                    binding.btnSignUp.enable()
                    try {

                        when {
                            response is Result.Success && response.data.status == HttpResponseCodes.SUCCESS.value -> {
                                handleCheckFullNameResponse(response.data)
                            }
                            response is Result.Error -> {
                                if (isInternetAvailable(it as Context).not())
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                else
                                    binding.root.showSnackBar(response.error.message)
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
    }

    private fun handleCheckFullNameResponse(response: LoginResponse) {
        when(response.status) {
            HttpResponseCodes.SUCCESS.value -> signUp()
            else -> binding.root.showSnackBar(response.message)
        }
    }


    private fun signUp() {
        binding.btnSignUp.disable()
        activity?.let {
            binding.progressBar.toggleVisibility()
            val service = RetrofitBuilder.makeRetrofitService(it)
            CoroutineScope(Dispatchers.IO).launch {
                val response = safeApiCall { service.signUp(SignUpModel(username.get().toString(), email.get().toString(),
                    password.get().toString()))}

                withContext(Dispatchers.Main) {
                    binding.btnSignUp.enable()
                    try {
                        when {
                            response is Result.Success && response.data.status == HttpResponseCodes.SUCCESS.value -> {
                                handleSignUpResponse(response.data)
                            }
                            response is Result.Error -> {
                                if (isInternetAvailable(it as Context).not())
                                    binding.root.showSnackBar(getString(R.string.no_network_available))
                                else
                                    binding.root.showSnackBar(response.error.message)
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
    }

    private fun handleSignUpResponse(loginResponse: LoginResponse) {
        when(loginResponse.status) {
            HttpResponseCodes.SUCCESS.value -> {
                prefs.loginInfo = UtilsModel.updateServerUrls(loginResponse)
                activity?.let { startActivity(DashBoardActivity.createDashBoardActivity(it)) }
            }
            else -> {
                binding.root.showSnackBar(loginResponse.message)
            }
        }
    }

    private fun moveToLogin(view: View) {
        Navigation.findNavController(view).navigate(R.id.action_move_to_login_user)
    }

    private fun configureBackPress() {
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object: OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    moveToLogin(binding.root)
                }
            })
    }

    companion object {
        const val API_ERROR = "API_ERROR"
    }
}