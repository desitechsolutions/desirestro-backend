# Repository Methods to Add

## KOTRepository
Add these methods to `src/main/java/com/dts/restro/order/repository/KOTRepository.java`:

```java
import java.time.LocalDateTime;

long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
long countByRestaurantId(Long restaurantId);
long countByRestaurantIdAndCreatedAtBetween(Long restaurantId, LocalDateTime startDate, LocalDateTime endDate);
```

## BillRepository
Add these methods to `src/main/java/com/dts/restro/billing/repository/BillRepository.java`:

```java
import java.util.List;

List<Bill> findByRestaurantId(Long restaurantId);
```

## MenuItemRepository
Add these methods to `src/main/java/com/dts/restro/menu/repository/MenuItemRepository.java`:

```java
long countByRestaurantId(Long restaurantId);
```

## RestaurantTableRepository
Add these methods to `src/main/java/com/dts/restro/order/repository/RestaurantTableRepository.java`:

```java
long countByRestaurantId(Long restaurantId);
```

Note: These methods follow Spring Data JPA naming conventions and will be automatically implemented.
The SuperAdminService uses these methods to calculate statistics.