package com.heavyrent.grpc.common;

public record UserContext(
        String keycloakId,
        String role)
{}
