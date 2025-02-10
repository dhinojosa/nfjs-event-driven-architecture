package com.evolutionnext.order.domain.aggregate;


public record OrderCreated(Order order) implements OrderEvent{
}
