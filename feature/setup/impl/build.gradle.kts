plugins {
    alias(libs.plugins.locationjoystick.android.feature)
}

android {
    namespace = "com.locationjoystick.feature.setup.impl"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":feature:setup:api"))
    implementation(project(":core:data"))

    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.compose.material.icons.extended)
}
