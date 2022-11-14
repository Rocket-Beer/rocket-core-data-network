plugins {
    id("org.sonarqube")
}

sonarqube {
    properties {
        property("sonar.projectVersion", "1.0.0")
        // This must match "versionName" in the build.gradle of module "presentation".
        val files = fileTree(baseDir = project.projectDir).apply {
            setIncludes(listOf("**/jacocoTestReport.xml"))
        }.files
        property("sonar.coverage.jacoco.xmlReportPaths", files.joinToString(", "))
        property("sonar.projectKey", "RocketCoreDataNetwork")
        property("sonar.projectName", "rocket-core-data-network")

        // Exclusions:
        // - UI classes are nearly impossible to test because they are highly coupled to Android SDK.
        property(
            "sonar.exclusions",
            "**/*.java"
        )

    }
}
