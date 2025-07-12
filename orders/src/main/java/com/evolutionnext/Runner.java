package com.evolutionnext;


import com.evolutionnext.application.OrderApplicationService;
import com.evolutionnext.domain.events.OrderEvent;
import com.evolutionnext.infrastructure.in.order.OrdersServer;
import com.evolutionnext.port.out.OrderEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Runner {
    private static Logger logger = LoggerFactory.getLogger(Runner.class);
    public static void main(String[] args) throws IOException {

        OrdersServer ordersServer = new OrdersServer(new OrderApplicationService(orderEvent -> {
            logger.info("Publishing OrderEvent {}", orderEvent);
        }));
        ordersServer.start(new InetSocketAddress(8080));
    }
}
