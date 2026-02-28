package com.heavyrent.keycloak;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;

import java.util.NoSuchElementException;

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
                    "{" +
                            "\"userId\":\"%s\"," +
                            "\"email\":\"%s\"" +
                            ",\"eventType\":\"USER_REGISTERED\"}",
                    event.getUserId(),
                    event.getDetails().get("email")
            );
            producer.send(new ProducerRecord<>(topic, event.getUserId(), payload));
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        if (adminEvent.getResourceType() == ResourceType.USER
                && adminEvent.getOperationType() == OperationType.CREATE
                && includeRepresentation) {
            try {

                // Парсим representation — это JSON с данными пользователя
                String representation = adminEvent.getRepresentation();
                String userId = adminEvent.getResourcePath().replace("users/", "");
                JsonNode root = new ObjectMapper().readTree(representation);
                JsonNode rolesArray = root.path("realmRoles");

                if (rolesArray.isEmpty()) throw new NoSuchElementException("realmRoles is empty");

                String role = rolesArray.get(0).asText();

                // Простой способ — передаём representation как есть
                // user-service сам распарсит нужные поля
                String payload = String.format(
                        "{\"userId\":\"%s\",\"eventType\":\"USER_REGISTERED\",\"data\":%s,\"role\":\"%s\"}",
                        userId,
                        representation,
                        role
                );
                producer.send(new ProducerRecord<>(topic, userId, payload));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void close() {
        // Producer закрывает фабрика
    }
}