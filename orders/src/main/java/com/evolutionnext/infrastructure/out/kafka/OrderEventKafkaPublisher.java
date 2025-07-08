package com.evolutionnext.infrastructure.out.kafka;


import com.evolutionnext.domain.events.OrderCancelled;
import com.evolutionnext.domain.events.OrderCreated;
import com.evolutionnext.domain.events.OrderEvent;
import com.evolutionnext.domain.events.OrderPlaced;
import com.evolutionnext.messaging.*;
import com.evolutionnext.port.out.OrderEventPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Properties;

public class OrderEventKafkaPublisher implements OrderEventPublisher {

    private final Producer<String, OrderEventMessage> producer;
    private static final String TOPIC = "order-events";

    public OrderEventKafkaPublisher() {
        producer = new KafkaProducer<>(getProperties());
    }

    private Properties getProperties() {
        final Properties properties;
        properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        return properties;
    }

    @Override
    public void publish(OrderEvent orderEvent) {
        switch (orderEvent) {
            case OrderCreated orderCreated -> {
                OrderEventMessage event = createOrderCreatedMessage(orderCreated);
                producer.send(new ProducerRecord<>(TOPIC, orderCreated.order().getOrderId().toString(), event));
            }
            case OrderPlaced orderPlaced -> {
                OrderEventMessage event = createOrderPlacedMessage(orderPlaced);
                producer.send(new ProducerRecord<>(TOPIC, orderPlaced.order().getOrderId().toString(), event));
            }
            case OrderCancelled orderCancelled -> {
                OrderEventMessage event = createOrderCancelledMessage(orderCancelled);
                producer.send(new ProducerRecord<>(TOPIC, orderCancelled.order().getOrderId().toString(), event));
            }
        }
    }

    private OrderEventMessage createOrderCancelledMessage(OrderCancelled orderCancelled) {
        return new OrderEventMessage(orderCancelled.order().getOrderId().toString(),
            Instant.now(),
            EventType.ORDER_CANCELLED, new OrderCancelledMessage(orderCancelled.reason()));
    }

    private OrderEventMessage createOrderPlacedMessage(OrderPlaced orderPlaced) {
        return new OrderEventMessage(orderPlaced.order().getOrderId().toString(),
            Instant.now(),
            EventType.ORDER_PLACED, new OrderPlacedMessage());
    }

    private OrderEventMessage createOrderCreatedMessage(OrderCreated orderCreated) {
        return new OrderEventMessage(orderCreated.order().getOrderId().id(),
            Instant.now(), EventType.ORDER_CREATED, new OrderCreatedMessage());
    }
}
