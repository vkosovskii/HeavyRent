package com.heavyrent.keycloak;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

import java.util.Properties;

public class KafkaEventListenerProviderFactory implements EventListenerProviderFactory {

    private KafkaProducer<String, String> producer;
    private String topic;

    @Override
    public EventListenerProvider create(KeycloakSession session) {
        // Каждое событие получает новый Provider, но один и тот же Producer
        return new KafkaEventListenerProvider(producer, topic);
    }

    @Override
    public void init(Config.Scope config) {
        // Читаем конфиг из переменных окружения Keycloak
        String bootstrapServers = System.getenv()
                .getOrDefault("KAFKA_BOOTSTRAP_SERVERS", "kafka:9092");
        topic = System.getenv()
                .getOrDefault("KAFKA_TOPIC_USER_REGISTERED", "user.registered");

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(props);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {}

    @Override
    public void close() {
        // Вот здесь закрываем — фабрика живёт всё время работы Keycloak
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    public String getId() {
        // Это имя по которому Keycloak находит наш listener
        return "kafka-event-listener";
    }
}