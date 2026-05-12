plugins {
    alias(libs.plugins.locationjoystick.android.library)
    alias(libs.plugins.locationjoystick.hilt)
}

android {
    namespace = "com.locationjoystick.core.location"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:routing"))
    implementation(project(":feature:joystick:impl"))
    implementation(project(":feature:widget:impl"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
