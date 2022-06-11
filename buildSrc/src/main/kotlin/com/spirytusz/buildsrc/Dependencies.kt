package com.spirytusz.buildsrc

object Versions {
    const val kotlin_version = "1.5.20"
    const val agp_version = "7.1.2"
    const val androidx_appcompat = "1.3.1"
    const val androidx_constraintlayout = "2.1.1"
    const val material_design = "1.4.0"
    const val core_ktx = "1.7.0"
    const val glide = "4.13.0"
    const val asm = "9.1"
    const val junit = "4.12"
    const val gson = "2.8.6"
    const val common_codec = "1.15"
    const val common_io = "2.6"
    const val booster = "1.3.0"
    const val spi_weaver = "0.0.1"
}

object Dependencies {

    const val agp = "com.android.tools.build:gradle:${Versions.agp_version}"
    const val kotlin_gradle_plugin =
        "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin_version}"
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$${Versions.kotlin_version}"
    const val androidx_constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.androidx_constraintlayout}"
    const val androidx_appcompat = "androidx.appcompat:appcompat:${Versions.androidx_appcompat}"
    const val material_design = "com.google.android.material:material:${Versions.material_design}"
    const val core_ktx = "androidx.core:core-ktx:${Versions.core_ktx}"
    const val glide = "com.github.bumptech.glide:glide:${Versions.glide}"
    const val asm = "org.ow2.asm:asm-commons:${Versions.asm}"
    const val junit = "junit:junit:${Versions.junit}"
    const val gson = "com.google.code.gson:gson:${Versions.gson}"
    const val common_codec = "commons-codec:commons-codec:${Versions.common_codec}"
    const val common_io = "commons-io:commons-io:${Versions.common_io}"
    const val booster_annotation = "com.spirytusz:booster-annotation:${Versions.booster}"
    const val booster_processor = "com.spirytusz:booster-processor:${Versions.booster}"

    const val spi_weaver_compiler = "com.spirytusz:spi-compiler:${Versions.spi_weaver}"
    const val spi_weaver_runtime = "com.spirytusz:spi-runtime:${Versions.spi_weaver}"
}