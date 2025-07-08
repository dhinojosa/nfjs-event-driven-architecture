package com.evolutionnext.domain.events;


public sealed interface OrderEvent permits OrderCancelled, OrderCreated, OrderPlaced {

}
