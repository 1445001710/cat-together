package com.cat_together.meta.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.cat_together.meta.ui.login.LoginActivity
import com.cat_together.meta.ui.main.MainActivity
import com.cat_together.meta.utils.SharedPreferencesHelper

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
            finish()
        }, SPLASH_DELAY)
    }

    private fun navigateToNextScreen() {
        val prefs = SharedPreferencesHelper.getInstance(this)
        if (prefs.isLoggedIn()) {
            // 已登录，进入主页
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // 未登录，进入登录页
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
