package com.evolutionnext.order.adapter.out;

import com.evolutionnext.order.domain.aggregate.Order;
import com.evolutionnext.order.domain.aggregate.OrderEvent;
import com.evolutionnext.order.domain.aggregate.OrderEventPublisher;
import com.evolutionnext.order.domain.aggregate.OrderPlaced;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class OrderEventKafkaPublisher implements OrderEventPublisher {

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            "localhost:9092");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            IntegerSerializer.class);
        return properties;
    }

    @Override
    public void publish(OrderEvent orderEvent) {
        if (Objects.requireNonNull(orderEvent) instanceof OrderPlaced(Order order)) {
            publishToKafka(order);
        } else {
            System.out.println("We are only sending new orders to Kafka at the moment");
        }
    }

    private void publishToKafka(Order order) {
        try (KafkaProducer<String, Integer> producer = new KafkaProducer<>(getProperties())) {
            ProducerRecord<String, Integer> producerRecord =
                new ProducerRecord<>("orders_plain", order.getState(), order.getTotal());
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
