package com.vdotok.one2one.models

import android.os.Parcelable
import com.vdotok.streaming.enums.CallType
import com.vdotok.streaming.enums.MediaType
import com.vdotok.streaming.enums.SessionType
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AcceptCallModel(

    var from: String = "",

    var sessionUUID: String = "",

    var requestID: String = "",

    var deviceType: CallType,

    var mediaType: MediaType,

    var sessionType: SessionType

): Parcelable {
    companion object {
        const val TAG = "ACCEPT_CALL_MODEL"
    }
}