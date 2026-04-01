package com.dts.restro.common.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Provides static access to the Spring {@link ApplicationContext}.
 * Required by JPA {@link jakarta.persistence.EntityListener} classes
 * (e.g. {@link com.dts.restro.common.listener.RestaurantEntityListener})
 * that are not Spring-managed beans.
 */
@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
