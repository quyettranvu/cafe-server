package com.example.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerOrder {
    private Integer id;
    private String name;
    private String contactNumber;
    private String paymentMethod;
    private Integer total;
    private String productDetails;
}
