package com.dts.restro.billing.dto;

import com.dts.restro.billing.enums.OrderType;
import com.dts.restro.billing.enums.PaymentMethod;
import com.dts.restro.billing.enums.TaxType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new bill
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBillRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    private Long customerId;
    
    private Integer tableNumber;
    
    @NotNull(message = "Order type is required")
    private OrderType orderType;
    
    @NotNull(message = "Tax type is required")
    private TaxType taxType;
    
    @DecimalMin(value = "0.0", message = "Service charge rate must be non-negative")
    private BigDecimal serviceChargeRate;
    
    @DecimalMin(value = "0.0", message = "Packaging charges must be non-negative")
    private BigDecimal packagingCharges;
    
    @DecimalMin(value = "0.0", message = "Delivery charges must be non-negative")
    private BigDecimal deliveryCharges;
    
    @DecimalMin(value = "0.0", message = "Discount rate must be non-negative")
    private BigDecimal discountRate;
    
    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount;
    
    private String discountReason;
    
    private PaymentMethod paymentMethod;
    
    private String paymentReference;
    
    @DecimalMin(value = "0.0", message = "Paid amount must be non-negative")
    private BigDecimal paidAmount;
    
    private Long captainId;
    
    private Long cashierId;
}

// Made with Bob
