package com.dts.restro.order.repository;

import com.dts.restro.order.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByTableIdAndStatus(Long tableId, String status);

    List<Party> findByStatus(String status);

    @Query("SELECT p FROM Party p JOIN FETCH p.table WHERE p.id = :id")
    Optional<Party> findByIdWithTable(@Param("id") Long id);
}