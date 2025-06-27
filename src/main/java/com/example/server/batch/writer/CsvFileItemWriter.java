package com.example.server.batch.writer;

import com.example.server.dto.DailySalesReport;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CsvFileItemWriter {

    @Bean
    public FlatFileItemWriter<DailySalesReport> dailySalesReportWriter() {
        return new FlatFileItemWriterBuilder<DailySalesReport>()
                .name("dailySalesReportWriter")
                .resource(new FileSystemResource("reports/daily-sales-report-" + LocalDate.now() + ".csv"))
                .delimited() // khai báo chuẩn bị phân cách và sau đó phân cách bằng dấu ,
                .delimiter(",")
                .names("itemName", "totalQuantity", "totalRevenue")
                .build();
    }
}