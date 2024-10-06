plugins {
    id("java")
}

dependencies {
    implementation(libs.utility.lombok)
    implementation(libs.jetbrains.annotations)
    implementation(project(":netty5-connection"))
    annotationProcessor(libs.utility.lombok)
}