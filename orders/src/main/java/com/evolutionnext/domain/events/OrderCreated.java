package com.evolutionnext.domain.events;


import com.evolutionnext.domain.aggregate.Order;

public record OrderCreated(Order order) implements OrderEvent {
}
