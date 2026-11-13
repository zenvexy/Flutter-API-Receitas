package com.example.flutter_receitas_api

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.UUID

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "receita_widget/pin")
            .setMethodCallHandler { call, result ->
                if (call.method != "fixar") {
                    result.notImplemented()
                    return@setMethodCallHandler
                }

                val receitaId = call.arguments as? String
                if (receitaId.isNullOrBlank() || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    result.success(false)
                    return@setMethodCallHandler
                }

                val manager = AppWidgetManager.getInstance(this)
                if (!manager.isRequestPinAppWidgetSupported) {
                    result.success(false)
                    return@setMethodCallHandler
                }

                val callbackIntent = Intent(this, ReceitaWidgetPinReceiver::class.java).apply {
                    data = Uri.parse("receita-widget://pin/${UUID.randomUUID()}")
                    putExtra("receita_id", receitaId)
                }
                val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_MUTABLE
                    } else {
                        0
                    })
                val callback = PendingIntent.getBroadcast(this, 0, callbackIntent, flags)
                val provider = ComponentName(this, ReceitaWidgetProvider::class.java)
                result.success(manager.requestPinAppWidget(provider, null, callback))
            }
    }
}
