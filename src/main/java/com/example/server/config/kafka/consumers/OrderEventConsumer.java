package com.example.server.config.kafka.consumers;


import com.example.server.dto.CustomerOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventConsumer {

    @KafkaListener(topics = "orders", groupId = "kitchen-group", containerFactory = "kafkaListenerContainerFactory")
    private void listenOrderFromBillPdf(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            ConsumerRecord<String, CustomerOrder> orderConsumerRecord,
            Acknowledgment acknowledgment) {
        log.info("RAW JSON string customer order data: {}", message);
        log.info("Current topic is: {} ", topic);
        log.info("ID of assigned partition for the listener on the channel: {}", partition);
        log.info("Order consumer record: {}", orderConsumerRecord.value()); // notice that here the value of the record is any result of JSON deserialization
        // Process order in the kitchen

        // after business logic, manually acknowledge for offset commit + once processing (position in partition where a message is read and processed successfully)
        acknowledgment.acknowledge();
    }
}
