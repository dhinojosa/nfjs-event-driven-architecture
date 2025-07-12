package com.evolutionnext.infrastructure.out.kafka;

import com.evolutionnext.domain.events.*;
import com.evolutionnext.messaging.*;
import com.evolutionnext.port.out.OrderEventPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class OrderEventKafkaPublisher implements OrderEventPublisher {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventKafkaPublisher.class);

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
        logger.info("Publishing OrderEvent {}", orderEvent);
        switch (orderEvent) {
            case OrderCreated(java.util.UUID uuid, java.time.Instant now) -> {
                OrderEventMessage message =
                    new OrderEventMessage(uuid.toString(), now, EventType.ORDER_PLACED, new OrderCreatedMessage());
                producer.send(new ProducerRecord<>(TOPIC, null, now.toEpochMilli(), uuid.toString(), message));
            }
            case OrderItemAdded(
                java.util.UUID uuid, java.util.UUID orderItemId, long productId, int quantity,
                java.math.BigDecimal price, java.time.Instant now
            ) -> {
                OrderEventMessage message =
                    new OrderEventMessage(uuid.toString(), now, EventType.ORDER_ITEM,
                        new OrderItemMessage(orderItemId, productId, OrderItemType.ADD, quantity, price.doubleValue()));
                producer.send(new ProducerRecord<>(TOPIC, null, now.toEpochMilli(), uuid.toString(), message));
            }
            case OrderDeleted(java.util.UUID uuid, java.time.Instant now) -> {
                OrderEventMessage message =
                    new OrderEventMessage(uuid.toString(), now, EventType.ORDER_CANCELLED, new OrderCancelledMessage());
                producer.send(new ProducerRecord<>(TOPIC, null, now.toEpochMilli(), uuid.toString(), message));
            }
            case OrderItemChanged(
                java.util.UUID uuid, java.util.UUID orderItemId, Long productId, int quantity,
                java.math.BigDecimal price, java.time.Instant now
            ) -> {
                OrderEventMessage message =
                    new OrderEventMessage(uuid.toString(), now, EventType.ORDER_ITEM,
                        new OrderItemMessage(orderItemId, productId, OrderItemType.UPDATE, quantity, price.doubleValue()));
                producer.send(new ProducerRecord<>(TOPIC, null, now.toEpochMilli(), uuid.toString(), message));
            }
            case OrderItemDeleted(java.util.UUID uuid, java.util.UUID orderItemId, java.time.Instant now) -> {
                OrderEventMessage message =
                    new OrderEventMessage(uuid.toString(), now, EventType.ORDER_ITEM,
                        new OrderItemMessage(orderItemId, 0L, OrderItemType.DELETE, 0, 0.0));
                producer.send(new ProducerRecord<>(TOPIC, null, now.toEpochMilli(), uuid.toString(), message));
            }
            case OrderPlaced(java.util.UUID uuid, java.time.Instant now) -> {
                OrderEventMessage message =
                    new OrderEventMessage(uuid.toString(), now, EventType.ORDER_PLACED, new OrderPlacedMessage());
                producer.send(new ProducerRecord<>(TOPIC, null, now.toEpochMilli(), uuid.toString(), message));
            }
        }
    }
}
