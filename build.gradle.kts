import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "de.lumesolutions.netty5-api"
    version = "1.0.2"

    repositories {
        maven {
            name = "lumesolutions"
            url = uri("https://repository02.lumesolutions.de/repository/lumesolutions-intern-release/")
            credentials {
                username = project.findProperty("lumesolutions_user") as String?
                password = project.findProperty("lumesolutions_password") as String?
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        mavenCentral()
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = JavaVersion.VERSION_22.toString()
        targetCompatibility = JavaVersion.VERSION_22.toString()
        options.encoding = "UTF-8"
    }

    tasks.register<Jar>("sourcesJar") {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.jar {
        manifest {
            attributes(
                "Implementation-Version" to project.version
            )
        }
    }

    tasks.named<ShadowJar>("shadowJar") {
        mergeServiceFiles()
        manifest {
            attributes(
                "Implementation-Version" to project.version
            )
        }
    }
}