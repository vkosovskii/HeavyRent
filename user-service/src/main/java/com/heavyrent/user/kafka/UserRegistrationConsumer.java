package com.heavyrent.user.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heavyrent.user.dto.KeycloakRequest;
import com.heavyrent.user.model.UserProfile;
import com.heavyrent.user.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class UserRegistrationConsumer {


    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final UserProfileService userProfileService;

    public UserRegistrationConsumer(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @KafkaListener(topics = "user.registered", groupId = "user-service")
    public void handleUserRegistered(String message) {
        try {
            log.info("Received event from Kafka: {}", message);

            JsonNode root = objectMapper.readTree(message);
            String userId = root.get("userId").asText();
            JsonNode data = root.get("data");

            if (data == null) {
                log.warn("No data in event for userId: {}", userId);
                throw new IllegalArgumentException("Data is required for registration");
            }

            String email = data.get("email").asText();

            JsonNode phoneNode = data.path("attributes").path("phone");
            String phone = Optional.ofNullable(phoneNode.get(0))
                    .map(JsonNode::asText)
                    .orElseThrow(() -> new IllegalArgumentException("Phone is required for registration"));

            String role = Optional.of(root.get("role").asText())
                    .filter(r -> !r.isEmpty())
                    .orElseThrow(() -> new IllegalArgumentException("Role is required for registration"));

            log.info("Processing registration for userId: {}, email: {}", userId, email);

            KeycloakRequest keycloakRequest = KeycloakRequest.builder()
                    .keycloakId(userId)
                    .email(email)
                    .firstName(data.path("firstName").asText())
                    .lastName(data.path("lastName").asText())
                    .phone(phone)
                    .role(UserProfile.Role.valueOf(role))
                    .build();

            userProfileService.createUserProfile(keycloakRequest);

        } catch (Exception e) {
            log.error("Failed to process user registration event: {}", message, e);
        }
    }
}