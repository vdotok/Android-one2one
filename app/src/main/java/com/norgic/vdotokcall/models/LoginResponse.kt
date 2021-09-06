package com.norgic.vdotokcall.models

import com.google.gson.annotations.SerializedName

/**
 * Created By: Norgic
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Response model map class getting the response after user has successfully logged in
 */
class LoginResponse {

    @SerializedName("min_android_verion")
    var minAndroidVersion: String? = null

    @SerializedName("min_ios_verion")
    var minIosVersion: String? = null

    @SerializedName("profile_pic")
    var profilePic: String? = null

    @SerializedName("active")
    var active: String? = null

    @SerializedName("last_name")
    var lastName: String? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("process_time")
    var processTime: String? = null

    @SerializedName("user_id")
    var userid: String? = null

    @SerializedName("client_id")
    var clientId: String? = null

    @SerializedName("block_users")
    var blockUsers: ArrayList<String> = ArrayList()

    @SerializedName("full_name")
    var fullName: String? = null

    @SerializedName("phone_num")
    var phoneNum: String? = null

    @SerializedName("auth_token")
    var authToken: String? = null

    @SerializedName("app_id")
    var appId: String? = null

    @SerializedName("first_name")
    var firstName: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("status")
    var status: String? = null

    @SerializedName("username")
    var userName: String? = null

    @SerializedName("ref_id")
    var refId: String? = null

    @SerializedName("authorization_token")
    var authorizationToken: String? = null

    var mcToken: String? = null

    @SerializedName("media_server")
    var mediaServer: String?= null

    @SerializedName("media_server_map")
    var mediaServerMap: MediaServerMap?= null

    @SerializedName("messaging_server_map")
    val messagingServerMap: MessagingServerMap?= null

    @SerializedName("messaging_server")
    var messagingServer: String?= null

}