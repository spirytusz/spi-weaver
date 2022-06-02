package com.spirytusz.buildsrc

object Versions {
    const val kotlin_version = "1.5.20"
    const val agp_version = "7.1.2"
    const val asm = "7.0"
    const val gson = "2.8.6"
    const val common_codec = "1.15"
    const val common_io = "2.6"
}

object Dependencies {

    const val agp = "com.android.tools.build:gradle:${Versions.agp_version}"
    const val kotlin_gradle_plugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin_version}"
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin_version}"
    const val asm = "org.ow2.asm:asm-util:${Versions.asm}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val common_codec = "commons-codec:commons-codec:${Versions.common_codec}"
    const val common_io = "commons-io:commons-io:${Versions.common_io}"
}