package com.evolutionnext.domain.events;


import java.util.UUID;

public record OrderDeleted(UUID uuid) implements OrderEvent {
}
