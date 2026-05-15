plugins {
    kotlin("jvm") version "2.2.21"
    alias(libs.plugins.kotlin.serialization)
    application
}

group = "org.xlin.kotcode"
version = "1.0-SNAPSHOT"

repositories {
//    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}

application {
    mainClass.set("org.xlin.kotcode.com.kotcode.MainKt")
    applicationDefaultJvmArgs = listOf("-Dtarget=windows-arm64")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}


dependencies {
    testImplementation(kotlin("test"))
    implementation(libs.koog.agents)
    implementation(libs.slf4j.nop)
    implementation(libs.mordant.core)
    implementation(libs.jline.core)
    implementation(libs.jansi.core)

}

kotlin {
    jvmToolchain(21)
}


tasks.test {
    useJUnitPlatform()
}