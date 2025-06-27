package com.example.server.config.kafka.producers;

import java.util.List;

import com.example.server.config.kafka.producers.sender.KafkaMessageSender;
import com.example.server.dto.CustomerOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventProducer {

    @Autowired
    private KafkaMessageSender kafkaMessageSender;

    /**
     *
     * @param topic: topic of the channel
     * @param customerOrder: Customer Order exported with bill
     * @throws JsonProcessingException: error in processing json
     */
    public void sendOrderEvent(String topic, CustomerOrder customerOrder) throws JsonProcessingException {
        var key = customerOrder.getId();
        List<Header> recordHeader = List.of(new RecordHeader("event-source", "order-event-producer".getBytes()));
        var orderProducerRecord = new ProducerRecord<>(topic, null, key, customerOrder, recordHeader);
        kafkaMessageSender.sendMessage(topic, key, orderProducerRecord, recordHeader);
    }
}
