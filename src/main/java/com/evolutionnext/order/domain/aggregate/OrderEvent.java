package com.evolutionnext.order.domain.aggregate;


public sealed interface OrderEvent permits OrderPlaced, OrderCancelled, OrderCreated {

}
