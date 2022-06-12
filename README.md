# SpiWeaver

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.spirytusz/spi-compiler/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.spirytusz/spi-compiler)

**SpiWeaver**能够帮助你在编译期收集所有服务；在运行时统一管理，提供一个获取对应的实现类实例的能力。

# Why

## 问题

在使用组件化的架构时，我们不得不面临两个问题：

1. 一方面，我们希望某个组件对外只暴露接口，不暴露具体的实现；
2. 另一方面，我们希望能够收集所有暴露接口的实现，统一起来进行管理；

矛盾的地方在于：我们希望在源码期做到隔离，但又希望能够收集被隔离的实现类，收拢在一起统一管理。

## 方案

仅仅通过反射来获取源码期隔离的实现类实例，虽然能做到源码期隔离，但反射在运行时可能会影响性能，并且不容易统一管理；

使用注解处理器收集所有的实现类，能做到一定程度上的统一管理，也能做到源码期隔离；但由于注解处理器是模块级别的工具，最终我们仍然需要在app模块下手动收集一次所有模块的实现类，并不能完全的统一管理。

AGP提供了Transform机制，这个机制提供了对class文件的修改和生成的能力。由于此时源码已经被编译成为了class文件，因此也没有了模块的概念。因此我们能够收集实现类完毕后，生成一个统一管理类，在运行期获取该类的实例，也就能过获取源码期隔离的实现类实例了。

# Download

根目录下的`build.gradle`文件：

```groovy
buildscripts {
    dependencies {
        // 添加classpath，以便gradle能够识别到plugin
        classpath 'com.spirytusz:spi-compiler:0.0.1'
    }
}
```

# Usage

在`build.gradle`文件：

```groovy
plugins {
    id 'com.spirytusz.spi.weaver'
}

dependencies {
    implementation 'com.spirytusz:spi-runtime:0.0.1'
}
```

为服务接口加上<b>@Service</b>注解：

```kotlin
@Service
interface IPushService {
    fun init()
}

@Service
interface ILocationService {
    fun obtainSingleLocation(): Location
}
```

并为服务实现类加上<b>@ServiceImpl</b>的注解：

```kotlin
@ServiceImpl
class PushService : IPushService {
    override fun init() {
        Log.d("PushService", "init")
    }
}

@ServiceImpl(alias = "coarse")
class CoarseLocationService : ILocationService {
    override fun obtainSingleLocation(): Location {
        return Location(LocationManager.NETWORK_PROVIDER)
    }
}

@ServiceImpl(alias = "fine")
class FineLocationService : ILocationService {
    override fun obtainSingleLocation(): Location {
        return Location(LocationManager.GPS_PROVIDER)
    }
}

@ServiceImpl(alias = "fused")
class FusedLocationManager : ILocationService {
    @SuppressLint("InlinedApi")
    override fun obtainSingleLocation(): Location {
        return Location(LocationManager.FUSED_PROVIDER)
    }
}
```

使用**ServiceProvider**来获取服务实现：

```kotlin
val pushService = ServiceProvider.of(IPushService::class.java)
val fineLocationService = ServiceProvider.of(ILocationService::class.java, "fine")

pushService?.init()
fineLocationService?.obtainSingleLcation()
```

# License

```
MIT License

Copyright (c) 2021 ZSpirytus

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```