plugins {
    id("java")
}

dependencies {
    compileOnly(libs.utility.lombok)
    compileOnly(libs.jetbrains.annotations)
    implementation(libs.netty5.all)
    annotationProcessor(libs.utility.lombok)
}