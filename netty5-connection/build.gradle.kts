plugins {
    id("java")
}

dependencies {
    compileOnly(libs.utility.lombok)
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.gson)
    implementation(libs.netty5.all)
    annotationProcessor(libs.utility.lombok)
}

publishing {
    repositories {
        maven {
            name = "lumesolutions"
            url = uri(
                if (version.toString()
                        .endsWith("SNAPSHOT")
                ) "https://repository02.lumesolutions.de/repository/lumesolutions-public-dev/" else
                    "https://repository02.lumesolutions.de/repository/lumesolutions-public-productive/"
            )
            credentials {
                username = project.findProperty("lumesolutions_user") as String?
                password = project.findProperty("lumesolutions_password") as String?
            }
        }
    }

    publications {
        create<MavenPublication>("lumesolutions") {
            groupId = groupId
            artifactId = artifactId
            version = version

            from(components["java"])
        }
    }
}