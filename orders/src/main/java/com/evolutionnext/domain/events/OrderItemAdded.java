package com.evolutionnext.domain.events;


public record OrderItemAdded(java.util.UUID uuid, java.util.UUID orderItemId, long productId, int quantity,
                             java.math.BigDecimal price, java.time.Instant now) implements OrderEvent {
}
