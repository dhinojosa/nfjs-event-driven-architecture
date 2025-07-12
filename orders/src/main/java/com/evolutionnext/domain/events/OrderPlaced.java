package com.evolutionnext.domain.events;


import java.time.Instant;
import java.util.UUID;

public record OrderPlaced(UUID uuid, Instant now) implements OrderEvent {
}
