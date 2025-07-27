plugins {
    java
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    // Core module dependency
    implementation(project(":core"))
    implementation(project(":api"))
    
    // Velocity API
    implementation(libs.velocity.api)
    
    // Discord integration
    implementation(libs.jda)
    
    // Lombok
    implementation(libs.lombok)
    annotationProcessor(libs.lombok)
    
    // Logging
    implementation(libs.slf4j.api)
    
    // Utilities
    implementation(libs.guava)
    implementation(libs.gson)
    
    // Testing
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
