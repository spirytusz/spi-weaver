plugins {
    id("org.jetbrains.kotlin.jvm")
    id("maven-publish-plugin")
}

dependencies {
    compileOnly(project(":spi-weaver-stub"))
}