package com.norgic.vdotokcall.feature.account.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.norgic.vdotokcall.R
import com.norgic.vdotokcall.databinding.LayoutFragmentLoginBinding
import com.norgic.vdotokcall.extensions.*
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity.Companion.createDashBoardActivity
import com.norgic.vdotokcall.models.LoginUserModel
import com.norgic.vdotokcall.network.HttpResponseCodes
import com.norgic.vdotokcall.network.RetrofitBuilder
import com.norgic.vdotokcall.prefs.Prefs
import com.norgic.vdotokcall.network.Result
import com.norgic.vdotokcall.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException


/**
 * Created By: Norgic
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class LoginFragment: Fragment() {

    private lateinit var binding: LayoutFragmentLoginBinding
    private var email : ObservableField<String> = ObservableField<String>()
    private var password : ObservableField<String> = ObservableField<String>()
    private lateinit var prefs: Prefs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = LayoutFragmentLoginBinding.inflate(inflater, container, false)

        binding.userEmail = email
        binding.password = password

        init()

        return binding.root
    }

    private fun init() {

        prefs = Prefs(activity)

        binding.signInBtn.setOnClickListener {  validateAndLogin()}

        binding.signUpBtn.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_move_to_signup_user)
        }

    }

    private fun validateAndLogin() {
        val inputText = email.get().toString()

        when {
            binding.root.checkInputTextIsEmailType(inputText) -> checkValidationForEmail()
            else -> checkValidationForUsername()
        }
    }

    private fun checkValidationForUsername() {
        if (binding.root.checkedPassword(password.get().toString()) && binding.root.checkedUserName(email.get().toString(), true)) {
            loginAction()
        }
    }

    private fun checkValidationForEmail() {
        val view = binding.signInBtn
        if (view.checkedPassword(password.get().toString()) && view.checkedEmail(email.get().toString(), true)) {
            loginAction()
        }
    }

    private fun loginAction(){
        loginUser(email.get().toString(), password.get().toString())
        binding.signInBtn.disable()
    }

    private fun loginUser(email: String, password: String) {
        activity?.let { it ->
            binding.progressBar.toggleVisibility()
            val service = RetrofitBuilder.makeRetrofitService(it)
            CoroutineScope(Dispatchers.IO).launch {
                val response = safeApiCall { service.loginUser(LoginUserModel(email, password)) }
                withContext(Dispatchers.Main) {
                    binding.signInBtn.enable()
                    try {

                        when {
                            response is Result.Success && response.data.status == HttpResponseCodes.SUCCESS.value -> {
                                prefs.loginInfo = response.data
                                startActivity(createDashBoardActivity(it))
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
                        Log.e(API_ERROR, "loginUser: ${e.printStackTrace()}")
                    } catch (e: Throwable) {
                        Log.e(API_ERROR, "loginUser: ${e.printStackTrace()}")
                    }
                    binding.progressBar.toggleVisibility()
                }
            }
        }
    }

    companion object {

        const val API_ERROR = "API_ERROR"

    }
}