package com.evolutionnext.application;

import com.evolutionnext.port.in.ForClientSubmitOrder;
import com.evolutionnext.port.out.OrderEventPublisher;
import com.evolutionnext.domain.aggregate.Order;

public class OrderApplicationService implements ForClientSubmitOrder {

    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    public void submit(Order order) {
        order.events().forEach(orderEventPublisher::publish);
        order.clearEvents();
    }
}
