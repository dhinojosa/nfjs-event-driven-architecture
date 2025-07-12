package com.evolutionnext.application.commands;


import java.time.Instant;
import java.util.UUID;

public record DeleteOrder(UUID uuid, Instant now) implements OrderCommand {

}
