package com.minimonk.order;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.OrderCreatedPayload;
import com.minimonk.events.OrderItemPayload;
import com.minimonk.events.RabbitEventPublisher;
import com.minimonk.observability.TraceContext;
import com.minimonk.order.api.CreateOrderRequest;
import com.minimonk.order.api.CreateOrderResponse;
import com.minimonk.order.api.OrderOverviewDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private final CustomerOrderRepository orders;
    private final RabbitEventPublisher events;

    public OrderService(CustomerOrderRepository orders, RabbitEventPublisher events) {
        this.orders = orders;
        this.events = events;
    }

    @Transactional
    public CreateOrderResponse createOrder(UUID customerId, CreateOrderRequest request, String traceparent) {
        var order = new CustomerOrder(customerId, request.paymentCardNumber());
        request.items().forEach(item -> order.addItem(item.productId(), item.sku(), item.name(), item.quantity(), item.unitPrice()));
        orders.save(order);
        var eventItems = order.getItems().stream()
                .map(item -> new OrderItemPayload(item.getProductId(), item.getSku(), item.getName(), item.getQuantity(), item.getUnitPrice()))
                .toList();
        var event = EventEnvelope.create("OrderCreated", TraceContext.traceId(traceparent),
                new OrderCreatedPayload(order.getId(), order.getCustomerId(), order.getPaymentCardNumber(), order.getTotalAmount(), eventItems));
        events.publish("order.created", event);
        return new CreateOrderResponse(order.getId(), order.getStatus());
    }

    public List<OrderOverviewDto> listOrders(UUID customerId) {
        return orders.findOverviewByCustomerId(customerId);
    }

    public List<OrderOverviewDto> listAllOrders() {
        return orders.findOverview();
    }
}
