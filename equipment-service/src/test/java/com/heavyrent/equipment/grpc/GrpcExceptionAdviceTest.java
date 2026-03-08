package com.heavyrent.equipment.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GrpcExceptionAdviceTest {

    private GrpcExceptionAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new GrpcExceptionAdvice();
    }

    @Test
    void handleNotFound_shouldReturnNotFoundStatus() {
        NoSuchElementException exception = new NoSuchElementException("Equipment not found: 123");

        StatusRuntimeException result = advice.handleNotFound(exception);

        assertEquals(Status.NOT_FOUND.getCode(), result.getStatus().getCode());
        assertEquals("Not found: Equipment not found: 123", result.getStatus().getDescription());
    }

    @Test
    void handleAlreadyExists_shouldReturnAlreadyExistsStatus() {
        DataIntegrityViolationException exception =
                new DataIntegrityViolationException("duplicate key value violates unique constraint");

        StatusRuntimeException result = advice.handleAlreadyExists(exception);

        assertEquals(Status.ALREADY_EXISTS.getCode(), result.getStatus().getCode());
        assertEquals("Equipment with this registration number already exists", result.getStatus().getDescription());
    }

    @Test
    void handleInvalidArgument_shouldReturnInvalidArgumentStatus() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid UUID string");

        StatusRuntimeException result = advice.handleInvalidArgument(exception);

        assertEquals(Status.INVALID_ARGUMENT.getCode(), result.getStatus().getCode());
        assertEquals("Invalid request: Invalid UUID string", result.getStatus().getDescription());
    }

    @Test
    void handleInternal_shouldReturnInternalStatus() {
        Exception exception = new Exception("Something went wrong");

        StatusRuntimeException result = advice.handleInternal(exception);

        assertEquals(Status.INTERNAL.getCode(), result.getStatus().getCode());
        assertEquals("Internal server error", result.getStatus().getDescription());
    }

    @Test
    void handleStatus_shouldReturnSameException() {
        StatusRuntimeException exception = Status.PERMISSION_DENIED
                .withDescription("Wrong ROLE: CUSTOMER")
                .asRuntimeException();

        StatusRuntimeException result = advice.handleStatus(exception);

        assertSame(exception, result);
        assertEquals(Status.PERMISSION_DENIED.getCode(), result.getStatus().getCode());
        assertEquals("Wrong ROLE: CUSTOMER", result.getStatus().getDescription());
    }
}