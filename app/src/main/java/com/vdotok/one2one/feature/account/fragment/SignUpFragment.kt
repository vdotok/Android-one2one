package com.vdotok.one2one.feature.account.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import com.vdotok.network.models.LoginResponse
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.*
import com.vdotok.network.utils.Constants
import com.vdotok.one2one.QrCodeScannerContract
import com.vdotok.network.network.Result
import com.vdotok.one2one.R
import com.vdotok.one2one.databinding.LayoutFragmentSignupBinding
import com.vdotok.one2one.extensions.*
import com.vdotok.one2one.feature.account.viewmodel.AccountViewModel
import com.vdotok.one2one.models.QRCodeModel
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

        binding.scanner.performSingleClick {
            activity?.runOnUiThread {
                qrCodeScannerLauncher.launch(IntentIntegrator.forSupportFragment(this))
            }
        }

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
            if (!prefs.userProjectId.isNullOrEmpty() && !prefs.userBaseUrl.isNullOrEmpty()){
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
        }else{
                binding.root.showSnackBar("Kindly scan QR code to setup project")
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
        if (!prefs.userProjectId.isNullOrEmpty() &&   !prefs.userBaseUrl.isNullOrEmpty()){
            viewModel.signUp(
                SignUpModel(
                    username.get().toString(), email.get().toString(),
                    password.get().toString(), project_id = prefs.userProjectId.toString()
                )
            ).observe(viewLifecycleOwner) {

                when (it) {
                    Result.Loading -> {
                        binding.progressBar.toggleVisibility()
                    }
                    is Result.Success -> {
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
        }else{
            binding.root.showSnackBar("Kindly scan QR code to setup project")
        }

    }

    private val qrCodeScannerLauncher = registerForActivityResult(QrCodeScannerContract()){
        if (!it.contents.isNullOrEmpty()){
            Log.d("RESULT_INTENT", it.contents)
            val data: QRCodeModel? = Gson().fromJson(it.contents, QRCodeModel::class.java)
            prefs.userProjectId = data?.project_id.toString()
            prefs.userBaseUrl = data?.tenant_api_url.toString()
            if (!prefs.userProjectId.isNullOrEmpty() &&   !prefs.userBaseUrl.isNullOrEmpty()){
                SDK_PROJECT_ID = prefs.userProjectId.toString()
                Constants.BASE_URL =  prefs.userBaseUrl.toString()
            }
            Log.d("RESULT_INTENT",data.toString())
        }else{
            binding.root.showSnackBar("QR CODE is not correct!!!")
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