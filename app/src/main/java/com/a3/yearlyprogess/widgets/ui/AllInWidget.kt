package com.a3.yearlyprogess.widgets.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.SizeF
import android.view.View
import android.widget.RemoteViews
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.YearlyProgressManager.Companion.formatProgress
import com.a3.yearlyprogess.YearlyProgressManager
import com.a3.yearlyprogess.widgets.manager.updateManager.AlarmHandler
import com.a3.yearlyprogess.widgets.ui.util.BaseWidget
import kotlin.math.roundToInt

/**
 * Implementation of App Widget functionality.
 */
class AllInWidget : BaseWidget(AlarmHandler.ALL_IN_WIDGET_SERVICE) {

    companion object {

        private fun initiateView(context: Context, views: RemoteViews) {

            // Set default week and calculation mode
            YearlyProgressManager(context).setDefaultWeek()
            YearlyProgressManager(context).setDefaultCalculationMode()


            val dayProgress =
                YearlyProgressManager.getProgress(YearlyProgressManager.DAY).roundToInt()
            val weekProgress =
                YearlyProgressManager.getProgress(YearlyProgressManager.WEEK).roundToInt()
            val monthProgress =
                YearlyProgressManager.getProgress(YearlyProgressManager.MONTH).roundToInt()
            val yearProgress =
                YearlyProgressManager.getProgress(YearlyProgressManager.YEAR).roundToInt()



            views.setTextViewText(R.id.progressTextDay, formatProgress(dayProgress))
            views.setTextViewText(R.id.progressTextWeek, formatProgress(weekProgress))
            views.setTextViewText(R.id.progressTextMonth, formatProgress(monthProgress))
            views.setTextViewText(R.id.progressTextYear, formatProgress(yearProgress))

            views.setProgressBar(R.id.progressBarDay, 100, dayProgress, false)
            views.setProgressBar(R.id.progressBarWeek, 100, weekProgress, false)
            views.setProgressBar(R.id.progressBarMonth, 100, monthProgress, false)
            views.setProgressBar(R.id.progressBarYear, 100, yearProgress, false)


            views.setTextViewText(
                R.id.progressTitle,
                YearlyProgressManager.getDay(formatted = true)
            )
            views.setTextViewText(
                R.id.progressWeekTitle,
                YearlyProgressManager.getWeek(isLong = false)
            )
            views.setTextViewText(
                R.id.progressMonthTitle,
                YearlyProgressManager.getMonth(isLong = false)
            )
            views.setTextViewText(
                R.id.progressYearTitle,
                YearlyProgressManager.getYear().toString()
            )

            views.setOnClickPendingIntent(
                R.id.gridLayout, PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )


            views.setViewVisibility(R.id.testDay, View.VISIBLE)
            views.setViewVisibility(R.id.testWeek, View.VISIBLE)
            views.setViewVisibility(R.id.testMonth, View.VISIBLE)
            views.setViewVisibility(R.id.testYear, View.VISIBLE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                views.setInt(R.id.gridLayout, "setColumnCount", 4)

            }
        }


        fun AllInOneWidgetRemoteView(context: Context): RemoteViews {
            val xlarge = RemoteViews(context.packageName, R.layout.all_in_widget)
            initiateView(context, xlarge)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                return xlarge
            }


            val small = RemoteViews(context.packageName, R.layout.all_in_widget)
            val medium = RemoteViews(context.packageName, R.layout.all_in_widget)
            val large = RemoteViews(context.packageName, R.layout.all_in_widget)
            val square = RemoteViews(context.packageName, R.layout.all_in_widget)
            val tall = RemoteViews(context.packageName, R.layout.all_in_widget)

            initiateView(context, small)
            initiateView(context, medium)
            initiateView(context, large)
            initiateView(context, square)
            initiateView(context, tall)

            small.setViewVisibility(R.id.testWeek, View.GONE)
            small.setViewVisibility(R.id.testMonth, View.GONE)
            small.setViewVisibility(R.id.testYear, View.GONE)

            medium.setViewVisibility(R.id.testWeek, View.GONE)
            medium.setViewVisibility(R.id.testYear, View.GONE)

            large.setViewVisibility(R.id.testWeek, View.GONE)

            square.setInt(R.id.gridLayout, "setColumnCount", 2)
            tall.setInt(R.id.gridLayout, "setColumnCount", 1)


            // Instruct the widget manager to update the widget


            val viewMapping: Map<SizeF, RemoteViews> = mapOf(
                SizeF(300f, 80f) to xlarge,
                SizeF(220f, 80f) to large,
                SizeF(130f, 130f) to square,
                SizeF(102f, 276f) to tall,
                SizeF(160f, 80f) to medium,
                SizeF(100f, 80f) to small,
            )

            return RemoteViews(viewMapping)

        }
    }

    override fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {


        appWidgetManager.updateAppWidget(appWidgetId, AllInOneWidgetRemoteView(context))
    }


}



