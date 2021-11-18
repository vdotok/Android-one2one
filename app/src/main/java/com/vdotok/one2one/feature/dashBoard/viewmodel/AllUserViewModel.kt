package com.vdotok.one2one.feature.dashBoard.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.vdotok.network.di.module.RetrofitModule
import com.vdotok.network.models.LoginUserModel
import com.vdotok.network.network.Result
import com.vdotok.network.repository.AccountRepository
import com.vdotok.network.repository.UserListRepository
import com.vdotok.one2one.utils.ApplicationConstants


class AllUserViewModel: ViewModel() {

    fun getAllUsers(context: Context, token: String) = liveData {

            val service = RetrofitModule.provideRetrofitService()
            val repo = UserListRepository(service)
            emit(Result.Loading)
            emit(repo.getAllUsers(token))
    }

}