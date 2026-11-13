package com.example.flutter_receitas_api

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import es.antonborri.home_widget.HomeWidgetPlugin

class ReceitaWidgetPinReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val receitaId = intent.getStringExtra("receita_id") ?: return
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        )
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        HomeWidgetPlugin.getData(context)
            .edit()
            .putString("widget_receita_$widgetId", receitaId)
            .apply()

        ReceitaWidgetProvider().onUpdate(
            context,
            AppWidgetManager.getInstance(context),
            intArrayOf(widgetId),
        )
    }
}
