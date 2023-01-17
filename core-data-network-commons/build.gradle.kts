plugins {
    id("kotlin")
    id("java-library")
    kotlin("kapt")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.30")

    api("com.rocket.core:crash-reporting:0.0-beta1-SNAPSHOT20220909")

    api("com.squareup.retrofit2:retrofit:2.9.0")
    api("com.squareup.retrofit2:converter-moshi:2.9.0")
    api("com.squareup.okhttp3:logging-interceptor:4.9.0")
    api("com.squareup.moshi:moshi-kotlin:1.12.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.10.0")
}
