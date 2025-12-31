package com.dts.restro.service;

import com.dts.restro.entity.*;
import com.dts.restro.repository.KOTRepository;
import com.dts.restro.repository.PartyRepository;
import com.dts.restro.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class KOTService {

    private final KOTRepository kotRepository;
    private final PartyRepository partyRepository;

    public KOTService(KOTRepository kotRepository, PartyRepository partyRepository) {
        this.kotRepository = kotRepository;
        this.partyRepository = partyRepository;
    }

    public KOT createKOT(Long tableId, List<KOTItem> items) {
        Party party = partyRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        KOT kot = new KOT();
        kot.setParty(party);
        kot.setItems(items);
        kot.setKotNumber(generateKotNumber());
        kot.setStatus("NEW");

        // Update table status
        /*table.setStatus("OCCUPIED");
        tableRepository.save(table);*/

        return kotRepository.save(kot);
    }

    public List<KOT> getActiveKOTs() {
        return kotRepository.findByStatusInOrderByCreatedAtAsc(List.of("NEW", "PREPARING"));
    }

    private String generateKotNumber() {
        LocalDate today = LocalDate.now();
        String prefix = "KOT-" + today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        int sequence = kotRepository.countByKotNumberStartingWith(prefix) + 1;

        return prefix + "-" + String.format("%04d", sequence); // KOT-20251230-0001
    }

    public KOT markAsReady(Long kotId) {
        KOT kot = kotRepository.findById(kotId)
                .orElseThrow(() -> new RuntimeException("KOT not found"));
        kot.setStatus("READY");
        return kotRepository.save(kot);
    }

    public List<KOT> getKOTsByParty(Long partyId) {
        return kotRepository.findByPartyId(partyId);
    }

    /*public List<KOT> getKOTsForTable(Long tableId) {
        return kotRepository.findByTableId(tableId);
    }*/

/*    public void settleTable(Long tableId) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));
        table.setStatus("EMPTY");
        tableRepository.save(table);
    }*/
}