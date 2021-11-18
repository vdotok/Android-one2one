package com.vdotok.network.network.api

import com.vdotok.network.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("API/v0/Login")
    suspend fun loginUser(@Body model: LoginUserModel): LoginResponse

    @POST("API/v0/SignUp")
    suspend fun signUp(@Body model: SignUpModel): LoginResponse

//    @POST("API/v0/CheckUsername")
//    suspend fun checkUserName(@Body model: CheckUserModel): Response<LoginResponse>

    @POST("API/v0/CheckEmail")
    suspend fun checkEmail(@Body model: EmailModel): LoginResponse

    @POST("API/v0/AllUsers")
    suspend fun getAllUsers(@Header("Authorization") auth_token: String): GetAllUsersResponseModel

}