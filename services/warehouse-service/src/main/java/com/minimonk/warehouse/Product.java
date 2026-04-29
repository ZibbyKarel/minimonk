package com.minimonk.warehouse;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private UUID id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private int availableQuantity;
    private int reservedQuantity;
    @Version
    private long version;

    protected Product() {
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public boolean reserve(int quantity) {
        if (availableQuantity < quantity) {
            return false;
        }
        availableQuantity -= quantity;
        reservedQuantity += quantity;
        return true;
    }

    public void release(int quantity) {
        reservedQuantity = Math.max(0, reservedQuantity - quantity);
        availableQuantity += quantity;
    }
}
