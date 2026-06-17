plugins {
    alias(libs.plugins.locationjoystick.android.library)
}

android {
    namespace = "com.locationjoystick.feature.group.api"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
}
