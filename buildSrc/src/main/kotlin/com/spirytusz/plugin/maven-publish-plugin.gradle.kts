import java.util.*

plugins {
    `maven-publish`
    `java-library`
    signing
}

java {
    withJavadocJar()
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}

ext["signing.keyId"] = null
ext["signing.password"] = null
ext["signing.secretKeyRingFile"] = null
ext["sonatype.username"] = null
ext["sonatype.password"] = null

fun getPropertyAsString(key: String): String {
    return ext[key].toString()
}

fun File.readAsProperties(): Properties {
    return reader().use { Properties().apply { load(it) } }
}

fun File.relativeTo(base: File): String {
    return toURI().relativize(base.toURI()).path
}

val rootProjectLocalProperties: File = project.rootProject.file("local.properties")
if (rootProjectLocalProperties.exists()) {
    rootProjectLocalProperties.readAsProperties().forEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["signing.keyId"] = System.getenv("KEY_ID")
    ext["signing.password"] = System.getenv("PASSWORD")
    ext["signing.secretKeyRingFile"] = System.getenv("SECRET_KEY_RING_FILE")
    ext["sonatype.username"] = System.getenv("SONATYPE_USERNAME")
    ext["sonatype.password"] = System.getenv("SONATYPE_PASSWORD")
}

val projectGradleProperties: File = project.file("gradle.properties")
if (!projectGradleProperties.exists()) {
    throw IllegalArgumentException("NOT found gradle.properties in project(${project.name})")
}
val gradleProperties = projectGradleProperties.readAsProperties()

val username = "ZSpirytus"
val repoUrl = "https://github.com/spirytusz/spi-weaver"
val myEmail = "zhangwel261717@gmail.com"
val licenseName = ""
val licenseUrl = ""
val inception = "2022"

val groupName: String = project.group.toString()
val artifactName: String = gradleProperties.getProperty("ARTIFACT")
val ver: String = project.version.toString()
val desc: String = gradleProperties.getProperty("DESC") ?: ""

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = groupName
            artifactId = artifactName
            version = ver
            if (!project.plugins.hasPlugin("java-gradle-plugin")) {
                from(components["java"])
            }

            pom {
                name.set(artifactName)

                groupId = groupName
                artifactId = artifactName
                version = ver

                description.set(desc)
                url.set(repoUrl)
                inceptionYear.set(inception)

                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }
                developers {
                    developer {
                        name.set(username)
                        email.set(myEmail)
                    }
                }
                scm {
                    connection.set("scm:git:git:github.com/spirytusz/spi-weaver.git")
                    developerConnection.set("scm:git:ssh:github.com/spirytusz/spi-weaver.git")
                    url.set(repoUrl)
                }
            }
        }
    }

    repositories {
        maven {
            name = "sonatype"
            setUrl("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = getPropertyAsString("sonatype.username")
                password = getPropertyAsString("sonatype.password")
            }
        }
    }

    repositories {
        maven {
            name = "myLocal"
            url = uri(rootProject.file(".repo"))
        }
    }
}

signing {
    sign(publishing.publications["pluginMaven"])
}