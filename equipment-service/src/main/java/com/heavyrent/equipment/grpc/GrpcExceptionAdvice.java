package com.heavyrent.equipment.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;

@Slf4j
@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler(NoSuchElementException.class)
    public StatusRuntimeException handleNotFound(NoSuchElementException e) {
        log.error("Resource not found: {}", e.getMessage(), e);
        return Status.NOT_FOUND
                .withDescription("Not found: " + e.getMessage())
                .asRuntimeException();
    }

    @GrpcExceptionHandler(DataIntegrityViolationException.class)
    public StatusRuntimeException handleAlreadyExists(DataIntegrityViolationException e) {
        log.error("Data integrity violation: {}", e.getMessage(), e);
        return Status.ALREADY_EXISTS
                .withDescription("Equipment with this registration number already exists")
                .asRuntimeException();
    }

    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleInvalidArgument(IllegalArgumentException e) {
        log.error("Invalid argument: {}", e.getMessage(), e);
        return Status.INVALID_ARGUMENT
                .withDescription("Invalid request: " + e.getMessage())
                .asRuntimeException();
    }

    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleInternal(Exception e) {
        log.error("Unhandled exception occurred", e);
        return Status.INTERNAL
                .withDescription("Internal server error")
                .asRuntimeException();
    }

    @GrpcExceptionHandler(StatusRuntimeException.class)
    public StatusRuntimeException handleStatus(StatusRuntimeException e) {
        log.error("gRPC status exception: {}", e.getStatus(), e);
        return e;
    }
}
