package com.minimonk.order;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerOrderTest {
    @Test
    void startsCreatedAndTotalsItems() {
        CustomerOrder order = new CustomerOrder(UUID.randomUUID(), "4242424242424242");

        order.addItem(UUID.randomUUID(), "SKU-1", "Item 1", 2, new BigDecimal("12.50"));
        order.addItem(UUID.randomUUID(), "SKU-2", "Item 2", 1, new BigDecimal("5.00"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("30.00");
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    void transitionUpdatesStatusAndTimestamp() {
        CustomerOrder order = new CustomerOrder(UUID.randomUUID(), "4242424242424242");
        var originalUpdatedAt = order.getUpdatedAt();

        order.transition(OrderStatus.READY_FOR_PICKING);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.READY_FOR_PICKING);
        assertThat(order.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}
