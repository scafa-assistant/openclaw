package de.openclaw.assistant.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import de.openclaw.assistant.MainActivity
import de.openclaw.assistant.R

class OpenClawWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_VOICE -> {
                // Launch app with voice mode
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("start_voice", true)
                }
                context.startActivity(launchIntent)
            }
            ACTION_QUICK_WEATHER -> sendQuickCommand(context, "Wie ist das Wetter?")
            ACTION_QUICK_REMINDER -> sendQuickCommand(context, "Erstelle eine Erinnerung")
            ACTION_QUICK_TIMER -> sendQuickCommand(context, "Timer für 5 Minuten")
        }
    }

    private fun sendQuickCommand(context: Context, command: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("quick_command", command)
        }
        context.startActivity(intent)
    }

    companion object {
        const val ACTION_VOICE = "de.openclaw.assistant.widget.VOICE"
        const val ACTION_QUICK_WEATHER = "de.openclaw.assistant.widget.WEATHER"
        const val ACTION_QUICK_REMINDER = "de.openclaw.assistant.widget.REMINDER"
        const val ACTION_QUICK_TIMER = "de.openclaw.assistant.widget.TIMER"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_openclaw)

            // Voice button
            val voiceIntent = Intent(context, OpenClawWidget::class.java).apply {
                action = ACTION_VOICE
            }
            val voicePendingIntent = PendingIntent.getBroadcast(
                context, 0, voiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_voice_button, voicePendingIntent)

            // Quick action buttons
            val weatherIntent = Intent(context, OpenClawWidget::class.java).apply {
                action = ACTION_QUICK_WEATHER
            }
            views.setOnClickPendingIntent(
                R.id.widget_weather_button,
                PendingIntent.getBroadcast(context, 1, weatherIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            val reminderIntent = Intent(context, OpenClawWidget::class.java).apply {
                action = ACTION_QUICK_REMINDER
            }
            views.setOnClickPendingIntent(
                R.id.widget_reminder_button,
                PendingIntent.getBroadcast(context, 2, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            val timerIntent = Intent(context, OpenClawWidget::class.java).apply {
                action = ACTION_QUICK_TIMER
            }
            views.setOnClickPendingIntent(
                R.id.widget_timer_button,
                PendingIntent.getBroadcast(context, 3, timerIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
