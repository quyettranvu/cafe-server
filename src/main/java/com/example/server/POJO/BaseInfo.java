package com.example.server.POJO;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@MappedSuperclass
public class BaseInfo {

    @Column(name = "createdBy")
    private String createdBy;

    @Column(name = "createdAt")
    private LocalDate createdAt;

    @Column(name = "updatedBy")
    private String updatedBy;

    @Column(name = "updatedAt")
    private LocalDate updatedAt;

    @Column(name = "deletedFlg")
    private Boolean deletedFlg;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDate.now();
    }
    
}
