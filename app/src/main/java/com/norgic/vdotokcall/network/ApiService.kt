package com.norgic.vdotokcall.network

import com.norgic.vdotokcall.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created By: Norgic
 * Date & Time: On 5/5/21 At 12:57 PM in 2021
 */
interface ApiService {

    @POST("API/v0/Login")
    suspend fun loginUser(@Body model: LoginUserModel): Response<LoginResponse>

    @POST("API/v0/SignUp")
    suspend fun signUp(@Body model: SignUpModel): Response<LoginResponse>

    @POST("API/v0/CheckUsername")
    suspend fun checkUserName(@Body model: CheckUserModel): Response<LoginResponse>

    @POST("API/v0/CheckEmail")
    suspend fun checkEmail(@Body model: CheckUserModel): Response<LoginResponse>

    @POST("API/v0/AllUsers")
    suspend fun getAllUsers(@Header("Authorization") auth_token: String): Response<GetAllUsersResponseModel>

}