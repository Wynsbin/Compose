plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.parcelize)
    id("maven-publish")
}

extra.apply {
    set("aarId", "pdf")
    set("aarDescription", "PDF Editor Module")
    set("versionName", "1.0.0")
    set("publishToServer", false)
    set("groupID", project.findProperty("GROUP_MODULE"))
    set("localPath", "${rootProject.projectDir}/repo")
}

apply(from = "../nexus_maven_push.gradle")

android {
    namespace = "com.yung.module_pdf"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt",
            )
        }
    }
}

dependencies {
    implementation(libs.utilcodex)
    implementation(libs.eventbus)
    implementation(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.guolin.permissionx)
    implementation(libs.orbit.viewmodel)
    implementation(libs.orbit.compose)
    implementation(libs.android.pdf.viewer)
    api(libs.pdfbox.android)
    implementation(libs.compose.reorderable)
    implementation(libs.coil.compose)
}
