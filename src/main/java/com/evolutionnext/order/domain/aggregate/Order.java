package com.evolutionnext.order.domain.aggregate;

import java.util.*;

public class Order {
    private final String state;
    private final OrderId orderId;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final List<OrderEvent> events = new ArrayList<>();

    protected Order(OrderId orderId, CustomerId customerId, String state) {
        this.state = state;
        this.orderId = orderId;
        this.customerId = customerId;
        this.status = OrderStatus.CREATED;
        this.items = new ArrayList<>();
        this.events.add(new OrderCreated(this));
    }

    public void placeOrder() {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order cannot be placed in its current state");
        }
        this.status = OrderStatus.PLACED;
        this.events.add(new OrderPlaced(this));
    }

    public void addOrderItem(OrderItem orderItem) {
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot add items to an order that is not in the NEW state");
        }
        this.items.add(orderItem);
    }

    public void cancelOrder() {
        this.status = OrderStatus.CANCELLED;
        this.events.add(new OrderCancelled(this, "Order cancelled by customer"));
    }

    public static Order create(OrderId orderId, CustomerId customerId, String state){
        return new Order(orderId, customerId, state);
    }

    public String getState() {
        return state;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public int getTotal() {
        return items.stream().mapToInt(OrderItem::price).sum();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
            .add("orderId='" + orderId + "'")
            .add("status=" + status)
            .add("items=" + items)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId) && status == order.status && Objects.equals(items, order.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, status, items);
    }

    public List<OrderEvent> events() {
        return Collections.unmodifiableList(events);
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(items);
    }

    public void clearEvents() {
        events.clear();
    }
}
