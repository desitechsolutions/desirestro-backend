package com.dts.restro.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Party party;

    private double subtotal;

    private double gst;

    private double total;

    private LocalDateTime paidAt;

    private String paymentMode;
}