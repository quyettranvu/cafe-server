package com.example.server.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.server.POJO.Category;

public interface CategoryDao extends JpaRepository<Category, Integer> {

    List<Category> getAllCategory();

}
