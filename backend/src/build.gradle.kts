plugins {
    kotlin("jvm") version "1.9.0"
    application
}

val applicationMainClass = "com.papaguycodes.driver_license_generator.ApplicationKt"

repositories {
    mavenCentral()
}

kotlin {1.9.0
    sourceSets {
        main {
            kotlin.srcDir("main/Kotlin")
        }
    }
}

sourceSets {
    main {
        resources.srcDir("main/resources")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-server-status-pages:2.3.12")
    implementation("io.ktor:ktor-serialization-jackson:2.3.12")
}

application {
    mainClass.set(applicationMainClass)
}

tasks.named<org.gradle.jvm.application.tasks.CreateStartScripts>("startScripts") {
    mainClass.set(applicationMainClass)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = applicationMainClass
    }
}
