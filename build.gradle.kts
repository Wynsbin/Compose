import com.android.build.api.dsl.LibraryExtension
import com.android.build.gradle.AppExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

subprojects {
    afterEvaluate {
        val compileSdkVersion = rootProject.libs.versions.compileSdk.get().toInt()
        val minSdkVersion = rootProject.libs.versions.minSdk.get().toInt()
        val targetSdkVersion = rootProject.libs.versions.targetSdk.get().toInt()

        extensions.findByType(LibraryExtension::class.java)?.apply {
            compileSdkVersion(compileSdkVersion)
            defaultConfig.minSdk = minSdkVersion
        }
        extensions.findByType(AppExtension::class.java)?.apply {
            compileSdkVersion(compileSdkVersion)
            defaultConfig {
                minSdk = minSdkVersion
                targetSdk = targetSdkVersion
            }
        }
    }

    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
}