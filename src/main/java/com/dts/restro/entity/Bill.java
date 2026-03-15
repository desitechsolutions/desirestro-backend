package com.dts.restro.entity;

import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "bill")
@Data
@EqualsAndHashCode(callSuper = false)
public class Bill extends RestaurantAwareEntity {

    @OneToOne
    @JoinColumn(name = "party_id")
    private Party party;

    private double subtotal;

    private double gst;

    private double total;

    private LocalDateTime paidAt;

    private String paymentMode;
}