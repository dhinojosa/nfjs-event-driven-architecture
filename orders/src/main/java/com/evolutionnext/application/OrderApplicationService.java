package com.evolutionnext.application;

import com.evolutionnext.domain.aggregate.OrderId;
import com.evolutionnext.port.in.ForClientSubmitOrder;
import com.evolutionnext.port.out.OrderEventPublisher;
import com.evolutionnext.domain.aggregate.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class OrderApplicationService implements ForClientSubmitOrder {


    private static final Logger logger = LoggerFactory.getLogger(OrderApplicationService.class);
    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public void submit(Order order) {
        logger.info("Submitting order with ID: {}", order.getOrderId().id());
        try {
            order.events().forEach(event -> {
                logger.debug("Publishing event: {}", event);
                orderEventPublisher.publish(event);
            });
            order.clearEvents();
            logger.info("Order submission completed for ID: {}", order.getOrderId().id());
        } catch (Exception e) {
            logger.error("Failed to submit order with ID: {}", order.getOrderId().id(), e);
            throw new RuntimeException("Order submission failed.", e);
        }
    }
}
