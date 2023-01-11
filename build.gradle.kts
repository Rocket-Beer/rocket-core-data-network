plugins {
    id("jacoco")
}

allprojects {
    repositories {
        google()
        mavenCentral()

        maven("https://maven.pkg.github.com/Rocket-Beer/*") {
            println("**** Rocket Beer maven ****")

            credentials {
                val userName = publish.CommonMethods.getPublisherUserName(rootProject)
                val userPass = publish.CommonMethods.getPublisherPassword(rootProject)

                println("user = $userName :: password = $userPass")

                username = userName
                password = userPass
            }
        }
    }

    apply(plugin = "jacoco")
    jacoco {
        toolVersion = "0.8.7"
    }

    tasks.withType(Test::class.java) {
        configure<JacocoTaskExtension> {
            isIncludeNoLocationClasses = true
            excludes = listOf("jdk.internal.*")
        }
    }
}

subprojects {
    println("\n********** Configuration for == $project == **********")
    apply(plugin = "rocket-plugin")
}

println("\n********** Sonar configuration for all projects **********")
apply(plugin = "sonar.plugin")



tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}