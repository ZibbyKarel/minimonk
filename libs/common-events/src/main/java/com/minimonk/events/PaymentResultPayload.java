package com.minimonk.events;

import java.util.UUID;

public record PaymentResultPayload(UUID orderId, String reason) {
}
