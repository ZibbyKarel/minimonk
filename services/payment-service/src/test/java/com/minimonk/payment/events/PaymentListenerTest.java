package com.minimonk.payment.events;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.RabbitEventPublisher;
import com.minimonk.events.StockReservedPayload;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PaymentListenerTest {
    @Test
    void publishesFailureForConfiguredCardOnlyOncePerEvent() {
        RabbitEventPublisher publisher = mock(RabbitEventPublisher.class);
        PaymentListener listener = new PaymentListener(publisher, "4000000000000002");
        var envelope = new EventEnvelope<>(UUID.randomUUID(), "StockReserved", "trace", Instant.now(),
                new StockReservedPayload(UUID.randomUUID(), UUID.randomUUID(), "4000000000000002"));

        listener.onStockReserved(envelope);
        listener.onStockReserved(envelope);

        verify(publisher, times(1)).publish(eq("payment.failed"), any());
    }

    @Test
    void publishesSuccessForRegularCard() {
        RabbitEventPublisher publisher = mock(RabbitEventPublisher.class);
        PaymentListener listener = new PaymentListener(publisher, "4000000000000002");
        var envelope = new EventEnvelope<>(UUID.randomUUID(), "StockReserved", "trace", Instant.now(),
                new StockReservedPayload(UUID.randomUUID(), UUID.randomUUID(), "4242424242424242"));

        listener.onStockReserved(envelope);

        verify(publisher).publish(eq("payment.succeeded"), any());
    }
}
