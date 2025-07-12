package com.evolutionnext.application.commands;


import java.util.UUID;

public record DeleteOrder(UUID uuid) implements OrderCommand {

}
