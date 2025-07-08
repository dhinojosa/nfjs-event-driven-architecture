package com.evolutionnext.port.in;


import com.evolutionnext.domain.aggregate.Order;

public interface ForClientSubmitOrder {
    void submit(Order order);
}
