package com.example.server.batch.scheduler;

import com.example.server.config.redis.RedisConfig;
import com.example.server.config.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class DailySalesScheduler {

    private final JobLauncher jobLauncher;

    private final Job dailySalesReportJob;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisConfig redisConfig;

    public DailySalesScheduler(JobLauncher jobLauncher,
                               @Qualifier("calculateDailySalesJob") Job dailySalesReportJob) {
        this.jobLauncher = jobLauncher;
        this.dailySalesReportJob = dailySalesReportJob;
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void runDailySalesReport() throws Exception {
        String processingDate = LocalDate.now().toString();
        log.info("Starting daily sales report for {}", processingDate);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("processingDate", processingDate)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(dailySalesReportJob, jobParameters);
        redisService.triggerRedisSnapshot();
        log.info("Daily report and Redis snapshot completed.");
    }
}
