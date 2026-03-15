package com.dts.restro.repository;

import com.dts.restro.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByPartyId(Long partyId);

    List<Bill> findAllByOrderByPaidAtDesc();

    @Query("SELECT COALESCE(SUM(b.total), 0), COUNT(b) FROM Bill b WHERE b.paidAt BETWEEN :start AND :end")
    List<Object[]> getDailyBillStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT DATE(b.paidAt), COALESCE(SUM(b.total), 0) " +
            "FROM Bill b " +
            "WHERE b.paidAt BETWEEN :start AND :end " +
            "GROUP BY DATE(b.paidAt) " +
            "ORDER BY DATE(b.paidAt)")
    List<Object[]> getRevenueByDay(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
