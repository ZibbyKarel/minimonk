package com.minimonk.events;

import java.util.UUID;

public record StockReservationFailedPayload(UUID orderId, String reason) {
}
