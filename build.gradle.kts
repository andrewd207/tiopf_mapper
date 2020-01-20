plugins {
    kotlin("jvm") version "1.3.61"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    //implementation(fileTree(mapOf("dir" to "jars", "include" to listOf("*.jar"))))
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
        // we don't need any database stuff and Firebird will cause a linking exception
        exclude(
            "*/tiOPF/Layers/*.kt",
            "*/tiOPF/SQL*.kt",
            "*/tiOPF/Query?.kt",
            "*/tiOPF/Mapper/*Filter*.kt"
        )
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    jar {
        manifest {
            attributes (
                "Main-Class" to "MapperApplicationKt"
            )
        }

        from(sourceSets.main.get().output)

        dependsOn(configurations.runtimeClasspath)
        from({ configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }  })

    }

}
