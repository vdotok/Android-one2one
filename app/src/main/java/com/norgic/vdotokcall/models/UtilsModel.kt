package com.norgic.vdotokcall.models


/**
 * Created By: Norgic
 * Date & Time: On 8/16/21 At 1:51 PM in 2021
 */
object UtilsModel {

    internal fun updateServerUrls(userModel: LoginResponse): LoginResponse {
        if (userModel.mediaServerMap?.completeAddress?.contains("wss") == true)
            userModel.mediaServerMap?.apply {
                completeAddress.let {
                    completeAddress = it.replace("wss", "ssl")

                }
            }

        if (userModel.messagingServerMap?.completeAddress?.contains("ws") == true) {
            userModel.messagingServerMap.apply {
                completeAddress = completeAddress.replace("ws", "tcp")
            }
        }
        return userModel
    }
}