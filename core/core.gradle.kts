plugins {
    kotlin("jvm")
}

dependencies {
    // Ivana
    implementation(platform(project(":ivana-bom")))

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // Kotlintest
    val kotlintestVersion = "3.4.2"
    testImplementation("io.kotlintest:kotlintest-core:$kotlintestVersion")
}
