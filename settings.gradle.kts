pluginManagement {
    repositories {
        maven { url = uri("./repo") }
//        maven {
//            url = uri(providers.gradleProperty("REPOSITORY_URL").get())
//            credentials {
//                username = providers.gradleProperty("NEXUS_USERNAME").get()
//                password = providers.gradleProperty("NEXUS_PASSWORD").get()
//            }
//        }
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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        exclusiveContent {
            forRepository { mavenCentral() }
            filter { includeGroup("io.ktor") }
        }
        maven { url = uri("./repo") }
//        maven {
//            url = uri(providers.gradleProperty("REPOSITORY_URL").get())
//            credentials {
//                username = providers.gradleProperty("NEXUS_USERNAME").get()
//                password = providers.gradleProperty("NEXUS_PASSWORD").get()
//            }
//        }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven(url = "https://jitpack.io")
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
include(":module_pdf")
include(":module_iot")
