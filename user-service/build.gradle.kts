plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // Internal modules
    implementation(project(":common"))
    implementation(project(":grpc-contracts"))
    // gRPC server
    implementation("net.devh:grpc-server-spring-boot-starter:3.1.0.RELEASE")
    // JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // PostgreSQL driver
    runtimeOnly("org.postgresql:postgresql")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}