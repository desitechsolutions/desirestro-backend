package com.dts.restro.common;

/**
 * Stores the current tenant (restaurant) ID in a per-request ThreadLocal.
 * Set by {@link com.dts.restro.security.JwtAuthenticationFilter} after JWT validation.
 * Cleared by the same filter in its {@code finally} block to prevent leaks.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_RESTAURANT_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static void setCurrentRestaurantId(Long restaurantId) {
        CURRENT_RESTAURANT_ID.set(restaurantId);
    }

    public static Long getCurrentRestaurantId() {
        return CURRENT_RESTAURANT_ID.get();
    }

    public static void clear() {
        CURRENT_RESTAURANT_ID.remove();
    }
}
