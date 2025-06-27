package com.example.server.config.kafka.topics;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaOrderTopicConfig {

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name("orders")
                .partitions(1)
                .replicas(1)
                .config("cleanup.policy", "compact") // event sourcing, save/cache the latest state, revoke state
                .build();
    }
}
