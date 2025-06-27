package com.example.server.config.common;

// contains all methods: equals, hashCode, toString
public record BroadCastEvent(String topic, String message) {
}
