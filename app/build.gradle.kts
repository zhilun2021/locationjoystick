plugins {
    alias(libs.plugins.locationjoystick.android.application)
    alias(libs.plugins.locationjoystick.hilt)
}

android {
    namespace = "com.locationjoystick.app"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:location"))
    implementation(project(":core:model"))
    implementation(project(":core:overlay"))
    implementation(project(":core:routing"))
    implementation(project(":core:ui"))

    implementation(project(":feature:setup:impl"))
    implementation(project(":feature:map:impl"))
    implementation(project(":feature:joystick:impl"))
    implementation(project(":feature:routes:impl"))
    implementation(project(":feature:favorites:impl"))
    implementation(project(":feature:roaming:impl"))
    implementation(project(":feature:settings:impl"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
