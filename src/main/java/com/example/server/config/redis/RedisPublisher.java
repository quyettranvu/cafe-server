package com.example.server.config.redis;

import com.example.server.config.common.BroadCastEvent;
import com.example.server.constants.SystemConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    @Value("${spring.redis.channel-name}")
    private String channelName;

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisPublisher(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Publish message to channel, then it will be sent to all Websocket Clients
     * @param event: custom type of carry data object for communication between Redis Pub/Sub and Websocket
     */
    public void publish(BroadCastEvent event) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(event);
            redisTemplate.convertAndSend(channelName, message);
        } catch (Exception e) {
            throw new RuntimeException(SystemConstants.PUBLISH_MESSAGE_FAILED, e);
        }
    }
}
