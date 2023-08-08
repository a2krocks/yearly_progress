package com.a3.yearlyprogess.manager

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.a3.yearlyprogess.manager.services.*
import java.lang.RuntimeException
import java.util.*

class AlarmHandler(private val context: Context, private val service : Int) {

    private val intent = when (service) {
        DAY_WIDGET_SERVICE ->  Intent(context, DayWidgetService::class.java)
        MONTH_WIDGET_SERVICE ->  Intent(context, MonthWidgetService::class.java)
        WEEK_WIDGET_SERVICE ->  Intent(context, WeekWidgetService::class.java)
        YEAR_WIDGET_SERVICE ->  Intent(context, YearWidgetService::class.java)
        EVENT_WIDGET_SERVICE -> Intent(context, EventWidgetService::class.java)
        ALL_IN_WIDGET_SERVICE -> Intent(context, AllInWidgetService::class.java)
        else -> Intent(context, DayWidgetService::class.java)
    }

    fun setAlarmManager() {
        val sender = PendingIntent.getBroadcast(context, service, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //get current time and add 2 seconds
        val c = Calendar.getInstance()
        val l = c.timeInMillis + 2000

        //set the alarm for 2 seconds in the future
        Log.d("ALARM_SET", service.toString())
        try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, l, sender)
        } catch (e: RuntimeException) {
            am.cancel(sender)
            Log.d("AM_FAILURE", e.stackTraceToString())
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, l, sender)
        }
        // am.setAlarmClock( AlarmManager.AlarmClockInfo(l,sender),sender)
    }

    fun cancelAlarmManager() {
        val sender: PendingIntent = PendingIntent.getBroadcast(context, service, intent, PendingIntent.FLAG_IMMUTABLE)
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(sender)
    }

    companion object {
        const val DAY_WIDGET_SERVICE = 500
        const val MONTH_WIDGET_SERVICE = 501
        const val WEEK_WIDGET_SERVICE = 502
        const val YEAR_WIDGET_SERVICE = 503
        const val EVENT_WIDGET_SERVICE = 504
        const val ALL_IN_WIDGET_SERVICE = 505
    }
}