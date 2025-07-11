package com.evolutionnext;


import com.evolutionnext.infrastructure.in.order.OrdersServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Runner {
    private static Logger logger = LoggerFactory.getLogger(Runner.class);
    public static void main(String[] args) throws IOException {

        OrdersServer ordersServer = new OrdersServer(order -> {
          logger.info("Received order: {} in the port", order);
        });
        ordersServer.start(new InetSocketAddress(8080));
    }
}
