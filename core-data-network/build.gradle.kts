plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
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
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.20")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.annotation:annotation:1.2.0")

    //api("com.rocket.core:data-network-commons:0.0.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.12.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.10.0")

    //testImplementation("com.rocket.android.core:data-network-test:0.0.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.3")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.5.20")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("io.mockk:mockk-android:1.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.2")
    testImplementation("androidx.test.ext:junit:1.1.3")
}
