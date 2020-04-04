import com.moowork.gradle.node.npm.NpmTask

plugins {
  id("com.github.node-gradle.node") version "2.2.0"
}

node {
  version = "12.16.1"
  npmVersion = "6.13.4"

  download = true
}

tasks {
  getByName("npm_install") {
    group = "build"

    outputs.upToDateWhen { projectDir.resolve("node_modules").exists() }
  }

  create<NpmTask>("assemble") {
    group = "build"
    dependsOn("npm_install")

    setArgs(listOf("run", "build"))

    outputs.upToDateWhen { buildDir.exists() }
  }

  create<NpmTask>("check") {
    group = "verification"
    dependsOn("assemble")

    setArgs(listOf("run", "test", "--", "--no-watch", "--no-progress", "--browsers=ChromeHeadlessCI"))
  }

  create("clean") {
    group = "build"

    doLast {
      buildDir.deleteRecursively()
      projectDir.resolve("node_modules").deleteRecursively()
    }
  }
}
