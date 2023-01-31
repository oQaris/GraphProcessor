import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("kapt") version "1.6.10"
    application
}

group = "me.oqaris"
version = "0.2.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.1")
    // Console
    kapt("info.picocli:picocli-codegen:4.7.1")
    implementation("info.picocli:picocli:4.7.1")
    implementation("info.picocli:picocli-jansi-graalvm:1.2.0")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    // Algorithm
    implementation("com.udojava:EvalEx:2.7")
    implementation("com.github.shiguruikai:combinatoricskt:1.6.0")
    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.slf4j:slf4j-api:2.0.3")
    implementation("ch.qos.logback:logback-classic:1.4.4")
}

fun Project.kapt(setup: KaptExtension.() -> Unit) = the<KaptExtension>().setup()

kapt {
    useBuildCache = false
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "console.MainKt"
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
