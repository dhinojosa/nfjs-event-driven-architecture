package com.evolutionnext.order.domain.application;


import com.evolutionnext.order.domain.aggregate.Order;
import com.evolutionnext.order.domain.aggregate.OrderEventPublisher;

public class OrderApplicationService {

    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    public void submit(Order order) {
        order.events().forEach(orderEventPublisher::publish);
    }
}
