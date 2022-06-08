plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish-plugin")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

dependencies {
    compileOnly(project(":spi-weaver-stub"))
}