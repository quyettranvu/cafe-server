package com.example.server.config.redis;

import com.example.server.config.common.BroadCastEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password}")
    private String redisPassword;

    /** ******************
     * Jedis Configuration
     *  ******************
     */
    // Jedis Factory
    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);
        try {
            return new JedisConnectionFactory(config);
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create JedisConnectionFactory", e);
        }
    }

    /**
     * Template to interact with Redis client: template <String, String>
     */
    @Bean
    public RedisTemplate<String, String> generateRedisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        JedisConnectionFactory factory = jedisConnectionFactory();
        if (factory == null) {
            throw new IllegalStateException("JedisConnectionFactory is null");
        }
        template.setConnectionFactory(jedisConnectionFactory());

        /* Types Mapper*/
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        return template;
    }


    /**
     * String Redis Template - using Jedis Connection Factory
     * @param jedisConnectionFactory: Jedis connection factory entry
     * @return template
     */
    @Bean(name = "redisTemplate")
    public StringRedisTemplate stringRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(jedisConnectionFactory);

        // Optionally configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }

    /** ******************
     * Lettuce Configuration
     *  ******************
     */
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);
        try {
            return new LettuceConnectionFactory(config);
        } catch (Exception e) {
            throw new BeanCreationException("Failed to create LettuceConnectionFactory", e);
        }
    }

    /**
     * Reactive Redis Template - using Lettuce Connection Factory
     * @return reactive redis template
     */
    @Bean
    public ReactiveRedisTemplate<String, BroadCastEvent> lettuceRedisTemplate() {
        LettuceConnectionFactory lettuceConnectionFactory = lettuceConnectionFactory();

        /* Types Mapper*/
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        RedisSerializationContext<String, BroadCastEvent> serializationContext = RedisSerializationContext
                .<String, BroadCastEvent>newSerializationContext(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(BroadCastEvent.class))
                .build();

        return new ReactiveRedisTemplate<>(lettuceConnectionFactory, serializationContext);
    }
}
