plugins {
    id("java")
}

group = "io.github.unjoinable"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.minestom:minestom:2025.07.03-1.21.5")
    implementation("ch.qos.logback:logback-classic:1.5.18")
}