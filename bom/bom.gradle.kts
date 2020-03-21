plugins {
    `java-platform`
}

javaPlatform {
    allowDependencies()
}

dependencies {
    // Spring
    api(platform("org.springframework.boot:spring-boot-dependencies:2.2.5.RELEASE"))
}
