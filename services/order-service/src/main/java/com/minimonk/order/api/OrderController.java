package com.minimonk.order.api;

import com.minimonk.observability.TraceContext;
import com.minimonk.order.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse create(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        var customerId = authenticatedCustomerId(authentication);
        if (hasRole(authentication, "ADMIN") && request.customerId() != null) {
            customerId = request.customerId();
        }
        if (customerId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated customer is required");
        }
        var traceparent = TraceContext.ensureTraceparent(httpRequest.getHeader(TraceContext.TRACEPARENT));
        return orderService.createOrder(customerId, request, traceparent);
    }

    @GetMapping
    public List<OrderOverviewDto> list(Authentication authentication) {
        if (hasRole(authentication, "ADMIN")) {
            return orderService.listAllOrders();
        }
        return orderService.listOrders(authenticatedCustomerId(authentication));
    }

    private UUID authenticatedCustomerId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return UUID.fromString(authentication.getName());
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role));
    }
}
