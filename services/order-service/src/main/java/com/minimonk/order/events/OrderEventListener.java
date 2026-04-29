package com.minimonk.order.events;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.PaymentResultPayload;
import com.minimonk.events.StockReservationFailedPayload;
import com.minimonk.events.StockReservedPayload;
import com.minimonk.order.CustomerOrderRepository;
import com.minimonk.order.OrderStatus;
import com.minimonk.order.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class OrderEventListener {
    private final CustomerOrderRepository orders;
    private final ProcessedEventRepository processedEvents;

    public OrderEventListener(CustomerOrderRepository orders, ProcessedEventRepository processedEvents) {
        this.orders = orders;
        this.processedEvents = processedEvents;
    }

    @RabbitListener(queues = RabbitConfig.STOCK_RESERVED_QUEUE)
    @Transactional
    public void onStockReserved(EventEnvelope<StockReservedPayload> envelope) {
        processOnce(envelope, () -> transition(envelope.payload().orderId(), OrderStatus.PAYMENT_PENDING));
    }

    @RabbitListener(queues = RabbitConfig.STOCK_FAILED_QUEUE)
    @Transactional
    public void onStockFailed(EventEnvelope<StockReservationFailedPayload> envelope) {
        processOnce(envelope, () -> transition(envelope.payload().orderId(), OrderStatus.CANCELLED));
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_SUCCEEDED_QUEUE)
    @Transactional
    public void onPaymentSucceeded(EventEnvelope<PaymentResultPayload> envelope) {
        processOnce(envelope, () -> transition(envelope.payload().orderId(), OrderStatus.READY_FOR_PICKING));
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_FAILED_QUEUE)
    @Transactional
    public void onPaymentFailed(EventEnvelope<PaymentResultPayload> envelope) {
        processOnce(envelope, () -> transition(envelope.payload().orderId(), OrderStatus.PAYMENT_FAILED));
    }

    @RabbitListener(queues = RabbitConfig.STOCK_RELEASED_QUEUE)
    @Transactional
    public void onStockReleased(EventEnvelope<PaymentResultPayload> envelope) {
        processOnce(envelope, () -> transition(envelope.payload().orderId(), OrderStatus.CANCELLED));
    }

    private void processOnce(EventEnvelope<?> envelope, Runnable handler) {
        if (processedEvents.existsById(envelope.eventId())) {
            return;
        }
        handler.run();
        processedEvents.save(new ProcessedEvent(envelope.eventId(), envelope.eventType()));
    }

    private void transition(UUID orderId, OrderStatus status) {
        orders.findById(orderId).ifPresent(order -> order.transition(status));
    }
}
