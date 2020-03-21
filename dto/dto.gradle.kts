plugins {
    kotlin("jvm")
}

dependencies {
    // Ivana
    implementation(platform(project(":ivana-bom")))

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
}
