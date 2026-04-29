package com.minimonk.order.events;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.PaymentResultPayload;
import com.minimonk.order.CustomerOrder;
import com.minimonk.order.CustomerOrderRepository;
import com.minimonk.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderEventListenerTest {
    private final CustomerOrderRepository orders = mock(CustomerOrderRepository.class);
    private final ProcessedEventRepository processedEvents = mock(ProcessedEventRepository.class);
    private final OrderEventListener listener = new OrderEventListener(orders, processedEvents);

    @Test
    void ignoresAlreadyProcessedEvents() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var envelope = new EventEnvelope<>(eventId, "PaymentSucceeded", "trace", java.time.Instant.now(),
                new PaymentResultPayload(orderId, "Approved"));
        when(processedEvents.existsById(eventId)).thenReturn(true);

        listener.onPaymentSucceeded(envelope);

        verify(orders, never()).findById(any());
        verify(processedEvents, never()).save(any());
    }

    @Test
    void transitionsAndRecordsNewEvents() {
        var eventId = UUID.randomUUID();
        var orderId = UUID.randomUUID();
        var order = new CustomerOrder(UUID.randomUUID(), "4242424242424242");
        var envelope = new EventEnvelope<>(eventId, "PaymentSucceeded", "trace", java.time.Instant.now(),
                new PaymentResultPayload(orderId, "Approved"));
        when(processedEvents.existsById(eventId)).thenReturn(false);
        when(orders.findById(orderId)).thenReturn(Optional.of(order));

        listener.onPaymentSucceeded(envelope);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY_FOR_PICKING);
        verify(processedEvents).save(any(ProcessedEvent.class));
    }
}
