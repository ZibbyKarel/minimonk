package com.minimonk.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    private UUID id;
    private UUID customerId;
    private String paymentCardNumber;
    private BigDecimal totalAmount;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerOrderItem> items = new ArrayList<>();

    protected CustomerOrder() {
    }

    public CustomerOrder(UUID customerId, String paymentCardNumber) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.paymentCardNumber = paymentCardNumber;
        this.totalAmount = BigDecimal.ZERO;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = createdAt;
    }

    public void addItem(UUID productId, String sku, String name, int quantity, BigDecimal unitPrice) {
        items.add(new CustomerOrderItem(this, productId, sku, name, quantity, unitPrice));
        totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    public void transition(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getPaymentCardNumber() {
        return paymentCardNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<CustomerOrderItem> getItems() {
        return items;
    }
}
