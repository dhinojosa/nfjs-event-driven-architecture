package com.evolutionnext.port.in.order;


import com.evolutionnext.domain.OrderSubmitted;

public interface ForSubscriberCommandPort {
    void handleOrderEvent(OrderSubmitted orderSubmitted);
}
