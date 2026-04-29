package com.minimonk.warehouse;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {
    @Test
    void reserveMovesAvailableToReservedWhenStockExists() {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "availableQuantity", 5);
        ReflectionTestUtils.setField(product, "reservedQuantity", 0);

        boolean reserved = product.reserve(3);

        assertThat(reserved).isTrue();
        assertThat(product.getAvailableQuantity()).isEqualTo(2);
        assertThat(product.getReservedQuantity()).isEqualTo(3);
    }

    @Test
    void reserveRejectsWhenStockIsInsufficient() {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "availableQuantity", 2);
        ReflectionTestUtils.setField(product, "reservedQuantity", 0);

        boolean reserved = product.reserve(3);

        assertThat(reserved).isFalse();
        assertThat(product.getAvailableQuantity()).isEqualTo(2);
        assertThat(product.getReservedQuantity()).isEqualTo(0);
    }

    @Test
    void releaseIsSafeWhenCalledMoreThanReserved() {
        Product product = new Product();
        ReflectionTestUtils.setField(product, "availableQuantity", 2);
        ReflectionTestUtils.setField(product, "reservedQuantity", 3);

        product.release(5);

        assertThat(product.getAvailableQuantity()).isEqualTo(7);
        assertThat(product.getReservedQuantity()).isZero();
    }
}
