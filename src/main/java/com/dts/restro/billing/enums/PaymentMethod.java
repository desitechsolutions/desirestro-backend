package com.dts.restro.billing.enums;

/**
 * Payment methods supported by the restaurant
 */
public enum PaymentMethod {
    CASH,           // Cash payment
    UPI,            // UPI payment (PhonePe, Google Pay, Paytm, etc.)
    CARD,           // Credit/Debit card payment
    WALLET,         // Digital wallet (Paytm, PhonePe wallet, etc.)
    CREDIT_ACCOUNT, // Credit account for regular customers
    ONLINE,         // Online payment gateway
    NET_BANKING,    // Net banking
    CREDIT          // Alias for CREDIT_ACCOUNT
}

// Made with Bob
