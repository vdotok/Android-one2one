package com.norgic.vdotokcall.models

import com.google.gson.annotations.SerializedName

data class AuthenticationResponse(

    @SerializedName("message")
    val message: String,

    @SerializedName("media_server")
    var mediaServer: String,

    @SerializedName("media_server_map")
    var mediaServerMap: MediaServerMap,

    @SerializedName("messaging_server_map")
    val messagingServerMap: MessagingServerMap,

    @SerializedName("messaging_server")
    var messagingServer: String,

    @SerializedName("process_time")
    val processTime: Int,

    @SerializedName("status")
    val status: Int
)

data class MediaServerMap(
    @SerializedName("complete_address")
    var completeAddress: String,

    @SerializedName("end_point")
    val endPoint: String,

    @SerializedName("host")
    val host: String,

    @SerializedName("port")
    val port: String,

    @SerializedName("protocol")
    val protocol: String
)

data class MessagingServerMap(
    @SerializedName("complete_address")
    var completeAddress: String,

    @SerializedName("host")
    val host: String,

    @SerializedName("port")
    val port: String,

    @SerializedName("protocol")
    val protocol: String
)