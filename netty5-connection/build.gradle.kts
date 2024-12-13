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
            name = "dataflair-public"
            url = uri(
                if (version.toString()
                        .endsWith("SNAPSHOT")
                ) "https://rp1.dataflair.cloud/repository/dataflair-public-development/"
                else "https://rp1.dataflair.cloud/repository/dataflair-public-release/"
            )
            credentials {
                username = project.findProperty("dataflair_rp1_admin_username") as String?
                password = project.findProperty("dataflair_rp1_admin_password") as String?
            }
        }
    }

    publications {
        create<MavenPublication>("dataflair-public") {
            groupId = groupId
            artifactId = artifactId
            version = version

            from(components["java"])
        }
    }
}