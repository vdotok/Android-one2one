package com.vdotok.one2one.utils


/**
 * Created By: VdoTok
 * Date & Time: On 5/19/21 At 7:40 PM in 2021
 */
object TimeUtils {

    fun getTimeFromSeconds(seconds: Int): String {

        val hoursPassed = seconds / 3600
        return if(hoursPassed > 0){
            String.format("%02d:%02d:%02d", (seconds / 3600), ((seconds % 3600) / 60), (seconds % 60))
        } else{
            String.format("%02d:%02d", ((seconds % 3600) / 60), (seconds % 60))
        }

    }

}