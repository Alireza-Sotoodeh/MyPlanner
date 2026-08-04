with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace(
    'implementation(libs.androidx.core.ktx)',
    'implementation(libs.androidx.core.ktx)\n  implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")'
)

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
