plugins {
    java
}

dependencies {
    implementation(project(":api"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
}
