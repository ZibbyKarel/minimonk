package com.minimonk.events;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        String traceId,
        Instant occurredAt,
        T payload
) {
    public static <T> EventEnvelope<T> create(String eventType, String traceId, T payload) {
        return new EventEnvelope<>(UUID.randomUUID(), eventType, traceId, Instant.now(), payload);
    }
}
