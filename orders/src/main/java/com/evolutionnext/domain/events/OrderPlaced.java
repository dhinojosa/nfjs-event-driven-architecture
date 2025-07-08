package com.evolutionnext.domain.events;


import com.evolutionnext.domain.aggregate.Order;

public record OrderPlaced(Order order) implements OrderEvent {
}
