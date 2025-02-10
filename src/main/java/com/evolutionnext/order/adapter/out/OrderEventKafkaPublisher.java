package com.evolutionnext.order.adapter.out;

import com.evolutionnext.adapter.out.*;
import com.evolutionnext.order.domain.aggregate.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class OrderEventKafkaPublisher implements OrderEventPublisher {

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            "io.confluent.kafka.serializers.KafkaAvroSerializer"
        );
        properties.put("schema.registry.url", "http://localhost:8081");
        return properties;
    }

    @Override
    public void publish(OrderEvent orderEvent) {
        OrderEventMessage orderEventMessage = switch(orderEvent) {
            case OrderPlaced orderPlaced -> toAvro(orderPlaced);
            case OrderCancelled orderCancelled -> toAvro(orderCancelled);
            case OrderCreated orderCreated -> toAvro(orderCreated);
        };
        publishToKafka(orderEventMessage);
    }


    private OrderItemMessage toAvro(OrderItem orderItem) {
        return new OrderItemMessage(orderItem.productId().id(), orderItem.quantity(), orderItem.price());
    }

    private OrderEventMessage toAvro(OrderPlaced orderPlaced) {
        List<OrderItemMessage> list = orderPlaced.order().getOrderItems().stream().map(this::toAvro).toList();
        return new OrderEventMessage(orderPlaced.order().getOrderId().id(), Instant.now(),
            EventType.ORDER_PLACED, new OrderPlacedMessage(
            new OrderMessage(orderPlaced.order().getOrderId().id(), orderPlaced.order().getCustomerId().value(), list)));
    }

    private OrderEventMessage toAvro(OrderCancelled orderCancelled) {
        return new OrderEventMessage(orderCancelled.order().getOrderId().id(), Instant.now(),
            EventType.ORDER_CANCELLED, new OrderCancelledMessage(orderCancelled.order().getOrderId().id(), orderCancelled.reason()));
    }

    private OrderEventMessage toAvro(OrderCreated orderCreated) {
        return new OrderEventMessage(
            orderCreated.order().getOrderId().id(),
            Instant.now(), EventType.ORDER_CREATED, new OrderCreatedMessage(orderCreated.order().getOrderId().id()));
    }


    private void publishToKafka(OrderEventMessage orderEventMessage) {
        try (KafkaProducer<String, OrderEventMessage> producer = new KafkaProducer<>(getProperties())) {
            ProducerRecord<String, OrderEventMessage> producerRecord =
                new ProducerRecord<>("orders_avro", orderEventMessage.getOrderId().toString(),
                    orderEventMessage);
            producer.send(producerRecord, (metadata, e) -> {
                if (metadata != null) {
                    System.out.println(producerRecord.key());
                    System.out.println(producerRecord.value());

                    if (metadata.hasOffset()) {
                        System.out.format("offset: %d\n",
                            metadata.offset());
                    }
                    System.out.format("partition: %d\n",
                        metadata.partition());
                    System.out.format("timestamp: %d\n",
                        metadata.timestamp());
                    System.out.format("topic: %s\n", metadata.topic());
                    System.out.format("toString: %s\n",
                        metadata);
                } else {
                    System.out.println("ERROR! ");
                    String firstException =
                        Arrays.stream(e.getStackTrace())
                            .findFirst()
                            .map(StackTraceElement::toString)
                            .orElse("Undefined Exception");
                    System.out.println(firstException);
                }
            });
        }
    }
}
