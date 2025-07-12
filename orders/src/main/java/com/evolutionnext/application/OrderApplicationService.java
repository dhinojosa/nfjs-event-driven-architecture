package com.evolutionnext.application;

import com.evolutionnext.application.commands.OrderCommand;
import com.evolutionnext.port.in.ForClientSubmitOrder;
import com.evolutionnext.port.out.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderApplicationService implements ForClientSubmitOrder {

    private static final Logger logger = LoggerFactory.getLogger(OrderApplicationService.class);
    private final OrderEventPublisher orderEventPublisher;

    public OrderApplicationService(OrderEventPublisher orderEventPublisher) {
        this.orderEventPublisher = orderEventPublisher;
    }

    @Override
    public void submit(OrderCommand orderCommand) {
        logger.info("Submitting OrderCommand {}", orderCommand);
    }
}
