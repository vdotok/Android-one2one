package com.vdotok.one2one.feature.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.vdotok.network.network.Result
import com.vdotok.one2one.R
import com.vdotok.one2one.databinding.LayoutFragmentLoginBinding
import com.vdotok.one2one.extensions.*
import com.vdotok.one2one.feature.account.viewmodel.AccountViewModel
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.one2one.utils.disable
import com.vdotok.one2one.utils.enable
import com.vdotok.one2one.utils.handleLoginResponse
import com.vdotok.one2one.utils.isInternetAvailable


/**
 * Created By: VdoTok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class LoginFragment: Fragment() {

    private lateinit var binding: LayoutFragmentLoginBinding
    private var email : ObservableField<String> = ObservableField<String>()
    private var password : ObservableField<String> = ObservableField<String>()
    private lateinit var prefs: Prefs
    private val viewModel: AccountViewModel by viewModels()


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

            viewModel.loginUser(email, password).observe(viewLifecycleOwner) {
                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        binding.progressBar.toggleVisibility()
                        handleLoginResponse(requireContext(), it.data, prefs, binding.root)
                        binding.signInBtn.enable()
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if (isInternetAvailable(this@LoginFragment.requireContext()).not())
                            binding.root.showSnackBar(getString(R.string.no_network_available))
                        else
                            binding.root.showSnackBar(it.exception.message)

                        binding.signInBtn.enable()
                    }
                }
            }

        }
    }

//    private fun handleLoginResponse(response: LoginResponse) {
//        when(response.status) {
//            HttpResponseCodes.SUCCESS.value -> {
//                saveResponseToPrefs(prefs, response)
//                startActivity(createDashBoardActivity(requireContext()))
//            }
//            else -> {
//                binding.root.showSnackBar(response.message)
//            }
//        }
//    }


}