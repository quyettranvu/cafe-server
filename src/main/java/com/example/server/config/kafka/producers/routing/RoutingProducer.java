package com.example.server.config.kafka.producers.routing;

import com.example.server.config.kafka.producers.sender.KafkaMessageSender;
import com.example.server.dto.CustomerOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoutingProducer {

    @Autowired
    private KafkaMessageSender kafkaMessageSender;

    /**
     * On default topic sent data will be bytes[]
     * @param message: sent message
     */
    public void sendDefaultTopic(String message) {
        kafkaMessageSender.sendRoutingMessage("default-topic", message.getBytes());
    }

    /**
     * On order event topic sent data will be type Object
     * @param customerOrder: sent customer order exported with bill
     */
    public void sendOrderEvent(CustomerOrder customerOrder) {
        kafkaMessageSender.sendRoutingMessage("orders-events", customerOrder);
    }
}
