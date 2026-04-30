package com.minimonk.warehouse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProductRepositoryTest {
    private static final UUID PRODUCT_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbb1");

    @Autowired
    private ProductRepository products;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("delete from stock_reservations");
        jdbcTemplate.update("delete from processed_events");
        jdbcTemplate.update("delete from products where id = ?", PRODUCT_ID);
        jdbcTemplate.update("""
                insert into products (id, sku, name, description, price, available_quantity, reserved_quantity, version)
                values (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                PRODUCT_ID, "SKU-1", "Test product", "Description", 10.00, 5, 0, 0L);
    }

    @Test
    void reserveAtomicallyMovesStockWhenEnoughInventoryExists() {
        int updatedRows = products.reserveAtomically(PRODUCT_ID, 3);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(availableQuantity()).isEqualTo(2);
        assertThat(reservedQuantity()).isEqualTo(3);
        assertThat(version()).isEqualTo(1L);
    }

    @Test
    void reserveAtomicallyDoesNothingWhenInventoryIsInsufficient() {
        int updatedRows = products.reserveAtomically(PRODUCT_ID, 6);

        assertThat(updatedRows).isZero();
        assertThat(availableQuantity()).isEqualTo(5);
        assertThat(reservedQuantity()).isZero();
        assertThat(version()).isZero();
    }

    @Test
    void releaseAtomicallyReturnsOnlyActuallyReservedStock() {
        products.reserveAtomically(PRODUCT_ID, 3);

        int updatedRows = products.releaseAtomically(PRODUCT_ID, 5);

        assertThat(updatedRows).isEqualTo(1);
        assertThat(availableQuantity()).isEqualTo(5);
        assertThat(reservedQuantity()).isZero();
        assertThat(version()).isEqualTo(2L);
    }

    private int availableQuantity() {
        return jdbcTemplate.queryForObject("select available_quantity from products where id = ?", Integer.class, PRODUCT_ID);
    }

    private int reservedQuantity() {
        return jdbcTemplate.queryForObject("select reserved_quantity from products where id = ?", Integer.class, PRODUCT_ID);
    }

    private long version() {
        return jdbcTemplate.queryForObject("select version from products where id = ?", Long.class, PRODUCT_ID);
    }
}
