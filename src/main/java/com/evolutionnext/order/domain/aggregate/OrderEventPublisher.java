package com.evolutionnext.order.domain.aggregate;


public interface OrderEventPublisher {
    void publish(OrderEvent orderEvent);
}
