package com.a3.yearlyprogess.widgets.manager.updateManager.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.a3.yearlyprogess.widgets.ui.EventWidget

class EventWidgetService : BaseWidgetService() {

    private val widgetClass = EventWidget::class.java

    override fun setIntent(context: Context): Intent {
        return Intent(context, widgetClass)
    }

    override fun setComponent(context: Context): ComponentName {
        return ComponentName(context, widgetClass)
    }
}