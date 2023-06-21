package com.vdotok.network.network.api

import com.vdotok.network.models.*
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("Login")
    suspend fun loginUser(@Body model: LoginUserModel): LoginResponse

    @POST("SignUp")
    suspend fun signUp(@Body model: SignUpModel): LoginResponse

//    @POST("CheckUsername")
//    suspend fun checkUserName(@Body model: CheckUserModel): Response<LoginResponse>

    @POST("CheckEmail")
    suspend fun checkEmail(@Body model: EmailModel): LoginResponse

    @POST("AllUsers")
    suspend fun getAllUsers(@Header("Authorization") auth_token: String): GetAllUsersResponseModel

}