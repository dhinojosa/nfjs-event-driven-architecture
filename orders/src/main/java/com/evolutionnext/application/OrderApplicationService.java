package com.evolutionnext.application;

import com.evolutionnext.application.commands.*;
import com.evolutionnext.domain.events.*;
import com.evolutionnext.port.in.ForClientSubmitOrder;
import com.evolutionnext.port.out.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderApplicationService implements ForClientSubmitOrder {

    private static final Logger logger = LoggerFactory.getLogger(OrderApplicationService.class);
    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public void submit(OrderCommand orderCommand) {
        logger.info("Submitting OrderCommand {}", orderCommand);
        switch (orderCommand) {
            case AddOrderItem(
                UUID uuid, UUID orderItemId, long productId,
                int quantity, BigDecimal price, java.time.Instant now
            ) -> orderEventPublisher.publish(new OrderItemAdded(uuid, orderItemId, productId, quantity, price, now));
            case CreateOrder(UUID uuid, java.time.Instant now) ->
                orderEventPublisher.publish(new OrderCreated(uuid, now));
            case DeleteOrder(UUID uuid, java.time.Instant now) -> orderEventPublisher.publish(new OrderDeleted(uuid, now));
            case DeleteOrderItem(UUID orderId, UUID orderItemId, java.time.Instant now) ->
                orderEventPublisher.publish(new OrderItemDeleted(orderId, orderItemId, now));
            case SubmitOrder(UUID uuid, java.time.Instant now) ->
                orderEventPublisher.publish(new OrderPlaced(uuid, now));
            case ChangeOrderItem(
                UUID orderId, UUID orderItemId, Long productId, int quantity, java.math.BigDecimal price,
                java.time.Instant now
            ) ->
                orderEventPublisher.publish(new OrderItemChanged(orderId, orderItemId, productId, quantity, price, now));
        }
    }
}
