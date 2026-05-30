import sys

content = open('app/src/main/kotlin/com/arturo254/opentune/playback/MusicService.kt').read()

# Add import
import_line = 'import com.arturo254.opentune.widget.NowPlayingWidgetProvider'
if import_line not in content:
    content = content.replace('package com.arturo254.opentune.playback', f'package com.arturo254.opentune.playback\n\n{import_line}\nimport android.appwidget.AppWidgetManager')

# Add call to update widget in updateNotification
target = 'private fun updateNotification() {'
widget_update = '''
    private fun updateWidget() {
        val intent = Intent(this, NowPlayingWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(applicationContext)
                .getAppWidgetIds(ComponentName(applicationContext, NowPlayingWidgetProvider::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        sendBroadcast(intent)
    }

    private fun updateNotification() {
        updateWidget()
'''
content = content.replace(target, widget_update)

with open('app/src/main/kotlin/com/arturo254/opentune/playback/MusicService.kt', 'w') as f:
    f.write(content)
