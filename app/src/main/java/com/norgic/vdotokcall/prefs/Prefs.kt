package com.norgic.vdotokcall.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.norgic.vdotokcall.models.AuthenticationResponse
import com.norgic.vdotokcall.models.LoginResponse
import com.norgic.vdotokcall.models.UserModel
import com.norgic.vdotokcall.utils.ApplicationConstants

/**
 * Created By: Norgic
 * Date & Time: On 1/20/21 At 3:31 PM in 2021
 *
 * This class is mainly used to locally store and use data in the application
 * @param context the context of the application or the activity from where it is called
 */
class Prefs(context: Context?) {

    private val REGISTERED_USER_INFO = "REGISTERED_USER_INFO"
    private val mPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    var userRegisterInfo: UserModel?
        get(){
            val gson = Gson()
            val json = mPrefs.getString(REGISTERED_USER_INFO, "")
            return gson.fromJson(json, UserModel::class.java)
        }
        set(userInfo) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            val gson = Gson()
            val json = gson.toJson(userInfo)
            mEditor.putString(REGISTERED_USER_INFO, json)
            mEditor.apply()
        }


    var loginInfo: LoginResponse?
        get(){
            val gson = Gson()
            val json = mPrefs.getString(ApplicationConstants.LOGIN_INFO, "")
            return gson.fromJson(json, LoginResponse::class.java)
        }
        set(loginObject) {
            val mEditor: SharedPreferences.Editor = mPrefs.edit()
            val gson = Gson()
            val json = gson.toJson(loginObject)
            mEditor.putString(ApplicationConstants.LOGIN_INFO, json)
            mEditor.apply()
        }

    /**
     * Function to save a list of any type in prefs
     * */
    fun <T> setList(key: String?, list: List<T>?) {
        val gson = Gson()
        val json = gson.toJson(list)
        set(key, json)
    }

    /**
     * Function to save a simple key value pair in prefs
     * */
    operator fun set(key: String?, value: String?) {
        val prefsEditor: SharedPreferences.Editor = mPrefs.edit()
        prefsEditor.putString(key, value)
        prefsEditor.apply()
    }

    /**
     * Function to clear all prefs from storage
     * */
    fun clearAll(){
        mPrefs.edit().clear().apply()
    }

    /**
     * Function to delete a specific prefs value from storage
     * */
    fun deleteKeyValuePair(key: String?) {
        mPrefs.edit().remove(key).apply()
    }
}