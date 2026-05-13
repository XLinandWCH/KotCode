pluginManagement {
    repositories {
        gradlePluginPortal()  // 必须要有这个！
        mavenCentral()
        google()  // 可选，但如果用了 Android 插件需要
    }

    // 可选：插件版本管理
    plugins {
        kotlin("jvm") version "2.3.10"
        kotlin("plugin.serialization") version "2.3.10"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "KotCode"