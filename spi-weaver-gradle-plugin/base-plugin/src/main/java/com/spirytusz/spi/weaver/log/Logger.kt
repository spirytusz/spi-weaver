package com.spirytusz.spi.weaver.log

object Logger {

    private const val LOG_PREFIX = "[SpiWeaver]"

    var isDebuggable = false

    fun d(tag: String, msgProvider: () -> String) {
        if (isDebuggable) {
            println("$LOG_PREFIX D/$tag: ${msgProvider.invoke()}")
        }
    }

    fun i(tag: String, msgProvider: () -> String) {
        println("$LOG_PREFIX I/$tag: ${msgProvider.invoke()}")
    }

    fun w(tag: String, msgProvider: () -> String) {
        println("$LOG_PREFIX W/$tag: ${msgProvider.invoke()}")
    }

    fun e(tag: String, msgProvider: () -> String) {
        error("$LOG_PREFIX E/$tag: ${msgProvider.invoke()}")
    }
}