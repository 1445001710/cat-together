package com.cat_together.meta.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cat_together.meta.R
import com.cat_together.meta.databinding.ActivityLoginBinding
import com.cat_together.meta.network.RetrofitClient
import com.cat_together.meta.ui.main.MainActivity
import com.cat_together.meta.utils.ApiErrorHandler
import com.cat_together.meta.utils.SharedPreferencesHelper
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val prefs by lazy { SharedPreferencesHelper.getInstance(this) }
    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        // 默认显示登录界面文字
        binding.btnLogin.text = getString(R.string.login)
        binding.btnRegister.text = getString(R.string.register)

        binding.btnLogin.setOnClickListener {
            android.util.Log.d("LoginActivity", "btnLogin clicked, isRegisterMode=$isRegisterMode")
            if (isRegisterMode) {
                register()
            } else {
                login()
            }
        }

        binding.btnRegister.setOnClickListener {
            android.util.Log.d("LoginActivity", "btnRegister clicked")
            toggleMode()
        }

        // 自动填充上次登录的用户名
        val lastUsername = prefs.username
        if (lastUsername.isNotEmpty()) {
            binding.etUsername.setText(lastUsername)
        }
    }

    private fun toggleMode() {
        isRegisterMode = !isRegisterMode
        binding.tilNickname.visibility = if (isRegisterMode) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
        binding.etNickname.text?.clear()
        binding.btnLogin.text = if (isRegisterMode) {
            getString(R.string.register)
        } else {
            getString(R.string.login)
        }
        binding.btnRegister.text = if (isRegisterMode) {
            "返回登录"
        } else {
            getString(R.string.register)
        }
    }

    private fun login() {
        android.util.Log.d("LoginActivity", "login() called, isRegisterMode=$isRegisterMode")
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        android.util.Log.d("LoginActivity", "username=$username, password=$password")

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(
                    com.cat_together.meta.network.LoginRequest(username, password)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        val data = body.data
                        data?.let {
                            prefs.token = it.token
                            prefs.user = it.user
                            prefs.username = username

                            Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } ?: run {
                            Toast.makeText(this@LoginActivity, "登录数据异常", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, body?.message ?: "登录失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@LoginActivity, ApiErrorHandler.parseErrorBody(errorBody, "账号或密码错误"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                ApiErrorHandler.handle(this@LoginActivity, e, "登录失败")
            }
        }
    }

    private fun register() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val nickname = binding.etNickname.text.toString().trim()

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show()
            return
        }

        if (TextUtils.isEmpty(nickname)) {
            Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show()
            return
        }

        performRegister(username, password, nickname)
    }


    private fun performRegister(username: String, password: String, nickname: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.register(
                    com.cat_together.meta.network.RegisterRequest(username, password, nickname)
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.isSuccess == true) {
                        val data = body.data
                        data?.let {
                            prefs.token = it.token
                            prefs.user = it.user
                            prefs.username = username

                            Toast.makeText(this@LoginActivity, "注册成功", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        } ?: run {
                            Toast.makeText(this@LoginActivity, "注册数据异常", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, body?.message ?: "注册失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@LoginActivity, ApiErrorHandler.parseErrorBody(errorBody, "注册失败"), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                ApiErrorHandler.handle(this@LoginActivity, e, "注册失败")
            }
        }
    }
}