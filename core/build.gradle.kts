plugins {
    java
}

dependencies {
    // API dependency
    implementation(project(":api"))
    
    // Database dependencies
    implementation(libs.hikaricp)
    implementation(libs.mariadb)
    
    // Caching
    implementation(libs.jedis)
    
    // Discord integration
    implementation(libs.jda)
    
    // Utilities
    implementation(libs.gson)
    implementation(libs.caffeine)
    
    // Adventure components for text handling
    implementation(libs.adventure.text)
    implementation(libs.adventure.platform)
    
    // Velocity API (compileOnly since it's provided by the proxy)
    compileOnly(libs.velocity)
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    
    // Annotations
    compileOnly("org.jetbrains:annotations:24.0.1")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.5.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    
    test {
        useJUnitPlatform()
    }
}
