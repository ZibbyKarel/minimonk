package com.minimonk.warehouse.events;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.OrderCreatedPayload;
import com.minimonk.events.PaymentResultPayload;
import com.minimonk.events.RabbitEventPublisher;
import com.minimonk.events.StockReservationFailedPayload;
import com.minimonk.events.StockReservedPayload;
import com.minimonk.warehouse.Product;
import com.minimonk.warehouse.ProductRepository;
import com.minimonk.warehouse.config.RabbitConfig;
import com.minimonk.warehouse.reservation.StockReservation;
import com.minimonk.warehouse.reservation.StockReservationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.HashMap;
import java.util.UUID;

@Component
public class WarehouseEventListener {
    private final ProductRepository products;
    private final StockReservationRepository reservations;
    private final ProcessedEventRepository processedEvents;
    private final TransactionTemplate transactionTemplate;
    private final RabbitEventPublisher events;

    public WarehouseEventListener(ProductRepository products, StockReservationRepository reservations, ProcessedEventRepository processedEvents, TransactionTemplate transactionTemplate, RabbitEventPublisher events) {
        this.products = products;
        this.reservations = reservations;
        this.processedEvents = processedEvents;
        this.transactionTemplate = transactionTemplate;
        this.events = events;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(EventEnvelope<OrderCreatedPayload> envelope) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                transactionTemplate.executeWithoutResult(status -> reserve(envelope));
                return;
            } catch (OptimisticLockingFailureException ignored) {
            }
        }
        transactionTemplate.executeWithoutResult(status -> markProcessed(envelope));
        publish("stock.reservation.failed", EventEnvelope.create("StockReservationFailed", envelope.traceId(),
                new StockReservationFailedPayload(envelope.payload().orderId(), "Concurrent stock update conflict")));
    }

    void reserve(EventEnvelope<OrderCreatedPayload> envelope) {
        if (processedEvents.existsById(envelope.eventId())) {
            return;
        }
        var payload = envelope.payload();
        var loaded = new HashMap<UUID, Product>();
        for (var item : payload.items()) {
            var product = products.findById(item.productId()).orElseThrow();
            loaded.put(item.productId(), product);
            if (product.getAvailableQuantity() < item.quantity()) {
                markProcessed(envelope);
                publish("stock.reservation.failed", EventEnvelope.create("StockReservationFailed", envelope.traceId(),
                        new StockReservationFailedPayload(payload.orderId(), "Insufficient stock for " + item.sku())));
                return;
            }
        }
        payload.items().forEach(item -> {
            loaded.get(item.productId()).reserve(item.quantity());
            reservations.save(new StockReservation(payload.orderId(), item.productId(), item.quantity()));
        });
        markProcessed(envelope);
        publish("stock.reserved", EventEnvelope.create("StockReserved", envelope.traceId(),
                new StockReservedPayload(payload.orderId(), payload.customerId(), payload.paymentCardNumber())));
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_FAILED_QUEUE)
    public void onPaymentFailed(EventEnvelope<PaymentResultPayload> envelope) {
        boolean released = Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            if (processedEvents.existsById(envelope.eventId())) {
                return false;
            }
            var stockReservations = reservations.findByOrderId(envelope.payload().orderId());
            for (var reservation : stockReservations) {
                if (!reservation.isReleased()) {
                    products.findById(reservation.getProductId()).ifPresent(product -> product.release(reservation.getQuantity()));
                    reservation.markReleased();
                }
            }
            markProcessed(envelope);
            return true;
        }));
        if (released) {
            publish("stock.released", EventEnvelope.create("StockReleased", envelope.traceId(), envelope.payload()));
        }
    }

    private void publish(String routingKey, EventEnvelope<?> event) {
        events.publish(routingKey, event);
    }

    private void markProcessed(EventEnvelope<?> envelope) {
        processedEvents.save(new ProcessedEvent(envelope.eventId(), envelope.eventType()));
    }
}
