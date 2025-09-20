plugins {
    id("chirp.spring-boot-app")
}

group = "xyz.zaph"
version = "0.0.1-SNAPSHOT"
description = "chirp"

dependencies {

    implementation(projects.chat)
    implementation(projects.common)
    implementation(projects.user)
    implementation(projects.notification)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)

    runtimeOnly(libs.postgresql)
}
