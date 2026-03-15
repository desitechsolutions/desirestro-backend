package com.dts.restro.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a restaurant tenant on the DesiRestro platform.
 * Each restaurant has one owner (User with role OWNER) and its own
 * isolated set of tables, menus, KOTs, bills, staff, and inventory.
 */
@Entity
@Table(name = "restaurant")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false)
    private String name;

    @Size(max = 255)
    private String address;

    @Size(max = 20)
    private String phone;

    @Size(max = 150)
    private String email;

    /** GST Identification Number (optional) */
    @Size(max = 20)
    private String gstin;

    /** State/city for GST and display purposes */
    @Size(max = 100)
    private String state;

    /** GST rate applied to all bills from this restaurant (default 18%) */
    @Column(nullable = false)
    private double gstRate = 18.0;

    /** Short code / slug used to identify the restaurant in URLs or displays */
    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
