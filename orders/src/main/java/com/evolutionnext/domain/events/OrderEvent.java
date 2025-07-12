package com.evolutionnext.domain.events;


public sealed interface OrderEvent permits OrderCancelled, OrderCreated, OrderDeleted, OrderItemAdded, OrderItemChanged, OrderItemDeleted, OrderPlaced {

}
