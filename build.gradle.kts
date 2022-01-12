plugins {
    java
    application
    id("com.google.protobuf") apply false
}

group = "ru.mse.itmo"
version = "1.0-SNAPSHOT"

configure<JavaApplication> {
    mainClass.set("ru.mse.itmo.Main")
}

repositories {
    mavenCentral()
}

subprojects {
    apply {
        plugin("java")
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.google.protobuf:protobuf-java:3.19.1")
    }

    sourceSets {
        main {
            java.srcDir("src")
            resources.srcDir("resources")
        }
        test {
            java.srcDir("test")
            resources.srcDir("testResources")
        }
    }
}

dependencies {
    implementation(project(":server"))
    implementation(project(":client"))
    implementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}
