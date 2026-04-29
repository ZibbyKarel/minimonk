package com.minimonk.payment.events;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.PaymentResultPayload;
import com.minimonk.events.RabbitEventPublisher;
import com.minimonk.events.StockReservedPayload;
import com.minimonk.payment.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentListener {
    private final RabbitEventPublisher events;
    private final String failingCard;

    public PaymentListener(RabbitEventPublisher events, @Value("${minimonk.payment.failing-card}") String failingCard) {
        this.events = events;
        this.failingCard = failingCard;
    }

    @RabbitListener(queues = RabbitConfig.STOCK_RESERVED_QUEUE)
    public void onStockReserved(EventEnvelope<StockReservedPayload> envelope) {
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
