plugins {
    kotlin("jvm")
}

dependencies {
    // Ivana
    implementation(platform(project(":ivana-bom")))

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // Kotlintest
    val kotlintestVersion = "3.4.2"
    testImplementation("io.kotlintest:kotlintest-core:$kotlintestVersion")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind")
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // Validation API
    implementation("javax.validation:validation-api")
}
