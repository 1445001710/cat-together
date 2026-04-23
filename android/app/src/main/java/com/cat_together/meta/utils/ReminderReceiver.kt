package com.cat_together.meta.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.cat_together.meta.R
import com.cat_together.meta.model.DietRecord
import com.cat_together.meta.ui.diet.DietFragment

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "feeding_reminder_channel"
        const val CHANNEL_NAME = "喂食提醒"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val reminderType = intent.getIntExtra("reminder_type", DietRecord.TYPE_FOOD)
        val catId = intent.getStringExtra("cat_id") ?: ""
        val catName = intent.getStringExtra("cat_name") ?: ""

        showNotification(context, reminderId, reminderType, catId, catName)
        vibrate(context)
        playSound(context)
    }

    private fun showNotification(context: Context, reminderId: String, type: Int, catId: String, catName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "猫咪喂食提醒通知"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 创建点击意图 - 跳转到饮食页面
        val mainIntent = Intent(context, com.cat_together.meta.ui.main.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("tab", "diet")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.hashCode(),
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val typeName = when (type) {
            DietRecord.TYPE_WATER -> "喝水"
            DietRecord.TYPE_FOOD -> "喂食"
            DietRecord.TYPE_SNACK -> "零食"
            DietRecord.TYPE_TREAT -> "猫条"
            else -> "提醒"
        }

        val displayCatName = if (catName.isNotEmpty()) catName else "猫咪"
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_diet)
            .setContentTitle("猫咪${typeName}时间到啦！")
            .setContentText("该给猫咪(${displayCatName})${typeName}了，点击查看详情")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(reminderId.hashCode(), notification)
    }

    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(1000)
        }
    }

    private fun playSound(context: Context) {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}