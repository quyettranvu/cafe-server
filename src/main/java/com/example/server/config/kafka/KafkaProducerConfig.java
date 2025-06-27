package com.example.server.config.kafka;

import com.example.server.constants.KafkaConstants;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.RoutingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> configProducerProps = new HashMap<>();
        configProducerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.KAFKA_BOOTSTRAP_SERVERS_DOMAIN);
        configProducerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProducerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        configProducerProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProducerProps.put(ProducerConfig.RETRIES_CONFIG, 10);
        configProducerProps.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);

        // For transactional messaging
        configProducerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
//        configProducerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "tx-producer-id");

        DefaultKafkaProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(configProducerProps);
//        factory.setTransactionIdPrefix("tx-");

        return factory;
    }

    @Bean
    public ProducerFactory<Object, Object> nonTransactionalProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaConstants.KAFKA_BOOTSTRAP_SERVERS_DOMAIN);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<?, ?> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // --- RoutingKafkaTemplate (for multiple serializers) ---
    @Bean
    public RoutingKafkaTemplate routingTemplate(GenericApplicationContext context) {
        // register the non-transactional producer factory as bean
        context.registerBean("bytesPF", ProducerFactory.class, this::nonTransactionalProducerFactory);

        Map<Pattern, ProducerFactory<Object, Object>> map = new LinkedHashMap<>();
        map.put(Pattern.compile("default-topic"), nonTransactionalProducerFactory());
        map.put(Pattern.compile("orders-events"), producerFactory()); // Default PF with JsonSerializer
        return new RoutingKafkaTemplate(map);
    }
}
