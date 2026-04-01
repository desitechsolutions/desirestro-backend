package com.dts.restro.order.service;

import com.dts.restro.order.entity.Party;
import com.dts.restro.order.entity.RestaurantTable;
import com.dts.restro.order.repository.PartyRepository;
import com.dts.restro.order.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PartyService {

    private final PartyRepository partyRepository;
    private final RestaurantTableRepository tableRepository;

    public PartyService(PartyRepository partyRepository, RestaurantTableRepository tableRepository) {
        this.partyRepository = partyRepository;
        this.tableRepository = tableRepository;
    }

    public Party createParty(Long tableId, int occupiedSeats) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("Table not found"));

        // Get current occupied seats (safe null handling)
        int currentOccupied = table.getOccupiedSeats() != 0 ? table.getOccupiedSeats() : 0;

        if (currentOccupied + occupiedSeats > table.getCapacity()) {
            throw new RuntimeException("Not enough seats available. " +
                    "Current: " + currentOccupied + ", Requested: " + occupiedSeats +
                    ", Capacity: " + table.getCapacity());
        }

        Party party = new Party();
        party.setTable(table);
        party.setOccupiedSeats(occupiedSeats);
        party = partyRepository.save(party);

        // Add party to table's list
        table.getParties().add(party);

        // UPDATE OCCUPIED SEATS
        table.setOccupiedSeats(currentOccupied + occupiedSeats);

        // UPDATE TABLE STATUS
        if (table.getOccupiedSeats() == table.getCapacity()) {
            table.setStatus("FULL");
        } else if (table.getOccupiedSeats() > 0) {
            table.setStatus("PARTIALLY_OCCUPIED");
        } else {
            table.setStatus("EMPTY");
        }

        tableRepository.save(table);

        return party;
    }

    public List<Party> getPartiesByTable(Long tableId) {
        return partyRepository.findByTableIdAndStatus(tableId, "ACTIVE");
    }

    public List<Party> getAllActiveParties() {
        return partyRepository.findByStatus("ACTIVE");
    }

    public Party getPartyById(Long partyId) {
        return partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));
    }
}