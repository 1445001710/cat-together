package com.cat_together.meta

import android.app.Application
import com.cat_together.meta.database.AppDatabase
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.utils.SharedPreferencesHelper

class CatTogetherApp : Application() {

    companion object {
        private var instance: CatTogetherApp? = null

        fun getInstance(): CatTogetherApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }

        val database: AppDatabase by lazy {
            AppDatabase.getInstance(instance!!)
        }

        val sharedPreferencesHelper: SharedPreferencesHelper by lazy {
            SharedPreferencesHelper.getInstance(instance!!)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize SharedPreferences
        SharedPreferencesHelper.getInstance(this)

        // Initialize RetrofitClient
        RetrofitClient.init(this)
    }
}
