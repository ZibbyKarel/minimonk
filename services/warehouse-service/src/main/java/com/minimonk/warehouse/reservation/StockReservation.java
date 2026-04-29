package com.minimonk.warehouse.reservation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "stock_reservations")
public class StockReservation {
    @Id
    @GeneratedValue
    private UUID id;
    private UUID orderId;
    private UUID productId;
    private int quantity;
    private boolean released;

    protected StockReservation() {
    }

    public StockReservation(UUID orderId, UUID productId, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isReleased() {
        return released;
    }

    public void markReleased() {
        this.released = true;
    }
}
