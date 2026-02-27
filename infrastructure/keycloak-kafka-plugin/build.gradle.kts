plugins {
    java
}

dependencies {
    // Keycloak SPI интерфейсы — provided, в рантайме их предоставляет Keycloak
    compileOnly("org.keycloak:keycloak-server-spi:24.0.0")
    compileOnly("org.keycloak:keycloak-server-spi-private:24.0.0")
    compileOnly("org.keycloak:keycloak-core:24.0.0")
    // Kafka клиент — войдёт в итоговый jar
    implementation("org.apache.kafka:kafka-clients:3.7.0")
}

// Собираем fat jar — все зависимости внутри одного файла
tasks.jar {
    archiveFileName.set("keycloak-kafka-plugin.jar")
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}