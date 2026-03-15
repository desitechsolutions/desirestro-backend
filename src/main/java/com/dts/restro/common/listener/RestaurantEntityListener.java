package com.dts.restro.common.listener;

import com.dts.restro.common.TenantContext;
import com.dts.restro.common.entity.RestaurantAwareEntity;
import com.dts.restro.common.util.SpringContext;
import com.dts.restro.restaurant.entity.Restaurant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA entity listener that automatically populates the {@code restaurant}
 * association on any {@link RestaurantAwareEntity} before it is persisted or
 * updated, using the restaurant ID stored in {@link TenantContext}.
 *
 * <p>This removes the need for every service method to manually set
 * {@code entity.setRestaurant(...)}.  The listener is registered on
 * {@link RestaurantAwareEntity} via {@code @EntityListeners}.</p>
 */
public class RestaurantEntityListener {

    private static final Logger log = LoggerFactory.getLogger(RestaurantEntityListener.class);

    @PrePersist
    @PreUpdate
    public void setRestaurantBeforeSave(Object entity) {
        if (!(entity instanceof RestaurantAwareEntity restaurantAware)) {
            return;
        }

        // If the restaurant is already set, nothing to do
        if (restaurantAware.getRestaurant() != null) {
            return;
        }

        Long currentRestaurantId = TenantContext.getCurrentRestaurantId();
        if (currentRestaurantId == null) {
            log.error("No restaurantId in TenantContext while saving {}", entity.getClass().getSimpleName());
            throw new IllegalStateException(
                    "No restaurantId found in TenantContext. Cannot persist tenant-scoped entity: "
                    + entity.getClass().getSimpleName());
        }

        EntityManager em = SpringContext.getBean(EntityManager.class);
        Restaurant restaurantRef = em.getReference(Restaurant.class, currentRestaurantId);
        restaurantAware.setRestaurant(restaurantRef);
        log.debug("Auto-set restaurant id={} for {}", currentRestaurantId, entity.getClass().getSimpleName());
    }
}
