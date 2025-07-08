package com.evolutionnext.domain.events;


import com.evolutionnext.domain.aggregate.Order;

public record OrderCancelled(Order order, String reason) implements OrderEvent {
}
