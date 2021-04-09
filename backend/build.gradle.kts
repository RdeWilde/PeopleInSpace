plugins {
    id("kotlin-platform-jvm")
    application
    kotlin("plugin.serialization")
}

val exposedVersion: String by project
dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}")

    implementation("io.ktor:ktor-server-core:${Versions.ktor}")
    implementation("io.ktor:ktor-server-netty:${Versions.ktor}")
    implementation("io.ktor:ktor-serialization:${Versions.ktor}")

    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.kotlinxSerialization}") // JVM dependency
    implementation("io.ktor:ktor-websockets:${Versions.ktor}")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")

    implementation("com.h2database:h2:1.4.197")

    implementation(project(":common"))
}

application {
    mainClass.set("ServerKt")
}