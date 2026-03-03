import com.google.protobuf.gradle.id

plugins {
    java
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    implementation("io.grpc:grpc-protobuf:1.68.0")
    implementation("io.grpc:grpc-stub:1.68.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("com.google.api.grpc:proto-google-common-protos:2.29.0")
    protobuf ("com.google.api.grpc:proto-google-common-protos:2.29.0")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.68.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.plugins {
                id("grpc")
            }
            task.generateDescriptorSet = true
            task.descriptorSetOptions.includeImports = true
            task.descriptorSetOptions.path = "$projectDir/build/descriptors/proto.pb"
        }
    }
}