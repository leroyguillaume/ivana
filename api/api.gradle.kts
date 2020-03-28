plugins {
    // Kotlin
    kotlin("jvm")
    kotlin("plugin.spring")

    // Liquibase
    id("org.liquibase.gradle") version "2.0.2"

    // Spring
    val springVersion = "2.2.5.RELEASE"
    id("org.springframework.boot") version springVersion
}

liquibase {
    activities {
        register("main") {
            val dbProps = dbProperties()
            arguments = mapOf(
                "logLevel" to "info",
                "changeLogFile" to "src/main/resources/db/changelog.yml",
                "url" to dbProps.jdbcUrl,
                "username" to dbProps.username,
                "password" to dbProps.password
            )
        }
    }
}

dependencies {
    // Auth0
    val javaJwtVersion = "3.10.1"
    implementation("com.auth0:java-jwt:$javaJwtVersion")

    // Ivana
    implementation(platform(project(":ivana-bom")))
    implementation(project(":ivana-core"))
    implementation(project(":ivana-dto"))
    liquibaseRuntime(platform(project(":ivana-bom")))

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))

    // Kotlintest
    val kotlintestVersion = "3.4.2"
    testImplementation("io.kotlintest:kotlintest-core:$kotlintestVersion")

    // Liquibase
    implementation("org.liquibase:liquibase-core")
    liquibaseRuntime("org.liquibase:liquibase-core")

    // Logback
    liquibaseRuntime("ch.qos.logback:logback-classic")

    // Mockk
    val mockkVersion = "1.9.3"
    testImplementation("io.mockk:mockk:$mockkVersion")

    // Mockito Kotlin
    val mockitoKotlin = "2.2.0"
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:$mockitoKotlin")

    // PostgreSQL
    implementation("org.postgresql:postgresql")
    liquibaseRuntime("org.postgresql:postgresql")

    // SnakeYAML
    liquibaseRuntime("org.yaml:snakeyaml")

    // Spring
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit")
    }
}

tasks {
    bootRun {
        jvmArgs = listOf("-Djava.net.preferIPv4Stack=true")
    }

    processResources {
        filesMatching("**/application.yml") {
            expand("database" to dbProperties())
        }
    }

    test {
        dependsOn("update")

        val dbProps = dbProperties()
        systemProperty("database.url", dbProps.jdbcUrl)
        systemProperty("database.username", dbProps.username)
        systemProperty("database.password", dbProps.password)
    }

    create<Exec>("dropDatabase") {
        group = "database"
        val dbProps = dbProperties()
        executable = "psql"
        args(
            listOf(
                "-h", dbProps.host,
                "-p", dbProps.port,
                "-U", dbProps.username,
                dbProps.name,
                "-c", "DROP SCHEMA \"public\" CASCADE; CREATE SCHEMA \"public\";"
            )
        )
        environment("PGPASSWORD", dbProps.password)
    }
}

data class DbProperties(
    val host: String,
    val port: Int,
    val name: String,
    val username: String,
    val password: String
) {
    val jdbcUrl = "jdbc:postgresql://$host:$port/$name"
}

fun dbProperties() = DbProperties(
    host = project.property("database.host") as String,
    port = (project.property("database.port") as String).toInt(),
    name = project.property("database.name") as String,
    username = project.property("database.username") as String,
    password = project.property("database.password") as String
)
