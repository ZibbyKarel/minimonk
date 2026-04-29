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

    public OrderEventListener(CustomerOrderRepository orders) {
        this.orders = orders;
    }

    @RabbitListener(queues = RabbitConfig.STOCK_RESERVED_QUEUE)
    @Transactional
    public void onStockReserved(EventEnvelope<StockReservedPayload> envelope) {
        transition(envelope.payload().orderId(), OrderStatus.PAYMENT_PENDING);
    }

    @RabbitListener(queues = RabbitConfig.STOCK_FAILED_QUEUE)
    @Transactional
    public void onStockFailed(EventEnvelope<StockReservationFailedPayload> envelope) {
        transition(envelope.payload().orderId(), OrderStatus.CANCELLED);
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_SUCCEEDED_QUEUE)
    @Transactional
    public void onPaymentSucceeded(EventEnvelope<PaymentResultPayload> envelope) {
        transition(envelope.payload().orderId(), OrderStatus.READY_FOR_PICKING);
    }

    @RabbitListener(queues = RabbitConfig.PAYMENT_FAILED_QUEUE)
    @Transactional
    public void onPaymentFailed(EventEnvelope<PaymentResultPayload> envelope) {
        transition(envelope.payload().orderId(), OrderStatus.PAYMENT_FAILED);
    }

    @RabbitListener(queues = RabbitConfig.STOCK_RELEASED_QUEUE)
    @Transactional
    public void onStockReleased(EventEnvelope<PaymentResultPayload> envelope) {
        transition(envelope.payload().orderId(), OrderStatus.CANCELLED);
    }

    private void transition(UUID orderId, OrderStatus status) {
        orders.findById(orderId).ifPresent(order -> order.transition(status));
    }
}
