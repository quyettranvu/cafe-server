package com.example.server.batch.job;

import com.example.server.batch.processor.OrderItemProcessor;
import com.example.server.batch.repeat.WaitForSaleDataTasklet;
import com.example.server.dto.DailySalesReport;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class DailySalesReportJob {

    @Bean
    public Job calculateDailySalesJob(JobRepository jobRepository, Step initialStepCheckSalesDataAvailable, Step calculateDailySalesJobStep) {
        return new JobBuilder("calculateDailySalesJob", jobRepository)
                .start(initialStepCheckSalesDataAvailable)
                .next(calculateDailySalesJobStep)
                .build();
    }

    @Bean
    @Deprecated
    public Step initialStepCheckSalesDataAvailable(JobRepository jobRepository, Tasklet waitForSalesDataTasklet, PlatformTransactionManager transactionManager) {
        return new StepBuilder("initialStepCheckSalesDataAvailable", jobRepository)
                .tasklet(waitForSalesDataTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step calculateDailySalesJobStep(
            JobRepository jobRepository,
            JdbcCursorItemReader<DailySalesReport> dailySalesReportReader,
            FlatFileItemWriter<DailySalesReport> dailySalesReportWriter,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("initialStepCheckSalesDataAvailable", jobRepository)
                .<DailySalesReport, DailySalesReport>chunk(10, transactionManager) // chunk-oriented processing
                .reader(dailySalesReportReader)
                .processor(processor())
                .writer(dailySalesReportWriter)
                .build();
    }

    @Bean
    public OrderItemProcessor processor() {
        return new OrderItemProcessor();
    }

    @Bean
    @StepScope
    public Tasklet waitForSalesDataTasklet(NamedParameterJdbcTemplate jdbcTemplate,
                                          @Value("#{jobParameters['processingDate']}") String processingDate) {
        return new WaitForSaleDataTasklet(jdbcTemplate, processingDate);
    }
}
