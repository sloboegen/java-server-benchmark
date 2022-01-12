buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.17")
    }
}

rootProject.name = "java-server-benchmark"
include("server")
include("client")
include("common")
