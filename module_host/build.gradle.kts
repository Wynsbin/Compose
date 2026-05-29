plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.yung.host"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}
dependencies {
    api(project(":lib_base"))
    api(project(":lib_database"))
    api(project(":module_route"))
    api(project(":module_home"))
    api(project(":module_route"))
    api(project(":module_user"))
    api(project(":module_pdf"))
//    api(libs.pdf)
    implementation(libs.androidx.core.ktx)
}
