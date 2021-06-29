package com.norgic.vdotokcall.models

import android.os.Parcelable
import com.norgic.callsdk.enums.CallType
import com.norgic.callsdk.enums.MediaType
import com.norgic.callsdk.enums.SessionType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AcceptCallModel(

    var from: String = "",

    var sessionUUID: String = "",

    var requestID: String = "",

    var deviceType: CallType,

    var mediaType: MediaType,

    var sessionType: SessionType

): Parcelable{
    companion object{
        const val TAG = "ACCEPT_CALL_MODEL"
    }
}