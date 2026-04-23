package com.cat_together.meta.ui.main

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cat_together.meta.R
import com.cat_together.meta.databinding.ActivityMainBinding
import com.cat_together.meta.ui.ai.ChatActivity
import com.cat_together.meta.ui.diet.DietFragment
import com.cat_together.meta.ui.health.HealthFragment
import com.cat_together.meta.ui.home.HomeFragment
import com.cat_together.meta.ui.profile.ProfileFragment
import com.cat_together.meta.ui.social.SocialFragment
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())

    // 状态: 0=左到右, 1=右到中坐, 2=中到左, 3=左到右然后直接回1
    private var state = 0
    private var isWalking = false
    private var pendingRunnable: Runnable? = null

    private val walkDrawable1 = R.drawable.img_cat_walk1
    private val walkDrawable2 = R.drawable.img_cat_walk2

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupBottomNav()
            setupTopNav()

            if (savedInstanceState == null) {
                replaceFragment(HomeFragment())
            }

            handler.postDelayed({
                initCatAndStart()
            }, 1200)
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("MainActivity", "Error in onCreate", e)
            finish()
        }
    }

    private fun initCatAndStart() {
        val catView = binding.ivCatWalk
        val padding = 60f
        catView.x = padding  // 从左边开始
        catView.scaleX = 1f // 朝右
        state = 0
        startCatLoop()
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_health -> replaceFragment(HealthFragment())
                R.id.nav_diet -> replaceFragment(DietFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                R.id.nav_social -> replaceFragment(SocialFragment())
            }
            true
        }
    }

    private fun setupTopNav() {
        binding.ivAI.setOnClickListener {
            openAIChat()
        }
    }

    private fun openAIChat() {
        val intent = Intent(this, ChatActivity::class.java)
        startActivity(intent)
    }

    private fun startCatLoop() {
        if (isWalking) return

        val containerWidth = binding.headerContainer.width.toFloat()
        val catView = binding.ivCatWalk
        val catWidth = catView.width.toFloat()
        if (catWidth <= 0 || containerWidth <= 0) return

        val padding = 60f
        val leftEdge = padding
        val rightEdge = containerWidth - catWidth - padding
        val middleEdge = leftEdge + (rightEdge - leftEdge) * 0.5f

        when (state) {
            0 -> {
                catView.scaleX = 1f
                walkTo(catView, rightEdge, 50) {
                    state = 1
                    isWalking = false
                    pendingRunnable = Runnable { startCatLoop() }
                    handler.postDelayed(pendingRunnable!!, 100)
                }
            }
            1 -> {
                catView.scaleX = -1f
                catView.x = rightEdge
                walkTo(catView, middleEdge, 50) {
                    catView.setImageResource(R.drawable.img_cat_lick)
                    pendingRunnable = Runnable {
                        state = 2
                        isWalking = false
                        startCatLoop()
                    }
                    handler.postDelayed(pendingRunnable!!, 3000)
                }
            }
            2 -> {
                catView.scaleX = -1f
                walkTo(catView, leftEdge, 50) {
                    state = 3
                    isWalking = false
                    pendingRunnable = Runnable { startCatLoop() }
                    handler.postDelayed(pendingRunnable!!, 100)
                }
            }
            3 -> {
                catView.scaleX = 1f
                walkTo(catView, rightEdge, 50) {
                    state = 1  // 直接回状态1循环
                    isWalking = false
                    pendingRunnable = Runnable { startCatLoop() }
                    handler.postDelayed(pendingRunnable!!, 100)
                }
            }
        }
    }

    private fun walkTo(catView: ImageView, targetX: Float, stepMs: Long, onComplete: () -> Unit) {
        isWalking = true
        val startX = catView.x
        val distance = targetX - startX
        val steps = (kotlin.math.abs(distance) / 5f).toInt().coerceAtLeast(6)
        val stepDistance = distance / steps
        var step = 0

        val runnable = object : Runnable {
            override fun run() {
                if (!isWalking) return  // 被中断了
                if (step < steps) {
                    catView.setImageResource(if (step % 2 == 0) walkDrawable1 else walkDrawable2)
                    catView.translationY = if (step % 2 == 0) -3f else 0f
                    catView.x = startX + stepDistance * step
                    step++
                    handler.postDelayed(this, stepMs)
                } else {
                    catView.x = targetX
                    catView.translationY = 0f
                    isWalking = false
                    onComplete()
                }
            }
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        isWalking = false
        pendingRunnable?.let { handler.removeCallbacks(it) }
        handler.removeCallbacksAndMessages(null)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
