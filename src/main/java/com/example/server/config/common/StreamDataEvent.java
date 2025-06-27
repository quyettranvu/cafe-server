package com.example.server.config.common;

import com.example.server.constants.RedisConstants;

public record StreamDataEvent(String message, String topic ) {

    public StreamDataEvent {
        if (topic == null || topic.isBlank()) {
            topic = RedisConstants.DEFAULT_TOPIC;
        }
    }
}
