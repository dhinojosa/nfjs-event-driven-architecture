package com.evolutionnext.order.domain.aggregate;


public record OrderCancelled(Order order, String reason) implements OrderEvent {
}
