package com.evolutionnext.order.domain.aggregate;


public interface OrderRepository {
    Order save(Order order);
    Order findById(String id);
    void delete(Order order);
}
