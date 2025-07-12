package com.evolutionnext.domain.events;


public sealed interface OrderEvent permits OrderCreated, OrderDeleted, OrderItemAdded, OrderItemChanged, OrderItemDeleted, OrderPlaced {

}
