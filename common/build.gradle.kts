import com.google.protobuf.gradle.proto
import com.google.protobuf.gradle.protobuf
import com.google.protobuf.gradle.protoc

plugins {
    java
}

group = "ru.mse.itmo"
version = "1.0-SNAPSHOT"

apply {
    plugin("com.google.protobuf")
}

sourceSets {
    main {
        proto {
            srcDir("proto")
        }
    }
    test {
        java.srcDir("test")
        resources.srcDir("testResources")
    }
}

//dependencies {
//    implementation("com.google.protobuf:protobuf-java:3.19.1")
//}

protobuf {
    generatedFilesBaseDir = "$projectDir/src/"
    protoc {
        artifact = "com.google.protobuf:protoc:3.19.0"
    }
}

tasks.test {
    useJUnitPlatform()
}
