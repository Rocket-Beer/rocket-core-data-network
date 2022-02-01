import org.jetbrains.kotlin.konan.properties.loadProperties

plugins {
    `kotlin-dsl`
    java
}

repositories {
    maven("https://plugins.gradle.org/m2/")
    google()
    mavenCentral()

    maven("https://maven.pkg.github.com/Rocket-Beer/*") {
        credentials {
            println("**** Rocket Beer maven ****")

            var userName: String?
            var token: String?

            try {
                val path = "$projectDir/../local.properties"

                println("local properties path = $path")

                val properties = loadProperties(path)

                userName = properties.getProperty("github.username")
                if (userName.isEmpty()) userName = System.getenv("GITHUB_ACTOR")

                token = properties.getProperty("github.token")
                if (token.isEmpty()) token = System.getenv("GITHUB_TOKEN")
            } catch (e: Exception) {
                userName = System.getenv("GITHUB_ACTOR")
                token = System.getenv("GITHUB_TOKEN")
            }

            println("username = $userName :: token = $token")

            username = userName ?: ""
            password = token ?: ""
        }
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:4.2.1")
    implementation(kotlin("gradle-plugin", version = "1.5.20"))
    implementation("rocket-gradle:rocket-plugin:1.0-dev02")
    implementation("de.mannodermaus.gradle.plugins:android-junit5:1.7.1.1")
}