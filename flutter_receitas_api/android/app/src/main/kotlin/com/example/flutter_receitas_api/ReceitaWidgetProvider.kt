package com.example.flutter_receitas_api

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import es.antonborri.home_widget.HomeWidgetLaunchIntent
import es.antonborri.home_widget.HomeWidgetPlugin
import es.antonborri.home_widget.HomeWidgetProvider

class ReceitaWidgetProvider : HomeWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        widgetData: SharedPreferences,
    ) {
        appWidgetIds.forEach { widgetId ->
            val receitaId = widgetData.getString("widget_receita_$widgetId", null)
            val nome = if (receitaId == null) {
                widgetData.getString("receita_nome", null)
            } else {
                widgetData.getString("receita_nome_$receitaId", null)
            }
            val imagePath = if (receitaId == null) {
                widgetData.getString("receita_imagem", null)
            } else {
                widgetData.getString("receita_imagem_$receitaId", null)
            }

            val views = RemoteViews(context.packageName, R.layout.receita_widget).apply {
                setTextViewText(
                    R.id.receita_widget_nome,
                    nome ?: context.getString(R.string.receita_widget_default_name),
                )

                val bitmap = imagePath?.takeIf { it.isNotEmpty() }?.let(BitmapFactory::decodeFile)
                if (bitmap != null) {
                    setImageViewBitmap(R.id.receita_widget_imagem, bitmap)
                } else {
                    setImageViewResource(
                        R.id.receita_widget_imagem,
                        android.R.drawable.ic_menu_gallery,
                    )
                }

                setOnClickPendingIntent(
                    R.id.receita_widget_container,
                    HomeWidgetLaunchIntent.getActivity(context, MainActivity::class.java),
                )
            }
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        val editor = HomeWidgetPlugin.getData(context).edit()
        appWidgetIds.forEach { editor.remove("widget_receita_$it") }
        editor.apply()
        super.onDeleted(context, appWidgetIds)
    }
}
