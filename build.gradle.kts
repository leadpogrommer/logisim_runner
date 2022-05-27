plugins {
    kotlin("jvm") version "1.6.10"
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "ru.leadpogrommer.cdm8e.runner"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(files("lib/logisim.jar"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation ("com.google.code.gson:gson:2.9.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.10")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.jar{
    manifest.attributes["Main-Class"] = "ru.leadpogrommer.cdm8e.runner.MainKt"
}