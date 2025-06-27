package com.example.server.config.redis;

import com.example.server.config.common.StreamDataEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RedisPublisherStream {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisPublisherStream(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void publish(String streamName, StreamDataEvent event) {
        StreamOperations<String, Object, Object> streamOps = redisTemplate.opsForStream();

        Map<String, String> payload = new HashMap<>();
        payload.put("topic", event.topic());
        payload.put("message", event.message());

        RecordId recordId = streamOps.add(streamName, payload);
        log.info("Published message to stream '{}' : '{}'", streamName, event);
        log.info("Record ID: {}", recordId);
    }
}
