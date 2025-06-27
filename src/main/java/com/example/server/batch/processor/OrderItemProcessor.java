package com.example.server.batch.processor;


import com.example.server.dto.DailySalesReport;
import lombok.NonNull;
import org.springframework.batch.item.ItemProcessor;

public class OrderItemProcessor implements ItemProcessor<DailySalesReport, DailySalesReport> {

    @Override
    public DailySalesReport process(@NonNull DailySalesReport dailySalesReport) throws Exception {
        return dailySalesReport;
    }
}
