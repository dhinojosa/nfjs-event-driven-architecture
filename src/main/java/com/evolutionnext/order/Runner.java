package com.evolutionnext.order;


import com.evolutionnext.order.adapter.out.OrderEventKafkaPublisher;
import com.evolutionnext.order.domain.aggregate.*;
import com.evolutionnext.order.domain.application.OrderApplicationService;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        String stateString =
            "AK,AL,AZ,AR,CA,CO,CT,DE,FL,GA," +
            "HI,ID,IL,IN,IA,KS,KY,LA,ME,MD," +
            "MA,MI,MN,MS,MO,MT,NE,NV,NH,NJ," +
            "NM,NY,NC,ND,OH,OK,OR,PA,RI,SC," +
            "SD,TN,TX,UT,VT,VA,WA,WV,WI,WY";

        OrderEventKafkaPublisher orderEventKafkaPublisher = new OrderEventKafkaPublisher();
        OrderApplicationService orderApplicationService = new OrderApplicationService(orderEventKafkaPublisher);

        AtomicBoolean done = new AtomicBoolean(false);

        Random random = new Random();

        try (ExecutorService executorService = Executors.newFixedThreadPool(4)) {
            while (!done.get()) {
                executorService.submit(() -> {
                    String[] states = stateString.split(",");
                    String state = states[random.nextInt(states.length)];
                    String customerId = String.valueOf(random.nextInt(10000) + 1);
                    boolean isPlaced = random.nextBoolean();
                    boolean isCancelled = random.nextInt(10) == 0;

                    Order order = Order.create(new OrderId(UUID.randomUUID().toString()), new CustomerId(customerId), state);
                    Set<Long> previousProducts = new HashSet<>();
                    for (int i = 0; i < random.nextInt(10) + 1; i++) {
                        long productId = random.nextLong(2000L) + 1L;
                        while (previousProducts.contains(productId)) {
                            productId = random.nextLong(2000L) + 1L;
                        }
                        previousProducts.add(productId);
                        int quantity = random.nextInt(20) + 1;
                        int amount = random.nextInt(300) + 1;
                        order.addOrderItem(new OrderItem(new ProductId(productId), quantity, amount));
                    }

                    orderApplicationService.submit(order);

                    if (isPlaced) {
                        order.placeOrder();
                        orderApplicationService.submit(order);
                        try {
                            Thread.sleep(random.nextInt(30000 - 1000 + 1) + 1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    if (isPlaced && isCancelled) {
                        order.cancelOrder();
                        orderApplicationService.submit(order);
                    }
                });
                Thread.sleep(random.nextInt(15000 - 1000 + 1) + 1000);
            }
        }
    }
}
