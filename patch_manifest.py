with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

content = content.replace(
    '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />',
    '<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />\n    <uses-permission android:name="android.permission.VIBRATE" />'
)

content = content.replace(
    '</activity>',
    '</activity>\n        <activity android:name=".ui.screens.AlarmActivity" android:exported="false" android:theme="@style/Theme.MyApplication" />'
)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
