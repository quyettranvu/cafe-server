package com.example.server.config.redis;

import com.example.server.constants.RedisConstants;
import com.example.server.constants.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RedisConsumerStream {

    @Value("${spring.redis.stream-name}")
    private String streamName;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SimpMessagingTemplate websocketTemplate;

    @PostConstruct
    public void initialize() {
        // key: Redis stream key, HK: field(hash key) of message, HV: value(hash value) of message
        StreamOperations<String, Object, Object> streamOperations = redisTemplate.opsForStream();

        try {
            streamOperations.createGroup(streamName, RedisConstants.CONSUMER_GROUP);
        } catch (Exception e) {
            log.error(SystemConstants.CONSUMER_GROUP_EXISTED);
        }

        // consume a message on background thread
        new Thread(this::consumeMessages).start();
    }

    public void consumeMessages() {
        // the message is returned to Record when passed simply from publisher
        StreamOperations<String, Object, Object> streamOperations = redisTemplate.opsForStream();

        @SuppressWarnings("unchecked")
        StreamOffset<String>[] offsets = new StreamOffset[] {
                StreamOffset.create(streamName, ReadOffset.lastConsumed())
        };

        while (!Thread.currentThread().isInterrupted()) {
            List<MapRecord<String, Object, Object>> messages = streamOperations.read(
                    Consumer.from(RedisConstants.CONSUMER_GROUP, RedisConstants.CONSUMER_NAME),
                    StreamReadOptions.empty().count(10).block(Duration.ofMillis(5000)), // number of messages returned per stream: 10, max wait for new message: 5 s
                    offsets
            );

            if (messages != null && !messages.isEmpty()) {
                for (MapRecord<String, Object, Object> message : messages) {
                    Map<Object, Object> body = message.getValue();

                    String topic = (String) body.get("topic");
                    String msg = (String) body.get("message");

                    //Forward message to websocket
                    websocketTemplate.convertAndSend("/topic/" + topic, msg);

                    // the server will add messages to PEL, mark as acknowledged -> consumed
                    streamOperations.acknowledge(streamName, RedisConstants.CONSUMER_GROUP, message.getId());
                }
            }
        }
    }
}
