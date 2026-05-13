plugins {
    kotlin("jvm") version "2.3.10"
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "org.xlin.kotcode"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.xlin.kotcode.com.kotcode.MainKt")
    applicationDefaultJvmArgs = listOf("-Dtarget=windows-arm64")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    jvmArgs = listOf("-Dtarget=windows-arm64") // 尝试这个值
}


dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.koog.agents)
    implementation(libs.slf4j.nop)

}

kotlin {
    jvmToolchain(21)
}


tasks.test {
    useJUnitPlatform()
}