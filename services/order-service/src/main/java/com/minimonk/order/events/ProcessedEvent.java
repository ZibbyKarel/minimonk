package com.minimonk.order.events;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private UUID eventId;
    private String eventType;
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(UUID eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.processedAt = Instant.now();
    }
}
