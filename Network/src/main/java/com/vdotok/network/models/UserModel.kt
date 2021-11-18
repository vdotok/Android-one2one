package com.vdotok.network.models

import android.os.Parcelable
import android.text.TextUtils
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created By: VdoTok
 * Date & Time: On 1/21/21 At 1:17 PM in 2021
 *
 * Request model map class to send that a user is selected to form a group
 */
@Parcelize
data class UserModel (

    @SerializedName("user_id")
    var userId: String? = null,
    @SerializedName("email")
    var email: String? = null,
    @SerializedName("full_name")
    var fullName: String? = null,
    @SerializedName("ref_id")
    var refId: String? = null,

    var isSelected: Boolean = false
) : Parcelable {

    val userName: String?
        get() {
            return if (!TextUtils.isEmpty(fullName)) {
                fullName
            } else
                email
        }

    companion object{
        const val TAG = "USER_MODEL"
    }
}