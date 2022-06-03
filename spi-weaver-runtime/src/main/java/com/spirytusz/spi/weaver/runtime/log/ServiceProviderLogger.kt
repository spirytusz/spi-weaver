package com.spirytusz.spi.weaver.runtime.log

internal object ServiceProviderLogger : LogProxy {

    var logProxy: LogProxy? = null

    override fun v(tag: String, msg: String) {
        logProxy?.v(tag, msg)
    }

    override fun d(tag: String, msg: String) {
        logProxy?.d(tag, msg)
    }

    override fun i(tag: String, msg: String) {
        logProxy?.i(tag, msg)
    }

    override fun w(tag: String, msg: String) {
        logProxy?.w(tag, msg)
    }

    override fun e(tag: String, msg: String) {
        logProxy?.e(tag, msg)
    }
}