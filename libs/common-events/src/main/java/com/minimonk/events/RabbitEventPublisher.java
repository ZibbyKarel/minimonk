package com.minimonk.events;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.MessagePostProcessor;

import java.security.SecureRandom;
import java.util.HexFormat;

public class RabbitEventPublisher {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final RabbitTemplate rabbitTemplate;
    private final String exchange;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate, String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void publish(String routingKey, EventEnvelope<?> event) {
        rabbitTemplate.convertAndSend(exchange, routingKey, event, headers(event));
    }

    private MessagePostProcessor headers(EventEnvelope<?> event) {
        return message -> {
            var headers = message.getMessageProperties().getHeaders();
            headers.put("traceparent", traceparent(event.traceId()));
            headers.put("event_id", event.eventId().toString());
            headers.put("event_type", event.eventType());
            return message;
        };
    }

    private String traceparent(String traceId) {
        String normalized = traceId != null && traceId.matches("^[\\da-f]{32}$") ? traceId : randomHex(16);
        return "00-" + normalized + "-" + randomHex(8) + "-01";
    }

    private String randomHex(int bytes) {
        byte[] value = new byte[bytes];
        RANDOM.nextBytes(value);
        return HexFormat.of().formatHex(value);
    }
}
