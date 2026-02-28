plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Internal modules
    implementation(project(":grpc-contracts"))
    // gRPC server
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
    // JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // Spring-Kafka starter
    implementation("org.springframework.kafka:spring-kafka")
    // PostgreSQL driver
    runtimeOnly("org.postgresql:postgresql")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}