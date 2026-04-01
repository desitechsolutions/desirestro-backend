package com.dts.restro.billing.enums;

/**
 * Tax types for Indian GST system
 * CGST_SGST: Central GST (9%) + State GST (9%) = 18% for intra-state transactions
 * IGST: Integrated GST (18%) for inter-state transactions
 * NO_TAX: For tax-exempt items
 */
public enum TaxType {
    CGST_SGST,  // Intra-state: 9% CGST + 9% SGST
    IGST,       // Inter-state: 18% IGST
    NO_TAX      // Tax-exempt items
}

// Made with Bob
