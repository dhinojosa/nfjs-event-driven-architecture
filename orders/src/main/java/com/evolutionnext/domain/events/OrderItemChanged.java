package com.evolutionnext.domain.events;


public record OrderItemChanged(java.util.UUID orderId, java.util.UUID orderItemId, Long productId, int quantity,
                               java.math.BigDecimal price, java.time.Instant now) implements OrderEvent {
}
