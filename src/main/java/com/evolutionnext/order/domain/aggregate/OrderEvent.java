package com.evolutionnext.order.domain.aggregate;


public sealed interface OrderEvent permits OrderCancelled, OrderCreated, OrderPlaced {

}
