import java.io.File
import java.util.Properties

// 必须在最前执行：IDE/Cursor 可能用 Red Hat 扩展 JRE 启动 Gradle，导致 androidJdkImage 找不到 jlink
run {
    fun File.hasJlink(): Boolean = File(this, "bin/jlink.exe").isFile

    fun resolveJdkHome(rootDir: File): String? {
        val localFile = File(rootDir, "local.properties")
        if (localFile.isFile) {
            val props = Properties()
            localFile.inputStream().use { props.load(it) }
            val fromLocal = props.getProperty("java.home")?.trim()
            if (!fromLocal.isNullOrEmpty()) {
                val jdk = File(fromLocal)
                if (jdk.hasJlink()) return jdk.absolutePath.replace('\\', '/')
            }
        }
        val defaults = listOf(
            "C:/Program Files/Java/jdk-17",
            System.getenv("JAVA_HOME"),
        )
        return defaults.firstOrNull { path ->
            !path.isNullOrBlank() && File(path).hasJlink()
        }?.replace('\\', '/')
    }

    resolveJdkHome(File(".").canonicalFile)?.let { jdkHome ->
        System.setProperty("org.gradle.java.home", jdkHome)
    }
}

pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Ktor 新版本在阿里云镜像上可能缺 JAR，强制从 Maven Central 拉取
        exclusiveContent {
            forRepository {
                mavenCentral()
            }
            filter {
                includeGroup("io.ktor")
            }
        }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}

rootProject.name = "Compose"
include(":app")
include(":lib_base")
include(":lib_database")
include(":module_user")
include(":module_host")
include(":module_home")
include(":module_route")
