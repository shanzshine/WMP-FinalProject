package com.example.final_wmp.Helper

import android.content.Context
import android.preference.PreferenceManager
import com.example.final_wmp.Domain.ItemsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TinyDB(context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun putListObject(key: String, playerList: ArrayList<ItemsModel>) {
        val gson = Gson()
        val json = gson.toJson(playerList)
        preferences.edit().putString(key, json).apply()
    }

    fun getListObject(key: String): ArrayList<ItemsModel> {
        val gson = Gson()
        val json = preferences.getString(key, "")

        if (json.isNullOrEmpty()) {
            return ArrayList()
        }

        val type = object : TypeToken<ArrayList<ItemsModel>>() {}.type
        return gson.fromJson(json, type)
    }

    fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    fun getString(key: String): String {
        return preferences.getString(key, "") ?: ""
    }

    fun clear() {
        preferences.edit().clear().apply()
    }
}