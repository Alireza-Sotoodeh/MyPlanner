with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

content = content.replace(
    '<uses-permission android:name="android.permission.USE_EXACT_ALARM" />',
    '<uses-permission android:name="android.permission.USE_EXACT_ALARM" />\n    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />'
)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
