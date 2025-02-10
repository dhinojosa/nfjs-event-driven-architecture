package com.evolutionnext.order.domain.aggregate;


public record OrderPlaced(Order order) implements OrderEvent {
}
