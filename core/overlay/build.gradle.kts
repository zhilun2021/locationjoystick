plugins {
    alias(libs.plugins.locationjoystick.android.library)
}

android {
    namespace = "com.locationjoystick.core.overlay"
}

dependencies {
    implementation(project(":core:common"))
}
