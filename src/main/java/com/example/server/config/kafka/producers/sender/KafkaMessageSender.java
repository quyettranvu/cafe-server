package com.example.server.config.kafka.producers.sender;

import lombok.extern.slf4j.Slf4j;
import java.util.List;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.RoutingKafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class KafkaMessageSender {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    RoutingKafkaTemplate routingKafkaTemplate;

    /**
     *
     * @param topic: topic of channel
     * @param key: id key
     * @param value: passed value
     * @param headers: metadata headers
     * @param <K>: key generic type
     * @param <V>: value generic type
     */
    public <K, V> void sendMessage(
            String topic,
            K key,
            V value,
            List<Header> headers
    ) {
        ProducerRecord<K, V> producerRecord = new ProducerRecord<>(topic, null, key, value, headers);

        // use Kafka transaction
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.executeInTransaction(kt -> kt.send(topic, (String) key, producerRecord));

        future.whenComplete(((sendResult, throwable) -> {
            if (throwable != null) {
                handleFailure(key, value, throwable);
            } else {
                handleSuccess(key, value, sendResult);
            }
        }));
    }

    /**
     *
     * @param topic: topic of the channel
     * @param value: passed value
     * @param <K>: key generic type
     * @param <V>: value generic type
     */
    public <K, V> void sendRoutingMessage(
            String topic,
            V value
    ) {
        CompletableFuture<SendResult<Object, Object>> future = routingKafkaTemplate.send(topic, value);

        future.whenComplete(((sendResult, throwable) -> {
            if (throwable != null) {
                handleRoutingFailure(value, throwable);
            } else {
                handleRoutingSuccess(value, sendResult);
            }
        }));
    }

    /**
     * Handle success publish message to channel of Kafka template
     */
    private <K,V> void handleSuccess(K key, V value, SendResult<String, Object> sendResult) {

        log.info("Message sent successfully for the key: {} and the value: {}, partition is: {}",
                key, value, sendResult.getRecordMetadata().partition());
    }

    /**
     * Handle success publish message to channel of Kafka routing template
     */
    private <K,V> void handleRoutingSuccess(V value, SendResult<K, V> sendResult) {

        log.info("Routing message sent successfully for the value: {}, partition is: {}",
                value, sendResult.getRecordMetadata().partition());
    }

    /**
     * Handle failure publish message to channel of Kafka template
     */
    private <K,V> void handleFailure(K key, V value, Throwable throwable) {
        log.error("Error sending message and exception is {}", throwable.getMessage(), throwable);
    }

    /**
     * Handle failure publish message to channel of Kafka routing template
     */
    private <K,V> void handleRoutingFailure(V value, Throwable throwable) {
        log.error("Error sending message and exception is {}", throwable.getMessage(), throwable);
    }
}
