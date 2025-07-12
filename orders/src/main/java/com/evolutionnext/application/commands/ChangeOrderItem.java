package com.evolutionnext.application.commands;


import java.util.UUID;

public record ChangeOrderItem (UUID orderId, UUID orderItemId, Long productId, int quantity, java.math.BigDecimal v,
                               java.time.Instant now) implements OrderCommand{
}
