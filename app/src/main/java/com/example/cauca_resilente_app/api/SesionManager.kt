package com.example.cauca_resilente_app.api

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object SessionManager {
    private const val PREFS_NAME = "cauca_session"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_JSON = "user_json"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun p(): SharedPreferences =
        prefs ?: throw IllegalStateException("SessionManager not initialized")

    fun saveSession(token: String, user: User) {
        p().edit()
            .putString(KEY_TOKEN, "Bearer $token")
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_JSON, gson.toJson(user))
            .apply()
    }

    fun saveToken(token: String) = p().edit().putString(KEY_TOKEN, "Bearer $token").apply()
    fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)

    fun saveUserId(userId: String) = p().edit().putString(KEY_USER_ID, userId).apply()
    fun getUserId(): String? = prefs?.getString(KEY_USER_ID, null)

    fun saveUser(user: User) {
        p().edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_JSON, gson.toJson(user))
            .apply()
    }

    fun getUser(): User? {
        val json = prefs?.getString(KEY_USER_JSON, null) ?: return null
        return runCatching { gson.fromJson(json, User::class.java) }.getOrNull()
    }

    fun logout() = p().edit().clear().apply()
    fun isLoggedIn(): Boolean = !getToken().isNullOrBlank()
}
