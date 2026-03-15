package com.dts.restro.common.aspect;

import com.dts.restro.common.TenantContext;
import com.dts.restro.common.annotation.SkipRestaurantFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that wraps every Spring Data repository method to automatically
 * enable the Hibernate {@code restaurantFilter}, ensuring row-level tenant
 * isolation without any changes in service or repository code.
 *
 * <p>Repositories annotated with {@link SkipRestaurantFilter} are excluded
 * (e.g. {@code UserRepository} which is queried globally during authentication).</p>
 *
 * <p>The filter is always disabled in the {@code finally} block so it cannot
 * leak across requests.</p>
 */
@Slf4j
@Aspect
@Component
public class RestaurantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* org.springframework.data.repository.Repository+.*(..))" +
            " && !@within(com.dts.restro.common.annotation.SkipRestaurantFilter)")
    public Object applyRestaurantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        Long restaurantId = TenantContext.getCurrentRestaurantId();

        if (restaurantId == null) {
            // Allow super-admin and unauthenticated flows (auth, actuator, swagger)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isSuperAdmin = auth != null && auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));

            if (isSuperAdmin) {
                log.debug("SUPER_ADMIN bypassing tenant filter for {}", joinPoint.getSignature().getName());
            } else {
                log.debug("No restaurantId in context for {}; proceeding without filter",
                        joinPoint.getSignature().getName());
            }
            return joinPoint.proceed();
        }

        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("restaurantFilter").setParameter("restaurantId", restaurantId);
        log.debug("Enabled restaurantFilter(id={}) for {}", restaurantId, joinPoint.getSignature().getName());

        try {
            return joinPoint.proceed();
        } finally {
            session.disableFilter("restaurantFilter");
        }
    }
}
