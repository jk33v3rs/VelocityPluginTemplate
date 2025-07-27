plugins {
    java
}

dependencies {
    compileOnly(libs.velocity)
    implementation(project(":api"))
    implementation(project(":modules:common-module"))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
}
