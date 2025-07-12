package com.evolutionnext.application.commands;


import java.util.UUID;

public record SubmitOrder(UUID uuid, java.time.Instant now) implements OrderCommand {
}
