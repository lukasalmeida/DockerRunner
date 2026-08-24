plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.olezelerunner.dockerruner"
version = "1.0.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    intellijPlatform {
        create("IC", "2024.1")
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginVerification {
        ides {
            recommended()
        }
    }
}