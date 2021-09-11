import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("kapt") version "1.5.30"
    application
}

group = "me.oqaris"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("info.picocli:picocli:4.6.1")
    implementation("info.picocli:picocli-jansi-graalvm:1.2.0")
    implementation("org.fusesource.jansi:jansi:1.18")
    kapt("info.picocli:picocli-codegen:4.6.1")

    implementation("com.udojava:EvalEx:2.6")
    implementation("org.slf4j:slf4j-simple:1.7.28")
}

fun Project.kapt(setup: KaptExtension.() -> Unit) = the<KaptExtension>().setup()

kapt {
    useBuildCache = false
    arguments {
        arg("project", "${project.group}/${project.name}")
    }
}

tasks.withType<Jar> {
    // Otherwise, you'll get a "No main manifest attribute" error
    manifest {
        attributes["Main-Class"] = "console.MainKt"
    }

    // To add all the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
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
