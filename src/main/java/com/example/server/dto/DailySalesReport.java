package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailySalesReport {
    private String itemName;
    private int totalQuantity;
    private BigDecimal totalRevenue;
}
