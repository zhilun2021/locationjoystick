plugins {
    alias(libs.plugins.locationjoystick.android.library)
}

android {
    namespace = "com.locationjoystick.feature.map.api"
}

dependencies {
    api(project(":core:map"))
    implementation(libs.androidx.navigation.compose)
}
