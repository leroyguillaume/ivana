import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.3.70" apply false
}

allprojects {
    val baseVersion = project.property("ivana.version").toString()

    group = "io.ivana"
    version = if (currentBranchName() == "master") baseVersion else "$baseVersion.${currentCommitShortHash()}"

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks {
    wrapper {
        gradleVersion = "6.2"
    }
}

fun currentBranchName(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        executable("git")
        args(listOf("rev-parse", "--abbrev-ref", "HEAD"))
        standardOutput = stdout
    }
    return stdout.toString(Charsets.UTF_8).trim()
}

fun currentCommitShortHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        executable("git")
        args(listOf("rev-parse", "--short", "HEAD"))
        standardOutput = stdout
    }
    return stdout.toString(Charsets.UTF_8).trim()
}
