package com.minimonk.events;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemPayload(UUID productId, String sku, String name, int quantity, BigDecimal unitPrice) {
}
