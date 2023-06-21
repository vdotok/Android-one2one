package com.vdotok.one2one.feature.account.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.EmailModel
import com.vdotok.network.models.LoginUserModel
import com.vdotok.network.models.SignUpModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.AccountRepository


class AccountViewModel: ViewModel() {

    fun loginUser(email: String, password: String,projectId :String) = liveData {
            val service = RetrofitModule.provideRetrofitService()
            val repo = AccountRepository(service)
            emit(Result.Loading)
            emit(repo.login(LoginUserModel(email, password,projectId)))
    }


    fun checkEmailAlreadyExist(email: String) = liveData {
        val service = RetrofitModule.provideRetrofitService()
        val repo = AccountRepository(service)
        emit(Result.Loading)
        emit(repo.emailAlreadyExist(EmailModel(email)))
    }

    fun signUp(signup: SignUpModel) = liveData {

        val service = RetrofitModule.provideRetrofitService()
        val repo = AccountRepository(service)
        emit(Result.Loading)
        emit(repo.signUp(signup))
    }

}