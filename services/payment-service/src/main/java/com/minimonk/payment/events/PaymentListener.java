package com.minimonk.payment.events;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.PaymentResultPayload;
import com.minimonk.events.RabbitEventPublisher;
import com.minimonk.events.StockReservedPayload;
import com.minimonk.payment.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentListener {
    private final RabbitEventPublisher events;
    private final String failingCard;
    private final Set<UUID> processedEvents = ConcurrentHashMap.newKeySet();

    public PaymentListener(RabbitEventPublisher events, @Value("${minimonk.payment.failing-card}") String failingCard) {
        this.events = events;
        this.failingCard = failingCard;
    }

    @RabbitListener(queues = RabbitConfig.STOCK_RESERVED_QUEUE)
    public void onStockReserved(EventEnvelope<StockReservedPayload> envelope) {
        if (!processedEvents.add(envelope.eventId())) {
            return;
        }
        var payload = envelope.payload();
        if (failingCard.equals(payload.paymentCardNumber())) {
            publish("payment.failed", EventEnvelope.create("PaymentFailed", envelope.traceId(),
                    new PaymentResultPayload(payload.orderId(), "Deterministic demo card failure")));
            return;
        }
        publish("payment.succeeded", EventEnvelope.create("PaymentSucceeded", envelope.traceId(),
                new PaymentResultPayload(payload.orderId(), "Approved")));
    }

    private void publish(String routingKey, EventEnvelope<?> event) {
        events.publish(routingKey, event);
    }
}
