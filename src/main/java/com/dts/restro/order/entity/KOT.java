package com.dts.restro.order.entity;

import com.dts.restro.billing.enums.OrderType;
import com.dts.restro.common.entity.RestaurantAwareEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kot")
@Data
@EqualsAndHashCode(callSuper = false)
public class KOT extends RestaurantAwareEntity {

    private String kotNumber;

    @ManyToOne
    @JoinColumn(name = "party_id")
    private Party party;

    @ElementCollection
    private List<KOTItem> items = new ArrayList<>();

    private String status = "NEW"; // NEW, PREPARING, READY, SERVED

    // Indian Restaurant Features
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 20)
    private OrderType orderType = OrderType.DINE_IN;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
}