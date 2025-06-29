package com.example.server.config.redis;

import com.example.server.config.common.BroadCastEvent;
import com.example.server.constants.SystemConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis Pub/Sub is suitable for scenery real-time updates to connected WebSocket clients.
 */
@Service
public class RedisConsumer {

    @Value("${spring.redis.channel-name}")
    private String channelName;

    private final SimpMessagingTemplate websocketTemplate; //STOMP

    @Autowired
    public RedisConsumer(SimpMessagingTemplate websocketTemplate) {
        this.websocketTemplate = websocketTemplate;
    }

    /**
     * Message listener handles message and forward them via Websocket
     */
    public void handleMessage(String message) {
        BroadCastEvent event = deserializeMessage(message);
        websocketTemplate.convertAndSend("/topic/" + event.topic(), event.message());
    }

    /**
     * utility function to deserialize a message (if required)
     */
    private BroadCastEvent deserializeMessage(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(message, BroadCastEvent.class);
        } catch (Exception e) {
            throw new RuntimeException(SystemConstants.DESERIALIZE_MESSAGE_FAILED, e);
        }
    }

    /**
     * Redis message listener adapter delegates to handleMessage
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter() {
        return new MessageListenerAdapter(this, "handleMessage");
    }

    /**
     * Redis container listens to a specific given container
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(lettuceConnectionFactory);
        redisMessageListenerContainer.addMessageListener(messageListenerAdapter(), new ChannelTopic(channelName));
        return redisMessageListenerContainer;
    }
}
