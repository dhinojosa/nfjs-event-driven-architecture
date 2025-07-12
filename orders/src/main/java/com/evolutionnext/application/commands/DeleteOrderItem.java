package com.evolutionnext.application.commands;


import java.util.UUID;

public record DeleteOrderItem(UUID orderId, UUID orderItemId, java.time.Instant now) implements OrderCommand{
}
