package com.norgic.vdotokcall.utils


/**
 * Created By: Norgic
 * Date & Time: On 5/5/21 At 5:06 PM in 2021
 */
object ApplicationConstants {

    const val API_BASE_URL: String = "https://tenant-api.vdotok.com/"
    const val SDK_AUTH_BASE_URL: String = "https://vtkapi.vdotok.com"
    const val API_VERSION: String = "v0/"

//    SDK AUTH PARAMS
    const val SDK_API_KEY: String = "3d9686b635b15b5bc2d19800407609fa"
    const val SDK_PROJECT_ID: String = "Add your own project key here"


    //    Prefs constants
    const val isLogin = "isLogin"
    const val LOGIN_INFO = "savedLoginInfo"
    const val GROUP_MODEL_KEY = "group_model_key"
    const val SDK_AUTH_RESPONSE = "SDK_AUTH_RESPONSE"

    const val CALL_PARAMS = "CALL_PARAMS"

//    API ERROR LOG TAGS
    const val API_ERROR = "API_ERROR"

    // This error code means a local error occurred while parsing the received json.
    const val HTTP_CODE_NO_NETWROK = 600

    const val MY_PERMISSIONS_REQUEST_CAMERA = 100
    const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101
    const val MY_PERMISSIONS_REQUEST = 102

}