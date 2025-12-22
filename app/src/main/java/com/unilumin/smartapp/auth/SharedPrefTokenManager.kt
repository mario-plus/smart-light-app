package com.unilumin.smartapp.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.unilumin.smartapp.constant.SharedPrefKeys


class SharedPrefTokenManager(context: Context?) : TokenManagerService {

    var sharedPreferences: SharedPreferences? =
        context!!.getSharedPreferences(SharedPrefKeys.ACCESS_TOKEN_PREF, Context.MODE_PRIVATE);

    override fun getAccessTokenTime(): Long {
        return sharedPreferences!!.getLong(SharedPrefKeys.ACCESS_TOKEN_TIME, 0L)
    }

    override fun setAccessToken(token: String) {
        sharedPreferences!!.edit {
            putString(SharedPrefKeys.ACCESS_TOKEN_KEY, token)
            putLong(SharedPrefKeys.ACCESS_TOKEN_TIME, System.currentTimeMillis())
        }
    }

    override fun getAccessToken(): String {
        return sharedPreferences!!.getString(SharedPrefKeys.ACCESS_TOKEN_KEY, null).toString()
    }

    override fun clear() {
        sharedPreferences!!.edit { remove(SharedPrefKeys.ACCESS_TOKEN_KEY) }
    }


}
