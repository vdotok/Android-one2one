package com.vdotok.one2one.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthenticationRequest(

    @SerializedName("auth_token")
    var auth_token: String,

    @SerializedName("project_id")
    var project_id: String

): Parcelable