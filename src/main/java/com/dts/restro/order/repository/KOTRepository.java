package com.dts.restro.order.repository;

import com.dts.restro.order.entity.KOT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KOTRepository extends JpaRepository<KOT, Long> {
    List<KOT> findByStatusInOrderByCreatedAtAsc(List<String> statuses);

    // For daily sequence
    int countByKotNumberStartingWith(String prefix);

    List<KOT> findByPartyId(Long partyId);
    List<KOT> findByStatus(String status);

    @Query("SELECT COALESCE(SUM(ki.quantity), 0) " +
            "FROM KOT k JOIN k.items ki " +
            "WHERE k.createdAt BETWEEN :start AND :end")
    Long getDailyItemCount(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT ki.menuItemName, SUM(ki.quantity), SUM(ki.quantity * ki.price) " +
            "FROM KOT k JOIN k.items ki " +
            "WHERE k.createdAt BETWEEN :start AND :end " +
            "GROUP BY ki.menuItemName " +
            "ORDER BY SUM(ki.quantity * ki.price) DESC " +
            "LIMIT :limit")
    List<Object[]> getTopSellingItems(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("limit") int limit
    );

    @Query("SELECT DISTINCT k FROM KOT k JOIN FETCH k.party p JOIN FETCH p.table WHERE k.status IN :statuses ORDER BY k.createdAt ASC")
    List<KOT> findActiveKOTsWithDetails(@Param("statuses") List<String> statuses);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByRestaurantId(Long restaurantId);

    long countByRestaurantIdAndCreatedAtBetween(Long restaurantId, LocalDateTime startDate, LocalDateTime endDate);
}