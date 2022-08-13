package com.a3.yearlyprogess.manager

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.format.DateFormat
import android.text.format.DateFormat.is24HourFormat
import androidx.appcompat.app.AppCompatActivity
import com.a3.yearlyprogess.databinding.ActivityEventConfigActivityBinding
import com.a3.yearlyprogess.mwidgets.updateEventWidget
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.*

class EventConfigActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventConfigActivityBinding

    private var eventStartDateTimeInMillis: Long = -1
    private var eventStartHour: Int = 0
    private var eventStartMinute: Int = 0


    private var eventEndDateTimeInMillis: Long = -1
    private var eventEndHour: Int = 0
    private var eventEndMinute: Int = 0

    private val DATE_FORMAT = "MMMM dd, YYYY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEventConfigActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setResult(Activity.RESULT_CANCELED)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val datePicker = MaterialDatePicker.Builder.datePicker()
        val isSystem24Hour = is24HourFormat(this)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
        val timePicker = MaterialTimePicker.Builder().setTimeFormat(clockFormat)

        val pref = this.getSharedPreferences(appWidgetId.toString(), Context.MODE_PRIVATE)
        val edit = pref.edit()

        loadWidgetDataIfExists(pref)



        binding.editTextStartDate.setOnClickListener {
            datePicker.setTitleText("Select Event Start Date")
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {


                        eventStartDateTimeInMillis = it.toLong()
                        eventStartDateTimeInMillis = modifiedEventDateTime(
                            eventStartDateTimeInMillis,
                            eventEndHour,
                            eventEndMinute
                        )

                        binding.editTextStartDate.text =
                            SimpleDateFormat(DATE_FORMAT).format(eventStartDateTimeInMillis)
                                .toString()
                    }
                }


        }
        binding.editTextStartTime.setOnClickListener {
            timePicker.setTitleText("Select Event Start Time")
                .setHour(eventStartHour)
                .setMinute(eventStartMinute)
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {
                        eventStartHour = hour
                        eventStartMinute = minute

                        eventStartDateTimeInMillis = modifiedEventDateTime(
                            eventStartDateTimeInMillis,
                            eventStartHour,
                            eventStartMinute
                        )

                        binding.editTextStartTime.text =
                            getHourMinuteLocal(eventStartDateTimeInMillis)
                        binding.editTextStartDate.text =
                            SimpleDateFormat(DATE_FORMAT).format(eventStartDateTimeInMillis)
                                .toString()

                    }
                }

        }
        binding.editTextEndDate.setOnClickListener {
            datePicker.setTitleText("Select Event End Date")
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {


                        eventEndDateTimeInMillis = it.toLong()
                        eventEndDateTimeInMillis = modifiedEventDateTime(
                            eventEndDateTimeInMillis,
                            eventEndHour,
                            eventEndMinute
                        )

                        binding.editTextEndDate.text =
                            SimpleDateFormat(DATE_FORMAT).format(eventEndDateTimeInMillis)
                                .toString()
                    }
                }


        }
        binding.editTextEndTime.setOnClickListener {
            timePicker.setTitleText("Select Event End Time")
                .setHour(eventEndHour)
                .setMinute(eventEndMinute)
                .build().apply {
                    show(supportFragmentManager, "tag")
                    addOnPositiveButtonClickListener {
                        eventEndHour = hour
                        eventEndMinute = minute

                        eventEndDateTimeInMillis = modifiedEventDateTime(
                            eventEndDateTimeInMillis,
                            eventEndHour,
                            eventEndMinute
                        )

                        binding.editTextEndTime.text = getHourMinuteLocal(eventEndDateTimeInMillis)
                        binding.editTextEndDate.text =
                            SimpleDateFormat(DATE_FORMAT).format(eventEndDateTimeInMillis)
                                .toString()

                    }
                }

        }


        binding.btnCancel.setOnClickListener {
            finish()
        }
        binding.btnSave.setOnClickListener {

            val eventTitle = binding.eventTitle.text.toString().ifEmpty { "" }
            val eventDesc = binding.eventDesc.text.toString().ifEmpty { "" }

            edit.putString("eventTitle", eventTitle)
            edit.putString("eventDesc", eventDesc)
            edit.putLong("eventStartTimeInMills", eventStartDateTimeInMillis)
            edit.putLong("eventEndDateTimeInMillis", eventEndDateTimeInMillis)

            edit.commit()

            updateEventWidget(this, appWidgetManager, appWidgetId)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    private fun getHourMinuteLocal(time: Long): String {
        return if (is24HourFormat(this)) SimpleDateFormat("HH:mm").format(
            time
        ) else SimpleDateFormat("hh:mm a").format(time)
    }

    private fun loadWidgetDataIfExists(pref: SharedPreferences) {
        val eventTitle = pref.getString("eventTitle", "").toString()
        val eventDesc = pref.getString("eventDesc", "").toString()
        eventStartDateTimeInMillis = pref.getLong("eventStartTimeInMills", 0)
        eventEndDateTimeInMillis = pref.getLong("eventEndDateTimeInMillis", 0)


        binding.eventTitle.setText(eventTitle)
        binding.eventDesc.setText(eventDesc)

        val localCalendar = Calendar.getInstance()


        localCalendar.timeInMillis = eventStartDateTimeInMillis
        eventStartHour = localCalendar.get(Calendar.HOUR_OF_DAY)
        eventStartMinute = localCalendar.get(Calendar.MINUTE)


        binding.editTextStartDate.text =
            SimpleDateFormat(DATE_FORMAT).format(eventStartDateTimeInMillis).toString()
        binding.editTextStartTime.text = getHourMinuteLocal(eventStartDateTimeInMillis)

        localCalendar.timeInMillis = eventEndDateTimeInMillis
        eventEndHour = localCalendar.get(Calendar.HOUR_OF_DAY)
        eventEndMinute = localCalendar.get(Calendar.MINUTE)

        binding.editTextEndDate.text =
            SimpleDateFormat(DATE_FORMAT).format(eventEndDateTimeInMillis).toString()
        binding.editTextEndTime.text = getHourMinuteLocal(eventEndDateTimeInMillis)
    }

    private fun modifiedEventDateTime(date: Long, hour: Int, min: Int): Long {
        val localCalendar = Calendar.getInstance()
        localCalendar.timeInMillis = date
        localCalendar.set(Calendar.MINUTE, min)
        localCalendar.set(Calendar.HOUR_OF_DAY, hour)

        return localCalendar.timeInMillis
    }
}