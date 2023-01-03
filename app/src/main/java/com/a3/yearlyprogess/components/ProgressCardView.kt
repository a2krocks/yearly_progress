package com.a3.yearlyprogess.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.helper.ProgressPercentage
import com.a3.yearlyprogess.helper.ProgressPercentage.Companion.formatProgressStyle
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
    private var field: Int = ProgressPercentage.YEAR


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
                field = obtainAttributeSet.getInt(R.styleable.ProgressCardView_dataType, 100)
            }
            obtainAttributeSet.recycle()
        }

        // data that doesn't change
        titleTextView.text = when (field) {
            ProgressPercentage.YEAR -> "Year"
            ProgressPercentage.MONTH -> "Month"
            ProgressPercentage.WEEK -> "Week"
            ProgressPercentage.DAY -> "Day"
            else -> ""
        }

        // Calculate frequency to update constant values
        val perP = ProgressPercentage()

        val freq = (perP.getSeconds(field) - perP.getSecondsPassed(field)) * 1000 // in milliseconds

        // update constant values
        launch(Dispatchers.IO) {
            while (true) {
                val percentageProgress = ProgressPercentage()
                val currentProgressType = when (field) {
                    ProgressPercentage.YEAR -> percentageProgress.getYear()
                    ProgressPercentage.MONTH -> percentageProgress.getMonth(
                        str = true,
                        isLong = true
                    )
                    ProgressPercentage.WEEK -> percentageProgress.getWeek(str = true)
                    ProgressPercentage.DAY -> percentageProgress.getDay(custom = true)
                    else -> ""
                }
                widgetDataTextView.text = currentProgressType
                widgetDataInfoTextView.text = "of ${percentageProgress.getSeconds(field)}s"
                delay(freq)
            }
        }

        // update the progress every seconds
        launch(Dispatchers.IO) {
            while (true) {
                val percentageProgress = ProgressPercentage()
                val progress: Double = percentageProgress.getPercent(field)

                launch(Dispatchers.Main) {
                    updateView(progress)
                }
                delay(1000)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateView(progress: Double) {

        perTextView.text = formatProgressStyle(SpannableString("%,.13f".format(progress) + "%"))

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