package com.dts.restro.repository;

import com.dts.restro.entity.KOT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface KOTRepository extends JpaRepository<KOT, Long> {
    List<KOT> findByStatusInOrderByCreatedAtAsc(List<String> statuses);

    // For daily sequence
    int countByKotNumberStartingWith(String prefix);

    List<KOT> findByTableId(Long tableId);
}