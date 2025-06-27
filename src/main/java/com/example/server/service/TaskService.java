package com.example.server.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class TaskService {

    @Autowired
    KafkaAdmin kafkaAdmin;

    /**
     *
     * @param topicName: Name of the topic
     * @throws ExecutionException: Execution exception
     * @throws InterruptedException: Interrupted exception
     */
    public void createNewTopic(String topicName) throws ExecutionException, InterruptedException {
        Map<String, String> topicConfiguration = new HashMap<>();
        topicConfiguration.put(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(24 * 60 * 60 * 1000));

        NewTopic newCreatedTopic = new NewTopic(topicName, 1, (short) 1).configs(topicConfiguration);

        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            // non-blocking wait until all topics created before procesisng next execution
            adminClient.createTopics(Collections.singletonList(newCreatedTopic)).all().get();
        }
    }
}
