package com.dts.restro.support.repository;

import com.dts.restro.support.entity.SupportTicket;
import com.dts.restro.support.enums.TicketStatus;
import com.dts.restro.support.enums.TicketPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    Page<SupportTicket> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    Page<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status, Pageable pageable);

    Page<SupportTicket> findByPriorityOrderByCreatedAtDesc(TicketPriority priority, Pageable pageable);

    Page<SupportTicket> findByAssignedToIdOrderByCreatedAtDesc(Long assignedToId, Pageable pageable);

    @Query("SELECT t FROM SupportTicket t WHERE t.status IN :statuses ORDER BY t.priority DESC, t.createdAt ASC")
    Page<SupportTicket> findByStatusInOrderByPriorityDescCreatedAtAsc(
            @Param("statuses") List<TicketStatus> statuses, Pageable pageable);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.restaurant.id = :restaurantId AND t.status = :status")
    Long countByRestaurantAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") TicketStatus status);

    @Query("SELECT COUNT(t) FROM SupportTicket t WHERE t.status = :status")
    Long countByStatus(@Param("status") TicketStatus status);

    @Query("SELECT t FROM SupportTicket t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    Page<SupportTicket> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}

// Made with Bob
