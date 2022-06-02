@file:JvmName("ServiceRegistry")

package com.spirytusz.spi.weaver.runtime

import java.util.concurrent.Callable

val sServicesPool: Map<Class<*>, Map<String, Callable<*>>> = mapOf()

