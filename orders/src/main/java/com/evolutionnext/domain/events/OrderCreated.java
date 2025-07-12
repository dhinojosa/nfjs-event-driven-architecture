package com.evolutionnext.domain.events;

public record OrderCreated(java.util.UUID uuid, java.time.Instant now) implements OrderEvent {
}
