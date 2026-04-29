package com.minimonk.order;

public enum OrderStatus {
    CREATED,
    STOCK_RESERVED,
    PAYMENT_PENDING,
    PAID,
    READY_FOR_PICKING,
    COMPLETED,
    STOCK_RESERVATION_FAILED,
    PAYMENT_FAILED,
    STOCK_RELEASED,
    CANCELLED
}
