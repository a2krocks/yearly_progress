package com.a3.yearlyprogess.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.data.models.SunriseSunsetResponse
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext

@SuppressLint("ViewConstructor", "SetTextI18n")
class DayNightLightProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
    defStyleRes: Int = 0,
) : LinearLayout(context, attrs, defStyle, defStyleRes), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job


    private var perTextView: TextView
    private var widgetDataInfoTextView: TextView
    private var widgetDataTextView: TextView
    private var titleTextView: TextView
    private var widgetParentCard: MaterialCardView
    private var widgetProgressCard: MaterialCardView
    private var job: Job
    private var dayLight = true


    private var startTime = 0L
    private var endTime = 0L


    init {
        LayoutInflater.from(context).inflate(R.layout.progress_card_view, this, true)
        orientation = VERTICAL


        job = Job()

        perTextView = findViewById(R.id.widget_per)
        widgetDataInfoTextView = findViewById(R.id.widget_data_info)
        widgetParentCard = findViewById(R.id.material_card_parent)
        widgetProgressCard = findViewById(R.id.material_card_progress)
        widgetDataTextView = findViewById(R.id.widget_data)
        titleTextView = findViewById(R.id.widget_title)

        if (attrs != null) {
            val obtainAttributeSet =
                context.obtainStyledAttributes(attrs, R.styleable.DayNightLightProgressView)
            if (obtainAttributeSet.hasValue(R.styleable.DayNightLightProgressView_day_light)) {
                dayLight = obtainAttributeSet.getBoolean(
                    R.styleable.DayNightLightProgressView_day_light, true
                )
            }
            obtainAttributeSet.recycle()
        }


        // data that doesn't change
        titleTextView.text = if (dayLight) "Day Light" else "Night Light"


        // update the progress every seconds
        launch(Dispatchers.IO) {
            while (true) {
                val progress: Double = calculateProgress(context, startTime, endTime)
                launch(Dispatchers.Main) {
                    val currentPeriodValue = if (dayLight) {
                        "Today sunrise at ${
                            startTime.toFormattedDateText()
                        } and sunset at ${endTime.toFormattedDateText()}."
                    } else {
                        "Last night's sunset was at ${startTime.toFormattedDateText()} and next sunrise will be at ${endTime.toFormattedDateText()}."
                    }
                    widgetDataTextView.text = currentPeriodValue
                    widgetDataTextView.textSize = 12f
                    widgetDataTextView.setTextColor(ContextCompat.getColor(context,R.color.widget_text_color_tertiary))
                    widgetDataInfoTextView.text = "of ${(endTime - startTime) / 1000}s"
                    updateView(progress)
                }
                delay(1000)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateView(progress: Double) {

        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val decimalPlace: Int =
            pref.getInt(context.getString(R.string.app_widget_decimal_point), 13)

        perTextView.text = progress.styleFormatted(decimalPlace)

        val params = widgetProgressCard.layoutParams
        val target = (progress * 0.01 * widgetParentCard.width).toInt()
        val valueAnimator = ValueAnimator.ofInt(params.width, target)
        valueAnimator.duration = 500
        valueAnimator.addUpdateListener {
            widgetProgressCard.layoutParams.width = it.animatedValue as Int
            widgetProgressCard.requestLayout()
        }
        valueAnimator.start()
    }

    fun loadSunriseSunset(data: SunriseSunsetResponse) {
        val (startTime, endTime) = data.getStartAndEndTime(dayLight)
        this.startTime = startTime
        this.endTime = endTime
    }

    fun Long.toFormattedDateText(): String {
        val date = Date(this)
        val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
        return format.format(date)
    }

}
