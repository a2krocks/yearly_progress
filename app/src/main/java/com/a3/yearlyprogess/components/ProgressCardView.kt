package com.a3.yearlyprogess.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.TimePeriod
import com.a3.yearlyprogess.YearlyProgressManager.Companion.formatProgressStyle
import com.a3.yearlyprogess.YearlyProgressManager
import com.a3.yearlyprogess.calculateEndTime
import com.a3.yearlyprogess.calculateProgress
import com.a3.yearlyprogess.calculateStartTime
import com.a3.yearlyprogess.getCurrentPeriodValue
import com.a3.yearlyprogess.widgets.ui.util.styleFormatted
import com.a3.yearlyprogess.widgets.ui.util.toFormattedTimePeriod
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@SuppressLint("ViewConstructor", "SetTextI18n")
class ProgressCardView @JvmOverloads constructor(
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
    private var field: TimePeriod = TimePeriod.DAY


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
                context.obtainStyledAttributes(attrs, R.styleable.ProgressCardView)
            if (obtainAttributeSet.hasValue(R.styleable.ProgressCardView_dataType)) {
                field = TimePeriod.entries[obtainAttributeSet.getInt(
                    R.styleable.ProgressCardView_dataType,
                    0
                )]
            }
            obtainAttributeSet.recycle()
        }

        // data that doesn't change
        titleTextView.text = field.name

        // Calculate frequency to update constant values
        val freq =
            calculateEndTime(context, field) -
                    calculateStartTime(context, field) // in milliseconds

        // update constant values
        launch(Dispatchers.IO) {
            while (true) {
                val currentPeriodValue = getCurrentPeriodValue(field).toFormattedTimePeriod(field)
                widgetDataTextView.text = currentPeriodValue
                widgetDataInfoTextView.text = "of ${
                    (calculateEndTime(context, field)
                            - calculateStartTime(context, field)) / 1000
                }s"
                delay(freq)
            }
        }

        // update the progress every seconds
        launch(Dispatchers.IO) {
            while (true) {

                val startTime = calculateStartTime(context, field)
                val endTime = calculateEndTime(context, field)
                val progress: Double = calculateProgress(context, startTime, endTime)

                launch(Dispatchers.Main) {
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

}