package com.dts.restro.repository;

import com.dts.restro.entity.Party;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartyRepository extends JpaRepository<Party, Long> {
    List<Party> findByTableIdAndStatus(Long tableId, String status);
}