package com.minimonk.order.api;

import com.minimonk.events.EventEnvelope;
import com.minimonk.events.OrderCreatedPayload;
import com.minimonk.events.OrderItemPayload;
import com.minimonk.events.RabbitEventPublisher;
import com.minimonk.observability.TraceContext;
import com.minimonk.order.CustomerOrder;
import com.minimonk.order.CustomerOrderRepository;
import com.minimonk.order.config.RabbitConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final CustomerOrderRepository orders;
    private final RabbitEventPublisher events;

    public OrderController(CustomerOrderRepository orders, RabbitEventPublisher events) {
        this.orders = orders;
        this.events = events;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public CreateOrderResponse create(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestHeader(value = "X-Minimonk-User-Id", required = false) UUID authenticatedCustomerId,
            HttpServletRequest httpRequest
    ) {
        var customerId = authenticatedCustomerId != null ? authenticatedCustomerId : request.customerId();
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required when no authenticated user header is present");
        }
        var order = new CustomerOrder(customerId, request.paymentCardNumber());
        request.items().forEach(item -> order.addItem(item.productId(), item.sku(), item.name(), item.quantity(), item.unitPrice()));
        orders.save(order);
        var eventItems = order.getItems().stream()
                .map(item -> new OrderItemPayload(item.getProductId(), item.getSku(), item.getName(), item.getQuantity(), item.getUnitPrice()))
                .toList();
        var traceparent = TraceContext.ensureTraceparent(httpRequest.getHeader(TraceContext.TRACEPARENT));
        var event = EventEnvelope.create("OrderCreated", TraceContext.traceId(traceparent),
                new OrderCreatedPayload(order.getId(), order.getCustomerId(), order.getPaymentCardNumber(), order.getTotalAmount(), eventItems));
        events.publish("order.created", event);
        return new CreateOrderResponse(order.getId(), order.getStatus());
    }

    @GetMapping
    public List<OrderOverviewDto> list() {
        return orders.findOverview();
    }
}
