package com.heavyrent.keycloak;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

public class KafkaEventListenerProvider implements EventListenerProvider {

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public KafkaEventListenerProvider(KafkaProducer<String, String> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public void onEvent(Event event) {
        // Нас интересует только регистрация пользователя
        if (EventType.REGISTER.equals(event.getType())) {
            String payload = String.format(
                    "{\"userId\":\"%s\",\"email\":\"%s\",\"eventType\":\"USER_REGISTERED\"}",
                    event.getUserId(),
                    event.getDetails().get("email")
            );
            producer.send(new ProducerRecord<>(topic, event.getUserId(), payload));
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        if (adminEvent.getResourceType() == ResourceType.USER
                && adminEvent.getOperationType() == OperationType.CREATE) {

            String userId = adminEvent.getResourcePath().replace("users/", "");
            String payload = String.format(
                    "{\"userId\":\"%s\",\"eventType\":\"USER_REGISTERED\"}",
                    userId
            );
            producer.send(new ProducerRecord<>(topic, userId, payload));
        }
    }

    @Override
    public void close() {
        // Producer закрывает фабрика
    }
}