package com.spirytusz.spi.weaver.config

import org.gradle.api.Project
import java.io.Serializable
import java.util.regex.Pattern

class ConfigProvider(project: Project) : Serializable {

    var debuggable: Boolean = false
        private set

    lateinit var blackList: Set<Pattern>

    init {
        project.extensions.create("spi", ServiceProviderExtension::class.java)
        project.afterEvaluate {
            val spiExtension = project.extensions.getByType(ServiceProviderExtension::class.java)
            debuggable = spiExtension.debuggable
            blackList = spiExtension.blackListClasses.map { Pattern.compile(it) }.toSet()
        }
    }

    override fun toString(): String {
        return "ConfigProvider(debuggable=$debuggable, blackList=$blackList)"
    }

}