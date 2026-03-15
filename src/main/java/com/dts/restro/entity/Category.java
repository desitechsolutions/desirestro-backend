package com.dts.restro.entity;

import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Entity
@Table(name = "category")
@Data
@EqualsAndHashCode(callSuper = false)
public class Category extends RestaurantAwareEntity {

    private String name;

    private Integer displayOrder;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<MenuItem> items;
}