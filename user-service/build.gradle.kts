plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

configurations.all {
    resolutionStrategy {
        force("io.grpc:grpc-api:1.68.0")
        force("io.grpc:grpc-core:1.68.0")
        force("io.grpc:grpc-stub:1.68.0")
        force("io.grpc:grpc-protobuf:1.68.0")
    }
}

dependencies {
    // Internal modules
    implementation(project(":grpc-contracts"))
    // gRPC server
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
    // JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Spring-Kafka starter
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.kafka:spring-kafka:4.0.2")
    implementation("org.springframework.boot:spring-boot-kafka:4.0.2")
    // PostgreSQL driver
    runtimeOnly("org.postgresql:postgresql")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}