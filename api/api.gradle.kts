plugins {
    val springVersion = "2.2.5.RELEASE"

    kotlin("jvm")
    id("org.springframework.boot") version springVersion
}

dependencies {
    // Ivana
    implementation(platform(project(":ivana-bom")))

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
}
