package com.vdotok.one2one.feature.account.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.*
import com.vdotok.one2one.R
import com.vdotok.one2one.databinding.LayoutFragmentSignupBinding
import com.vdotok.one2one.extensions.*
import com.vdotok.one2one.feature.account.viewmodel.AccountViewModel
import com.vdotok.one2one.network.HttpResponseCodes
import com.vdotok.one2one.prefs.Prefs
import com.vdotok.one2one.utils.*
import com.vdotok.one2one.utils.ApplicationConstants.SDK_PROJECT_ID


/**
 * Created By: VdoTok
 * Date & Time: On 5/3/21 At 1:26 PM in 2021
 */
class SignUpFragment: Fragment() {

    private lateinit var binding: LayoutFragmentSignupBinding
    private var email : ObservableField<String> = ObservableField<String>()
    private var username : ObservableField<String> = ObservableField<String>()
    private var password : ObservableField<String> = ObservableField<String>()

    private lateinit var prefs: Prefs
    private val viewModel: AccountViewModel by viewModels()


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

            viewModel.checkEmailAlreadyExist(email).observe(viewLifecycleOwner) {

                when (it) {
                    is Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success ->  {
                        binding.progressBar.toggleVisibility()
                        handleCheckFullNameResponse(it.data)
                        binding.btnSignUp.enable()
                    }
                    is Result.Failure -> {
                        binding.progressBar.toggleVisibility()
                        if (isInternetAvailable(this@SignUpFragment.requireContext()).not())
                            binding.root.showSnackBar(getString(R.string.no_network_available))
                        else
                            binding.root.showSnackBar(it.exception.message)
                        binding.btnSignUp.enable()
                    }
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

        viewModel.signUp(SignUpModel(username.get().toString(), email.get().toString(),
            password.get().toString(),project_id = SDK_PROJECT_ID)).observe(viewLifecycleOwner) {

            when (it) {
                Result.Loading -> {
                    binding.progressBar.toggleVisibility()
                }
                is Result.Success ->  {
                    binding.progressBar.toggleVisibility()
                    handleLoginResponse(requireContext(), it.data, prefs, binding.root)
                    binding.btnSignUp.enable()
                }
                is Result.Failure -> {
                    binding.progressBar.toggleVisibility()
                    if (isInternetAvailable(this@SignUpFragment.requireContext()).not())
                        binding.root.showSnackBar(getString(R.string.no_network_available))
                    else
                        binding.root.showSnackBar(it.exception.message)

                    binding.btnSignUp.enable()
                }
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