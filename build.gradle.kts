import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    kotlin("jvm") version "1.9.0"
    application
    id("gradle-plugin")
    id("io.gitlab.arturbosch.detekt") version ("1.23.3")
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(
        file("detekt.yml")
    )
}

ktlint {
    android.set(false)
    ignoreFailures.set(true)
    reporters {
        reporter(ReporterType.HTML)
    }
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

application {
    mainClass.set("MainKt")
}
