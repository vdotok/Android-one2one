package com.vdotok.one2one.utils


/**
 * Created By: VdoTok
 * Date & Time: On 5/5/21 At 5:06 PM in 2021
 */
object ApplicationConstants {

    const val API_BASE_URL: String = "https://tenant-api.vdotok.dev/"
    const val API_VERSION: String = "v0/"

//    SDK AUTH PARAMS
    const val SDK_PROJECT_ID: String = "15Q89R"


    //    Prefs constants
    const val LOGIN_INFO = "savedLoginInfo"

    const val CALL_PARAMS = "CALL_PARAMS"

//    API ERROR LOG TAGS
    const val API_ERROR = "API_ERROR"

    // This error code means a local error occurred while parsing the received json.
    const val HTTP_CODE_NO_NETWROK = 600

    const val MY_PERMISSIONS_REQUEST_CAMERA = 100
    const val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 101
    const val MY_PERMISSIONS_REQUEST = 102

}