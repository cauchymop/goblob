import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.protobuf)
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_23
    targetCompatibility = JavaVersion.VERSION_23
}

kotlin {
    jvmToolchain(23)
}

dependencies {
    api(libs.lobby.core)
    implementation(libs.protobuf.java)
    implementation(libs.guava)
    implementation(libs.dagger)
    implementation(libs.kotlin.stdlib)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.protobuf)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.guava.testlib)
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.truth)
}

protobuf {
    // Configure the protoc executable
    protoc {
        // The artifact spec for the Protobuf Compiler
        artifact = "com.google.protobuf:protoc:4.30.1"
        generatedFilesBaseDir = "$projectDir/generated"
    }
    plugins {
        // Optional: an artifact spec for a protoc plugin, with "grpc" as
        // the identifier, which can be referred to in the "plugins"
        // container of the "generateProtoTasks" closure.
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.57.0"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without
                // options. Note the braces cannot be omitted, otherwise the
                // plugin will not be added. This is because of the implicit way
                // NamedDomainObjectContainer binds the methods.
                id("grpc") { }
            }
        }
    }
}