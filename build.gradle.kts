group = "io.yz"
version = "0.1-SNAPSHOT"
description = "My HTTP Client"

repositories {
    mavenCentral()
}

plugins {
    java
    kotlin("jvm") version "1.6.10"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(platform("io.netty:netty-bom:4.1.73.Final"))
    testImplementation(platform("org.junit:junit-bom:5.8.2"))

    implementation(group = "io.netty", name = "netty-common")
    implementation(group = "io.netty", name = "netty-buffer")
    implementation(group = "io.netty", name = "netty-resolver")
    implementation(group = "io.netty", name = "netty-transport")
    implementation(group = "io.netty", name = "netty-handler")

    implementation(group = "org.slf4j", name = "slf4j-api", version = "1.7.32")

    runtimeOnly(group = "ch.qos.logback", name = "logback-classic", version = "1.2.10")

    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter")
}

tasks {
    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "1.8"
        incremental = true
    }
}
