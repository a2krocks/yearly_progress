package com.a3.yearlyprogess.widgets.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.text.SpannableString
import android.util.SizeF
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.*
import com.a3.yearlyprogess.widgets.ui.StandaloneWidgetOptions.Companion.WidgetShape
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod
import com.a3.yearlyprogess.widgets.ui.util.toTimePeriodLeftText
import kotlin.math.roundToInt

/** Utility object for creating and managing widget RemoteViews. */
object WidgetUtils {

  /**
   * Creates a RemoteViews object for the widget.
   *
   * @param context The context of the application.
   * @param widgetType The type of the widget.
   * @param startTime The start time of the period.
   * @param endTime The end time of the period.
   * @param currentValue The current value to display in the widget.
   * @param errorMessage An optional error message to display.
   * @param options Additional options for the widget.
   * @return A RemoteViews object representing the widget.
   */
  fun createRemoteView(
      context: Context,
      widgetType: String,
      startTime: Long,
      endTime: Long,
      currentValue: SpannableString,
      errorMessage: String? = null,
      options: StandaloneWidgetOptions? = null
  ): RemoteViews {

    // If there is an error message, create an error widget view.
    if (errorMessage != null) {
      return RemoteViews(context.packageName, R.layout.error_widget).apply {
        setTextViewText(R.id.error_text, errorMessage)
        setOnClickPendingIntent(
            R.id.background,
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE))
      }
    }

    // Extract options or set default values.
    val decimalPlace = options?.decimalPlaces ?: 2
    val timeLeftCounter = options?.timeLeftCounter == true
    val replaceProgressWithDaysLeft = options?.replaceProgressWithDaysLeft == true
    val widgetBackgroundAlpha = ((options?.backgroundTransparency ?: 100) / 100.0 * 255).toInt()
    val progress = calculateProgress(context, startTime, endTime)
    val widgetDaysLeftCounter =
        context.getString(
            R.string.time_left, calculateTimeLeft(endTime).toTimePeriodLeftText(options?.dynamicLeftCounter == true))

    /**
     * Creates a rectangular RemoteViews object.
     *
     * @return A RemoteViews object for a rectangular widget.
     */
    fun rectangularRemoteView(): RemoteViews {
      return RemoteViews(context.packageName, R.layout.standalone_widget_layout).apply {
        setTextViewText(R.id.widgetType, widgetType)
        setTextViewText(R.id.widgetCurrentValue, currentValue)
        setTextViewText(R.id.widgetDaysLeft, widgetDaysLeftCounter)
        setTextViewText(R.id.widgetProgress, progress.styleFormatted(decimalPlace))
        setProgressBar(R.id.widgetProgressBar, 100, progress.roundToInt(), false)
        setFloat(R.id.widgetCurrentValue, "setTextSize", 8f)
        setOnClickPendingIntent(
            R.id.background,
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE))
        setInt(R.id.widgetContainer, "setImageAlpha", widgetBackgroundAlpha)
        setViewVisibility(
            R.id.widgetDaysLeft,
            if (timeLeftCounter && !replaceProgressWithDaysLeft) View.VISIBLE else View.GONE)
        if (timeLeftCounter && replaceProgressWithDaysLeft) {
          setTextViewText(R.id.widgetProgress, widgetDaysLeftCounter)
          setTextViewTextSize(R.id.widgetProgress, 0, 35f)
        }
      }
    }

    /**
     * Creates a clover-shaped RemoteViews object.
     *
     * @return A RemoteViews object for a clover-shaped widget.
     */
    fun cloverRemoteView(): RemoteViews {
      return RemoteViews(context.packageName, R.layout.standalone_widget_layout_clover).apply {
        setTextViewText(R.id.widgetType, widgetType)
        setTextViewText(R.id.widgetCurrentValue, currentValue)
        setTextViewText(R.id.widgetDaysLeft, widgetDaysLeftCounter)
        setTextViewText(
            R.id.widgetProgress,
            progress.styleFormatted(decimalPlace.coerceIn(0, 2), cloverMode = true))
        setImageViewResource(
            R.id.widgetContainer,
            when (progress) {
              in 0.0..5.0 -> R.drawable.background_clover_00
              in 5.0..10.0 -> R.drawable.background_clover_05
              in 10.0..20.0 -> R.drawable.background_clover_10
              in 20.0..30.0 -> R.drawable.background_clover_20
              in 30.0..40.0 -> R.drawable.background_clover_30
              in 40.0..50.0 -> R.drawable.background_clover_40
              in 50.0..60.0 -> R.drawable.background_clover_50
              in 60.0..70.0 -> R.drawable.background_clover_60
              in 70.0..80.0 -> R.drawable.background_clover_70
              in 80.0..90.0 -> R.drawable.background_clover_80
              in 90.0..95.0 -> R.drawable.background_clover_90
              in 95.0..100.0 -> R.drawable.background_clover_95
              else -> R.drawable.background_clover_100
            })
        setOnClickPendingIntent(
            R.id.background,
            PendingIntent.getActivity(
                context,
                0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE))
        setViewVisibility(
            R.id.widgetDaysLeft,
            if (timeLeftCounter && !replaceProgressWithDaysLeft) View.VISIBLE else View.GONE)
        if (timeLeftCounter && replaceProgressWithDaysLeft) {
          setTextViewText(R.id.widgetProgress, widgetDaysLeftCounter)
          setTextViewTextSize(R.id.widgetProgress, 0, 35f)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          setInt(R.id.widgetContainer, "setBackgroundColor", Color.TRANSPARENT)
        }
      }
    }

    // Return the appropriate RemoteViews based on the widget shape.
    return when (options?.shape) {
      WidgetShape.RECTANGLE,
      WidgetShape.PILL -> rectangularRemoteView()
      WidgetShape.CLOVER -> {
        val large =
            cloverRemoteView().apply {
              setTextViewTextSize(R.id.widgetType, TypedValue.COMPLEX_UNIT_SP, 13f)
              setTextViewTextSize(R.id.widgetCurrentValue, TypedValue.COMPLEX_UNIT_SP, 24f)
              setTextViewTextSize(R.id.widgetProgress, TypedValue.COMPLEX_UNIT_SP, 38f)
              setTextViewTextSize(R.id.widgetDaysLeft, TypedValue.COMPLEX_UNIT_SP, 11f)
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          large.apply {
            setViewLayoutMargin(
                R.id.widgetDaysLeft, RemoteViews.MARGIN_TOP, -8f, TypedValue.COMPLEX_UNIT_DIP)
            setViewLayoutHeight(R.id.widget_spacer, 8f, TypedValue.COMPLEX_UNIT_DIP)
          }
          val square =
              cloverRemoteView().apply {
                setViewLayoutHeight(R.id.widget_spacer, 16f, TypedValue.COMPLEX_UNIT_DIP)
                setViewLayoutMargin(
                    R.id.widgetDaysLeft, RemoteViews.MARGIN_TOP, -8f, TypedValue.COMPLEX_UNIT_DIP)
                setTextViewTextSize(R.id.widgetType, TypedValue.COMPLEX_UNIT_SP, 10f)
                setTextViewTextSize(R.id.widgetCurrentValue, TypedValue.COMPLEX_UNIT_SP, 20f)
                setTextViewTextSize(R.id.widgetProgress, TypedValue.COMPLEX_UNIT_SP, 28f)
                setTextViewTextSize(R.id.widgetDaysLeft, TypedValue.COMPLEX_UNIT_SP, 8f)
              }
          val small =
              cloverRemoteView().apply {
                setViewLayoutHeight(R.id.widget_spacer, 2f, TypedValue.COMPLEX_UNIT_DIP)
                setViewLayoutMargin(
                    R.id.widgetDaysLeft, RemoteViews.MARGIN_TOP, -4f, TypedValue.COMPLEX_UNIT_DIP)
                setTextViewTextSize(R.id.widgetType, TypedValue.COMPLEX_UNIT_SP, 6f)
                setTextViewTextSize(R.id.widgetCurrentValue, TypedValue.COMPLEX_UNIT_SP, 8f)
                setTextViewTextSize(R.id.widgetProgress, TypedValue.COMPLEX_UNIT_SP, 16f)
                setTextViewTextSize(R.id.widgetDaysLeft, TypedValue.COMPLEX_UNIT_SP, 4f)
              }
          RemoteViews(
              mapOf(
                  SizeF(220f, 220f) to large,
                  SizeF(160f, 160f) to square,
                  SizeF(100f, 100f) to small))
        } else {
          large
        }
      }
      else -> rectangularRemoteView()
    }
  }
}

/**
 * Data class representing options for a standalone widget.
 *
 * @property widgetId The ID of the widget.
 * @property decimalPlaces The number of decimal places to display.
 * @property timeLeftCounter Whether to display the time left counter.
 * @property dynamicLeftCounter Whether to use a dynamic time left counter.
 * @property replaceProgressWithDaysLeft Whether to replace progress with days left.
 * @property backgroundTransparency The transparency of the widget background.
 * @property widgetType The type of the widget.
 * @property shape The shape of the widget.
 */
data class StandaloneWidgetOptions(
    val widgetId: Int,
    val decimalPlaces: Int,
    val timeLeftCounter: Boolean,
    val dynamicLeftCounter: Boolean,
    val replaceProgressWithDaysLeft: Boolean,
    @IntRange(from = 0, to = 100) val backgroundTransparency: Int,
    val widgetType: TimePeriod?,
    val shape: WidgetShape
) {
  companion object {
    private const val WIDGET_TYPE = "widget_type_"
    private const val WIDGET_SHAPE = "widget_shape_"

    /**
     * Loads the widget options from shared preferences.
     *
     * @param context The context of the application.
     * @param widgetId The ID of the widget.
     * @return The loaded StandaloneWidgetOptions.
     */
    fun load(context: Context, widgetId: Int): StandaloneWidgetOptions {
      val pref = PreferenceManager.getDefaultSharedPreferences(context)

      val globalDecimalPointKey = context.getString(R.string.widget_widget_decimal_point)
      val globalTimeLeftKey = context.getString(R.string.widget_widget_time_left)
      val globalDynamicTimeLeftKey = context.getString(R.string.widget_widget_use_dynamic_time_left)
      val globalReplaceWithCounterKey =
          context.getString(R.string.widget_widget_event_replace_progress_with_days_counter)
      val globalBackgroundTransparencyKey =
          context.getString(R.string.widget_widget_background_transparency)

      val widgetTypeKey = "$WIDGET_TYPE$widgetId"
      val widgetShapeKey = "$WIDGET_SHAPE$widgetId"

      val globalDecimalPoint = pref.getInt(globalDecimalPointKey, 2)
      val globalTimeLeft = pref.getBoolean(globalTimeLeftKey, false)
      val globalDynamicTimeLeft = pref.getBoolean(globalDynamicTimeLeftKey, false)
      val globalReplaceWithCounter = pref.getBoolean(globalReplaceWithCounterKey, false)
      val globalBackgroundTransparency = pref.getInt(globalBackgroundTransparencyKey, 100)

      return StandaloneWidgetOptions(
          widgetId,
          pref.getInt("$globalDecimalPointKey$widgetId", globalDecimalPoint),
          pref.getBoolean("$globalTimeLeftKey$widgetId", globalTimeLeft),
          pref.getBoolean("$globalDynamicTimeLeftKey$widgetId", globalDynamicTimeLeft),
          pref.getBoolean("$globalReplaceWithCounterKey$widgetId", globalReplaceWithCounter),
          pref.getInt("$globalBackgroundTransparencyKey$widgetId", globalBackgroundTransparency),
          pref.getString(widgetTypeKey, TimePeriod.DAY.name)?.let { TimePeriod.valueOf(it) },
          pref.getString(widgetShapeKey, WidgetShape.RECTANGLE.name)?.let {
            WidgetShape.valueOf(it)
          } ?: WidgetShape.RECTANGLE)
    }

    /** Enum class representing the shape of the widget. */
    enum class WidgetShape {
      RECTANGLE,
      CLOVER,
      PILL
    }
  }

  /**
   * Saves the widget options to shared preferences.
   *
   * @param context The context of the application.
   */
  fun save(context: Context) {
    PreferenceManager.getDefaultSharedPreferences(context).edit().apply {
      putInt("${context.getString(R.string.widget_widget_decimal_point)}${widgetId}", decimalPlaces)
      putBoolean(
          "${context.getString(R.string.widget_widget_time_left)}${widgetId}", timeLeftCounter)
      putBoolean(
          "${context.getString(R.string.widget_widget_use_dynamic_time_left)}${widgetId}",
          dynamicLeftCounter)
      putBoolean(
          "${context.getString(R.string.widget_widget_event_replace_progress_with_days_counter)}${widgetId}",
          replaceProgressWithDaysLeft)
      putInt(
          "${context.getString(R.string.widget_widget_background_transparency)}${widgetId}",
          backgroundTransparency)
      putString("$WIDGET_TYPE$widgetId", widgetType?.name)
      putString("$WIDGET_SHAPE$widgetId", shape.name)
      apply()
    }
  }
}

/**
 * Abstract class representing a standalone widget.
 *
 * @property widgetType The type of the widget.
 */
abstract class StandaloneWidget(private val widgetType: TimePeriod) : BaseWidget() {

  companion object {
    /**
     * Creates a RemoteViews object for a standalone widget.
     *
     * @param context The context of the application.
     * @param options The options for the widget.
     * @return A RemoteViews object representing the standalone widget.
     */
    fun standaloneWidgetRemoteView(
        context: Context,
        options: StandaloneWidgetOptions
    ): RemoteViews {
      val widgetType = options.widgetType ?: TimePeriod.DAY
      val startTime = calculateStartTime(context, widgetType)
      val endTime = calculateEndTime(context, widgetType)
      val currentValue = getCurrentPeriodValue(widgetType).toFormattedTimePeriod(widgetType)
      val widgetTitleText =
          when (widgetType) {
            TimePeriod.DAY -> context.getString(R.string.day)
            TimePeriod.WEEK -> context.getString(R.string.week)
            TimePeriod.MONTH -> context.getString(R.string.month)
            TimePeriod.YEAR -> context.getString(R.string.year)
          }
      return WidgetUtils.createRemoteView(
          context,
          widgetTitleText,
          startTime,
          endTime,
          SpannableString(currentValue),
          options = options)
    }
  }

  /**
   * Updates the widget.
   *
   * @param context The context of the application.
   * @param appWidgetManager The AppWidgetManager instance.
   * @param appWidgetId The ID of the widget.
   */
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    val options = StandaloneWidgetOptions.load(context, appWidgetId).copy(widgetType = widgetType)
    options.save(context) // This ensures that the widget type is saved, when freshly added.
    appWidgetManager.updateAppWidget(appWidgetId, standaloneWidgetRemoteView(context, options))
  }
}

/**
 * Abstract class representing a day/night widget.
 *
 * @property dayLight Whether the widget is for daylight.
 */
abstract class DayNightWidget(private val dayLight: Boolean) : BaseWidget() {

  companion object {
    /**
     * Creates a RemoteViews object for a day/night widget.
     *
     * @param context The context of the application.
     * @param dayLight Whether the widget is for daylight.
     * @param options The options for the widget.
     * @return A RemoteViews object representing the day/night widget.
     */
    fun dayNightLightWidgetRemoteView(
        context: Context,
        dayLight: Boolean,
        options: StandaloneWidgetOptions
    ): RemoteViews {
      if (ContextCompat.checkSelfPermission(
          context, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
          PackageManager.PERMISSION_GRANTED) {
        return WidgetUtils.createRemoteView(
            context,
            if (dayLight) context.getString(R.string.day_light)
            else context.getString(R.string.night_light),
            0,
            0,
            SpannableString(""),
            context.getString(R.string.no_location_permission))
      }

      val sunriseSunset =
          loadSunriseSunset(context)
              ?: return WidgetUtils.createRemoteView(
                  context,
                  if (dayLight) context.getString(R.string.day_light)
                  else context.getString(R.string.night_light),
                  0,
                  0,
                  SpannableString(""),
                  "No data, Tap to retry")

      val (startTime, endTime) = sunriseSunset.getStartAndEndTime(dayLight)
      val currentValue =
          if (dayLight) "🌇 ${sunriseSunset.results[1].sunset}"
          else "🌅 ${sunriseSunset.results[1].sunrise}"
      return WidgetUtils.createRemoteView(
          context,
          if (dayLight) context.getString(R.string.day_light)
          else context.getString(R.string.night_light),
          startTime,
          endTime,
          SpannableString(currentValue),
          options = options)
    }
  }

  /**
   * Updates the widget.
   *
   * @param context The context of the application.
   * @param appWidgetManager The AppWidgetManager instance.
   * @param appWidgetId The ID of the widget.
   */
  override fun updateWidget(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetId: Int
  ) {
    val options = StandaloneWidgetOptions.load(context, appWidgetId).copy(widgetType = null)
    appWidgetManager.updateAppWidget(
        appWidgetId, dayNightLightWidgetRemoteView(context, dayLight, options))
  }
}

/** Class representing a daylight widget. */
class DayLightWidget : DayNightWidget(true)

/** Class representing a nightlight widget. */
class NightLightWidget : DayNightWidget(false)

/** Class representing a day widget. */
class DayWidget : StandaloneWidget(TimePeriod.DAY)

/** Class representing a month widget. */
class MonthWidget : StandaloneWidget(TimePeriod.MONTH)

/** Class representing a week widget. */
class WeekWidget : StandaloneWidget(TimePeriod.WEEK)

/** Class representing a year widget. */
class YearWidget : StandaloneWidget(TimePeriod.YEAR)
