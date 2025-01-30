// version of LWJGL to use. Recommended to be latest.
val lwjglVersion = "3.3.4"

// list of modules that this implementation needs to work.
val lwjglModules = listOf("nanovg", "opengl", "stb", "glfw", null)

// list of platforms that this implementation will support.
val nativePlatforms = listOf("windows", "linux", "macos", "macos-arm64")

dependencies {
    for (module in lwjglModules) {
        val dep = if(module == null) "org.lwjgl:lwjgl:$lwjglVersion" else "org.lwjgl:lwjgl-$module:$lwjglVersion"
        implementation(dep)
        for (platform in nativePlatforms) {
            runtimeOnly("$dep:natives-$platform")
        }
    }

    testImplementation("com.electronwill.night-config:core:3.6.0")
    testImplementation("com.electronwill.night-config:json:3.6.0")
    testImplementation("com.electronwill.night-config:toml:3.6.0")
    testImplementation("com.electronwill.night-config:yaml:3.6.0")
}

tasks.register<Jar>("testJar") {
    dependsOn(":jar")
    manifest {
        attributes["Main-Class"] = "org.polyfrost.polyui.Testv2Kt"
    }
    from(sourceSets.test.get().output)
    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().mapNotNull { if (!it.exists()) null else if (it.isDirectory) it else zipTree(it) })
    from(configurations.testRuntimeClasspath.get().mapNotNull { if (!it.exists()) null else if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

