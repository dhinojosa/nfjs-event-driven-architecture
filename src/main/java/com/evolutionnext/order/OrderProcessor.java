package com.evolutionnext.order;

import java.time.Instant;
import java.util.List;
import java.util.Properties;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class OrderProcessor {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        props.put("schema.registry.url", "http://localhost:8081");

        try (KafkaProducer<String, OrderPlaced> producer = new KafkaProducer<>(props)) {
            List<OrderItem> orderItems = List.of(
                new OrderItem("m004", 3),
                new OrderItem("c008", 2),
                new OrderItem("c009", 1)
            );
            CreditCard creditCard = new CreditCard("2999-9991-1299", CardType.Amex);
            String orderId = "ord-1";

            Order order = new Order(orderId, "id-9", orderItems, creditCard);
            ProducerRecord<String, OrderPlaced> record = new ProducerRecord<>("order-placed", orderId, new OrderPlaced(orderId, "id-13", order, Instant.now()));
            producer.send(record);
        }
    }
}