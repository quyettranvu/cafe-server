package com.example.server.config.redis;

import com.example.server.config.common.BroadCastEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Value("${spring.redis.channel-name}")
    private String channelName;

    @Autowired
    private RedisConfig redisConfig;

    private RedisTemplate<String, String> redisTemplate;

    private ReactiveRedisTemplate<String, BroadCastEvent> lettuceRedisTemplate;

    /**
     * Jedis Redis Template + Lettuce Redis Template Initialization
     */
    @PostConstruct
    private void init() {
        redisTemplate = redisConfig.generateRedisTemplate();
        lettuceRedisTemplate = redisConfig.lettuceRedisTemplate();
    }

    /**
     * Lettuce Redis Template is configured to bound with Redis Stream
     */
    @PostConstruct()
    private void subscribe() {
        lettuceRedisTemplate.listenTo(ChannelTopic.of(channelName))
                .map(ReactiveSubscription.Message<String, BroadCastEvent>::getMessage)
                .subscribe(System.out::println);
    }

    // Store/retrieve data: single object, list object
    private void storeData(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    private void cachedDataWithTTL(String key, String value, long ttlInSeconds) {
        redisTemplate.opsForValue().set(key, value);
        redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
    }

    private void retrieveData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.get(key);
    }

    private void addToList(String key, String value) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        listOperations.leftPush(key, value);
    }

    private List getList(String key) {
        ListOperations<String, String> listOperations = redisTemplate.opsForList();
        return listOperations.range(key,0, -1);
    }

    // Distributed lock -> prevent deadlock
    private boolean acquireLock(String lockKey, String requestId, int expireTime) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, requestId, expireTime, TimeUnit.SECONDS));
    }

    private void releaseLock(String lockKey, String requestId) {
        if (requestId.equals(redisTemplate.opsForValue().get(lockKey))) {
            redisTemplate.delete(lockKey);
        }
    }

    // Clear keys by pattern
    private void clearKeysByPattern(String pattern) {
        Set<String> listKeys = redisTemplate.keys(pattern);
        if (!listKeys.isEmpty()) {
            redisTemplate.delete(listKeys);
        }
    }

    // Pub-sub messaging: subscribe to channel using Lettuce Reactive Redis template
    private void publish(BroadCastEvent event) {
        lettuceRedisTemplate.convertAndSend(channelName, event).subscribe();
    }

    // Real-time analytics
    private void trackEvent(String event) {
        redisTemplate.opsForHyperLogLog().add("events", event);
    }

    private long countDistinctEvents() {
        return redisTemplate.opsForHyperLogLog().size("events");
    }
}
