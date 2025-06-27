package com.example.server.batch.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailySalesScheduler {

    private final JobLauncher jobLauncher;

    private final Job dailySalesReportJob;

    public DailySalesScheduler(JobLauncher jobLauncher,
                               @Qualifier("calculateDailySalesJob") Job dailySalesReportJob) {
        this.jobLauncher = jobLauncher;
        this.dailySalesReportJob = dailySalesReportJob;
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void runDailySalesReport() throws Exception {
        String processingDate = LocalDate.now().toString();
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("processingDate", processingDate)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(dailySalesReportJob, jobParameters);
    }
}
