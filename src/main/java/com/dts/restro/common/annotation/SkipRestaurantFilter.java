package com.dts.restro.common.annotation;

import java.lang.annotation.*;

/**
 * Mark a Spring Data repository interface with this annotation to bypass the
 * automatic {@code restaurantFilter} Hibernate filter applied by
 * {@link com.dts.restro.common.aspect.RestaurantFilterAspect}.
 *
 * <p>Use sparingly — only for repositories that genuinely need cross-tenant
 * access (e.g. {@code UserRepository} during authentication).</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkipRestaurantFilter {
}
