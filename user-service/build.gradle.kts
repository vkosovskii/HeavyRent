plugins {
    java
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":common"))
    // Web — REST endpoints
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Resource Server — check JWT token
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    // JPA + Hibernate — for DB
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}