import sys

manifest_path = 'app/src/main/AndroidManifest.xml'
content = open(manifest_path).read()

widget_receiver = '''
        <receiver
            android:name=".widget.NowPlayingWidgetProvider"
            android:exported="true"
            android:label="@string/widget_now_playing">
            <intent-filter>
                <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
                <action android:name="com.arturo254.opentune.widget.ACTION_PLAY_PAUSE" />
                <action android:name="com.arturo254.opentune.widget.ACTION_NEXT" />
                <action android:name="com.arturo254.opentune.widget.ACTION_PREVIOUS" />
            </intent-filter>

            <meta-data
                android:name="android.appwidget.provider"
                android:resource="@xml/now_playing_widget_info" />
        </receiver>

        <service
            android:name="androidx.work.impl.background.systemjob.SystemJobService"
            android:permission="android.permission.BIND_JOB_SERVICE"
            tools:node="replace" />
'''

if '</application>' in content:
    content = content.replace('</application>', f'{widget_receiver}\n    </application>')

with open(manifest_path, 'w') as f:
    f.write(content)
