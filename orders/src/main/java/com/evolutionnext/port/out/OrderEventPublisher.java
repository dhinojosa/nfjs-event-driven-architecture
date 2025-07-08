package com.evolutionnext.port.out;


import com.evolutionnext.domain.events.OrderEvent;

public interface OrderEventPublisher {
    void publish(OrderEvent orderEvent);
}
