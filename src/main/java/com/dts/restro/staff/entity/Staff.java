package com.dts.restro.staff.entity;

import com.dts.restro.auth.entity.User;
import com.dts.restro.common.entity.RestaurantAwareEntity;
import com.dts.restro.staff.entity.Attendance;
import com.dts.restro.staff.entity.Leave;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "staff")
@Data
@EqualsAndHashCode(callSuper = false)
public class Staff extends RestaurantAwareEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String fullName;

    private String phone;
    private String email;
    private LocalDate joinDate;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<Attendance> attendances;

    @OneToMany(mappedBy = "staff", cascade = CascadeType.ALL)
    private List<Leave> leaves;
}