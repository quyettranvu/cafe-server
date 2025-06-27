package com.example.server.batch.repeat;

import lombok.NonNull;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class WaitForSaleDataTasklet implements Tasklet {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final String processingDate;

    public WaitForSaleDataTasklet(NamedParameterJdbcTemplate namedParameterJdbcTemplate, String processingDate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.processingDate = processingDate;
    }

    /**
     *
     * @param stepContribution: tasklet implemented bean in step
     * @param chunkContext: context of the current chunk
     * @return RepeatStatus to check the status of repeating action
     * @throws Exception: error in repeat execution
     */
    @Override
    public RepeatStatus execute(@NonNull StepContribution stepContribution, @NonNull ChunkContext chunkContext) throws Exception {
        LocalDate executionDate = LocalDate.now();
        if (processingDate != null && !processingDate.isEmpty()) {
            executionDate = LocalDate.parse(processingDate);
        }

        LocalDateTime startOfDay = executionDate.atStartOfDay();
        LocalDateTime nextStartOfDay = executionDate.plusDays(1).atStartOfDay();

        String querySql = """
            SELECT COUNT(*)\s
            FROM orders\s
            WHERE order_time >= :startTime\s
              AND order_time <  :endTime
        """;

        MapSqlParameterSource queryParams = new MapSqlParameterSource()
                .addValue("startTime", Timestamp.valueOf(startOfDay))
                .addValue("endTime", Timestamp.valueOf(nextStartOfDay));

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(60_000); // internal sleeping of a system: 1 second, not blocking main thread

        RetryTemplate template = new RetryTemplate();
        RetryTemplate.builder().maxAttempts(5).customBackoff(backOffPolicy).build();

        template.execute(context -> {
            Integer orderCount = namedParameterJdbcTemplate.queryForObject(querySql, queryParams, Integer.class);

            if (orderCount != null && orderCount > 0) {
                System.out.println("Data ready. Proceeding with report generation.");
                return RepeatStatus.FINISHED;
            } else {
                System.out.println("Waiting for data to be available...");
                return RepeatStatus.CONTINUABLE;
            }
        });

        return RepeatStatus.FINISHED;
    }
}