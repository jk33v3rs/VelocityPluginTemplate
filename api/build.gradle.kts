plugins {
    java
    `java-library`
}

dependencies {
    // Velocity API
    api(libs.velocity)
    api(libs.adventure.text)
    api(libs.adventure.platform)
    
    // Core utilities for API contracts
    api("org.jetbrains:annotations:24.0.1")
    
    // CompletableFuture utilities
    api("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            windowTitle = "Veloctopus Rising API ${project.version}"
            docTitle = "Veloctopus Rising API ${project.version}"
            bottom = "Copyright © 2025 Veloctopus Rising. All rights reserved."
            encoding = "UTF-8"
            charSet = "UTF-8"
            links(
                "https://docs.oracle.com/en/java/javase/17/docs/api/",
                "https://jd.advntr.dev/api/4.17.0/",
                "https://jd.papermc.io/velocity/3.4.0/"
            )
        }
    }
}
