plugins {
    id("com.android.library")
    id("de.mannodermaus.android-junit5")
    kotlin("android")
    kotlin("kapt")
}

tasks.register("jacocoTestReport", JacocoReport::class) {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val mainSrc = "${project.projectDir}/src/main/kotlin"
    sourceDirectories.setFrom(files(listOf(mainSrc)))

    val debugTree = fileTree(baseDir = "${project.buildDir}/tmp/kotlin-classes/debug") {
        val fileFilter =
            setOf("**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "**/*Test*.*")
        setExcludes(fileFilter)
    }
    classDirectories.setFrom(files(listOf(debugTree)))

    executionData.setFrom(fileTree(project.buildDir) { include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec") })
}

android {
    compileSdkVersion(30)
    buildToolsVersion("30.0.3")

    defaultConfig {
        minSdkVersion(24)
        targetSdkVersion(30)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

    val copyTestResources: Task = tasks.create<Copy>("copyTestResources") {
        from("$projectDir/src/test/resources")
        into("$buildDir/classes/test")
    }
    tasks.withType<Test> {
        dependsOn(copyTestResources)
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.annotation:annotation:1.2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

    implementation("com.rocket.core:core-domain:0.0-beta0")
    implementation("com.rocket.android.core:crash-reporting-android:0.0-beta0")
    implementation("com.rocket.core:core-data-network-commons:0.0-beta0")

    testImplementation("com.rocket.android.core:core-data-network-test:0.0-beta0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.20")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("io.mockk:mockk-android:1.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.3")
}
