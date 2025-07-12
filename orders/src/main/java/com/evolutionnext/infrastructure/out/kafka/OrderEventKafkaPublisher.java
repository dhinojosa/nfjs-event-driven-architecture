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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
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
    }
}
