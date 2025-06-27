package com.example.server.POJO;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Data;

@EqualsAndHashCode(callSuper = true)
@NamedQuery(name = "Category.getAllCategory", query = "select c from Category c where c.id in (select p.category.id from Product p where p.status='true')")

@Data // replace for constructor, settter and getter
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "category")
public class Category extends BaseInfo implements Serializable {

    // specify the serialized version of the class within tranmission
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;
    
}
