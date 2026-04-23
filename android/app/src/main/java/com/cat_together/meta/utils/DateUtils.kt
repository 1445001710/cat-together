package com.cat_together.meta.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    private const val MONTH_DAY_FORMAT = "MM月dd日"

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatMonthDay(timestamp: Long): String {
        val sdf = SimpleDateFormat(MONTH_DAY_FORMAT, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getTodayStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getTodayEnd(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun getWeekStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getMonthStart(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun parseDate(dateString: String): Long {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.parse(dateString)?.time ?: 0L
    }
}
