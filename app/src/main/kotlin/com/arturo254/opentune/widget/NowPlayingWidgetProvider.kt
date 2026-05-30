/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */

package com.arturo254.opentune.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.arturo254.opentune.playback.MusicService
import com.google.common.util.concurrent.MoreExecutors

class NowPlayingWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_PLAY_PAUSE, ACTION_NEXT, ACTION_PREVIOUS -> {
                handlePlaybackAction(context, intent.action!!)
            }
        }
    }

    private fun handlePlaybackAction(context: Context, action: String) {
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            when (action) {
                ACTION_PLAY_PAUSE -> {
                    if (controller.isPlaying) {
                        controller.pause()
                    } else {
                        controller.play()
                    }
                }
                ACTION_NEXT -> controller.seekToNext()
                ACTION_PREVIOUS -> controller.seekToPrevious()
            }
            controller.release()

            // Refresh widget after action
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>().build()
            WorkManager.getInstance(context).enqueue(request)
        }, MoreExecutors.directExecutor())
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.arturo254.opentune.widget.ACTION_PLAY_PAUSE"
        const val ACTION_NEXT = "com.arturo254.opentune.widget.ACTION_NEXT"
        const val ACTION_PREVIOUS = "com.arturo254.opentune.widget.ACTION_PREVIOUS"

        fun getPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, NowPlayingWidgetProvider::class.java).apply {
                this.action = action
            }
            return PendingIntent.getBroadcast(
                context,
                action.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
