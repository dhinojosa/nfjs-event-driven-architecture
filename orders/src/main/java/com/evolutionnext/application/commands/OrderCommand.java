package com.evolutionnext.application.commands;


public sealed interface OrderCommand permits AddOrderItem, ChangeOrderItem, CreateOrder, DeleteOrder, DeleteOrderItem {
}
