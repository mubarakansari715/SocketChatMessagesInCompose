package com.example.socketchatmessagesincompose.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class SessionManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_session", Context.MODE_PRIVATE)

    fun getUserId(): String? = prefs.getString("user_id", null)

    fun getAuthToken(): String? = prefs.getString("auth_token", null)

    fun saveUserSession(userId: String, token: String) {
        prefs.edit().apply {
            putString("user_id", userId)
            putString("auth_token", token)
            apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}