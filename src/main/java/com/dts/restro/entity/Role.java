package com.dts.restro.entity;

public enum Role {
    /** Platform-level super administrator (cross-tenant access) */
    SUPER_ADMIN,
    /** Restaurant owner — full access to their own restaurant */
    OWNER,
    /** Legacy role kept for backward compatibility; treated as OWNER */
    ADMIN,
    /** Floor captain managing tables and KOTs */
    CAPTAIN,
    /** Kitchen staff managing KOT fulfilment */
    KITCHEN,
    /** Cashier handling billing */
    CASHIER,
    /** General restaurant staff */
    STAFF
}
