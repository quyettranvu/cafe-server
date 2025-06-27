package com.example.server.config.kafka.consumers.backup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ErrorRetriesHandler {

    @Bean
    @Primary //override Spring Boot's default one if any
    public CommonErrorHandler errorHandler() {
        // interval between retries for more customizations
        var exponentialBackOff = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOff.setInitialInterval(1000L);
        exponentialBackOff.setMultiplier(2L);
        exponentialBackOff.setMaxInterval(4000L);

        // ignore some illegal and not retryable argument exceptions
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(exponentialBackOff);
        var exceptionsToIgnoreList = List.of(IllegalArgumentException.class);
        exceptionsToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);
        errorHandler.setRetryListeners((consumerRecord, ex, deliveryAttempt) -> {
            log.info("Failed Record in Retry Listener, Exception: {}, deliveryAttempt: {}",
                    ex.getMessage(), deliveryAttempt);
        });

        // we can also use DeadLetterPublishingRecoverer to publish failed messages on another topic
        return errorHandler;
    }
}
