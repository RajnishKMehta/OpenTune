/*
 * OpenTune Project Original (2026)
 * Arturo254 (github.com/Arturo254)
 * Licensed Under GPL-3.0 | see git history for contributors
 */

package com.arturo254.opentune.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.palette.graphics.Palette
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import coil3.ImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.arturo254.opentune.R
import com.arturo254.opentune.playback.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, NowPlayingWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return@withContext Result.success()

        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

        try {
            val controller = controllerFuture.await()
            val mediaItem = controller.currentMediaItem
            val isPlaying = controller.isPlaying

            val remoteViews = RemoteViews(context.packageName, R.layout.now_playing_widget)

            if (mediaItem != null) {
                val metadata = mediaItem.mediaMetadata
                remoteViews.setTextViewText(R.id.song_title, metadata.title ?: context.getString(R.string.playback_state_unknown))
                remoteViews.setTextViewText(R.id.artist_name, metadata.artist ?: "")

                val artworkUri = metadata.artworkUri
                var bitmap: Bitmap? = null
                if (artworkUri != null) {
                    try {
                        val imageLoader = ImageLoader.Builder(context).build()
                        val request = ImageRequest.Builder(context)
                            .data(artworkUri.toString())
                            .size(300, 300)
                            .build()
                        val result = imageLoader.execute(request)
                        if (result is SuccessResult) {
                            bitmap = result.image.toBitmap()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load album art for widget")
                    }
                }

                if (bitmap != null) {
                    remoteViews.setImageViewBitmap(R.id.album_art, bitmap)
                    applyDynamicColors(remoteViews, bitmap)
                } else {
                    remoteViews.setImageViewResource(R.id.album_art, R.drawable.player_preview)
                    applyDefaultColors(remoteViews)
                }
            } else {
                remoteViews.setTextViewText(R.id.song_title, context.getString(R.string.no_track_playing))
                remoteViews.setTextViewText(R.id.artist_name, "")
                remoteViews.setImageViewResource(R.id.album_art, R.drawable.player_preview)
                applyDefaultColors(remoteViews)
            }

            // Set button icons
            remoteViews.setImageViewResource(
                R.id.btn_play_pause,
                if (isPlaying) R.drawable.pause else R.drawable.play
            )

            // Set pending intents
            remoteViews.setOnClickPendingIntent(
                R.id.btn_play_pause,
                NowPlayingWidgetProvider.getPendingIntent(context, NowPlayingWidgetProvider.ACTION_PLAY_PAUSE)
            )
            remoteViews.setOnClickPendingIntent(
                R.id.btn_next,
                NowPlayingWidgetProvider.getPendingIntent(context, NowPlayingWidgetProvider.ACTION_NEXT)
            )
            remoteViews.setOnClickPendingIntent(
                R.id.btn_prev,
                NowPlayingWidgetProvider.getPendingIntent(context, NowPlayingWidgetProvider.ACTION_PREVIOUS)
            )

            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)

            withContext(Dispatchers.Main) {
                controller.release()
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error updating widget")
            Result.failure()
        }
    }

    private fun applyDynamicColors(remoteViews: RemoteViews, bitmap: Bitmap) {
        val palette = Palette.from(bitmap).generate()

        // Extract dominant and vibrant colors
        val backgroundColor = palette.getDominantColor(context.getColor(android.R.color.system_accent1_100))
        val vibrantColor = palette.getVibrantColor(context.getColor(android.R.color.system_accent1_900))
        val mutedColor = palette.getMutedColor(context.getColor(android.R.color.system_accent1_800))

        // Ensure contrast (simplified)
        val titleColor = vibrantColor
        val artistColor = mutedColor

        remoteViews.setInt(R.id.widget_background, "setBackgroundColor", backgroundColor)
        remoteViews.setTextColor(R.id.song_title, titleColor)
        remoteViews.setTextColor(R.id.artist_name, artistColor)

        // Tint buttons
        remoteViews.setInt(R.id.btn_prev, "setColorFilter", titleColor)
        remoteViews.setInt(R.id.btn_play_pause, "setColorFilter", titleColor)
        remoteViews.setInt(R.id.btn_next, "setColorFilter", titleColor)
    }

    private fun applyDefaultColors(remoteViews: RemoteViews) {
        remoteViews.setInt(R.id.widget_background, "setBackgroundColor", context.getColor(android.R.color.system_accent1_100))
        remoteViews.setTextColor(R.id.song_title, context.getColor(android.R.color.system_accent1_900))
        remoteViews.setTextColor(R.id.artist_name, context.getColor(android.R.color.system_accent1_800))

        val tint = context.getColor(android.R.color.system_accent1_900)
        remoteViews.setInt(R.id.btn_prev, "setColorFilter", tint)
        remoteViews.setInt(R.id.btn_play_pause, "setColorFilter", tint)
        remoteViews.setInt(R.id.btn_next, "setColorFilter", tint)
    }
}
