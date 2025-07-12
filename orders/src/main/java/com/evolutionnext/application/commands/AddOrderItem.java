package com.evolutionnext.application.commands;


import java.math.BigDecimal;
import java.util.UUID;

public record AddOrderItem(UUID uuid, UUID orderItemId, long productId, int quantity, BigDecimal price,
                           java.time.Instant now) implements OrderCommand {

}
