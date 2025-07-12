package com.evolutionnext.domain.events;


import java.time.Instant;
import java.util.UUID;

public record OrderItemDeleted(UUID orderId, UUID orderItemId, Instant now) implements OrderEvent {
}
