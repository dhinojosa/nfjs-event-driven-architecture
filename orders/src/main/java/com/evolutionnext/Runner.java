package com.evolutionnext;


import com.evolutionnext.application.OrderApplicationService;
import com.evolutionnext.infrastructure.in.order.OrdersServer;
import com.evolutionnext.infrastructure.out.kafka.OrderEventKafkaPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Runner {
    private static Logger logger = LoggerFactory.getLogger(Runner.class);
    public static void main(String[] args) throws IOException {
        OrderEventKafkaPublisher orderEventKafkaPublisher = new OrderEventKafkaPublisher();
        OrderApplicationService orderApplicationService = new OrderApplicationService(orderEventKafkaPublisher);
        OrdersServer ordersServer = new OrdersServer(orderApplicationService);
        ordersServer.start(new InetSocketAddress(8080));
    }
}
