package com.example.server.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.example.server.POJO.Category;

public interface CategoryService {

    ResponseEntity<String> addNewCategory(Map<String, String> requestMap);

    List<Category> getAllCategory(String filterValue);

    ResponseEntity<String> updateCategory(Map<String, String> requestMap);

}
