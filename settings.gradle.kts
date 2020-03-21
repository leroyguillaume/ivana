fun includeModule(name: String) {
    val projectName = "${rootProject.name}-$name"
    include(projectName)
    val project = project(":$projectName")
    project.projectDir = File(name)
    project.buildFileName = "$name.gradle.kts"
}

rootProject.name = "ivana"

arrayOf("api", "bom", "core", "dto").forEach { includeModule(it) }
