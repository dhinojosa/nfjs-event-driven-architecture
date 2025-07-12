package com.evolutionnext.domain.events;


import java.time.Instant;
import java.util.UUID;

public record OrderDeleted(UUID uuid, Instant now) implements OrderEvent {
}
