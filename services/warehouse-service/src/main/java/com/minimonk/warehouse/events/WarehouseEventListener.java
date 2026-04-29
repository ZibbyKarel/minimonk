package com.minimonk.warehouse.events;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.OrderCreatedPayload;
import com.minimonk.events.PaymentResultPayload;
import com.minimonk.events.StockReservationFailedPayload;
import com.minimonk.events.StockReservedPayload;
import com.minimonk.warehouse.Product;
import com.minimonk.warehouse.ProductRepository;
import com.minimonk.warehouse.config.RabbitConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.UUID;

@Component
public class WarehouseEventListener {
    private final ProductRepository products;
    private final RabbitTemplate rabbitTemplate;

    public WarehouseEventListener(ProductRepository products, RabbitTemplate rabbitTemplate) {
        this.products = products;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = RabbitConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(EventEnvelope<OrderCreatedPayload> envelope) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                reserve(envelope);
                return;
            } catch (OptimisticLockingFailureException ignored) {
            }
        }
        publish("stock.reservation.failed", EventEnvelope.create("StockReservationFailed", envelope.traceId(),
                new StockReservationFailedPayload(envelope.payload().orderId(), "Concurrent stock update conflict")));
    }

    @Transactional
    void reserve(EventEnvelope<OrderCreatedPayload> envelope) {
        var payload = envelope.payload();
        var loaded = new HashMap<UUID, Product>();
        for (var item : payload.items()) {
            var product = products.findById(item.productId()).orElseThrow();
            loaded.put(item.productId(), product);
            if (product.getAvailableQuantity() < item.quantity()) {
                publish("stock.reservation.failed", EventEnvelope.create("StockReservationFailed", envelope.traceId(),
                        new StockReservationFailedPayload(payload.orderId(), "Insufficient stock for " + item.sku())));
                return;
            }
        }
        payload.items().forEach(item -> loaded.get(item.productId()).reserve(item.quantity()));
        publish("stock.reserved", EventEnvelope.create("StockReserved", envelope.traceId(),
                new StockReservedPayload(payload.orderId(), payload.customerId(), payload.paymentCardNumber())));
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_FAILED_QUEUE)
    @Transactional
    public void onPaymentFailed(EventEnvelope<PaymentResultPayload> envelope) {
        publish("stock.released", EventEnvelope.create("StockReleased", envelope.traceId(), envelope.payload()));
    }

    private void publish(String routingKey, EventEnvelope<?> event) {
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
    }
}
