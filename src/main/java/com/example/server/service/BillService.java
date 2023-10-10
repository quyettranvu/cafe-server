package com.example.server.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.example.server.POJO.Bill;

public interface BillService {

    ResponseEntity<String> generateReport(Map<String, Object> requestMap);

    ResponseEntity<List<Bill>> getBills();

    ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap);

    ResponseEntity<String> deleteBill(Integer id);
}
