package com.heavyrent.equipment.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @SuppressWarnings("unused")
    @GrpcExceptionHandler(NoSuchElementException.class)
    public StatusRuntimeException handleNotFound(NoSuchElementException e) {
        return Status.NOT_FOUND
                .withDescription("Not found: " + e.getMessage())
                .asRuntimeException();
    }

    @SuppressWarnings("unused")
    @GrpcExceptionHandler(DataIntegrityViolationException.class)
    public StatusRuntimeException handleAlreadyExists(DataIntegrityViolationException e) {
        return Status.ALREADY_EXISTS
                .withDescription("Equipment with this registration number already exists")
                .asRuntimeException();
    }

    @SuppressWarnings("unused")
    @GrpcExceptionHandler(IllegalArgumentException.class)
    public StatusRuntimeException handleInvalidArgument(IllegalArgumentException e) {
        return Status.INVALID_ARGUMENT
                .withDescription("Invalid request: " + e.getMessage())
                .asRuntimeException();
    }

    @SuppressWarnings("unused")
    @GrpcExceptionHandler(Exception.class)
    public StatusRuntimeException handleInternal(Exception e) {
        return Status.INTERNAL
                .withDescription("Internal server error: " + e.getMessage())
                .asRuntimeException();
    }

    @SuppressWarnings("unused")
    @GrpcExceptionHandler(StatusRuntimeException.class)
    public StatusRuntimeException handleStatus(StatusRuntimeException e) {
        return e;
    }
}
