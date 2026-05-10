plugins {
    alias(libs.plugins.locationjoystick.android.library)
    alias(libs.plugins.locationjoystick.android.library.compose)
}

android {
    namespace = "com.locationjoystick.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))
}
