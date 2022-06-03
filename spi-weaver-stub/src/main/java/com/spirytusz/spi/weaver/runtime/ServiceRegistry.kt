@file:JvmName("ServiceRegistry")

package com.spirytusz.spi.weaver.runtime

import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap

val sServicesPool: Map<Class<*>, Map<String, Callable<*>>> = ConcurrentHashMap()

