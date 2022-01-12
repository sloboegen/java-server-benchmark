plugins {
    java
}

group = "ru.mse.itmo"
version = "1.0-SNAPSHOT"

dependencies {
    implementation(project(":common"))
}

tasks.test {
    useJUnitPlatform()
}
