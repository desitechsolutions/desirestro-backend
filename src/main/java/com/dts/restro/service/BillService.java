package com.dts.restro.service;

import com.dts.restro.entity.Bill;
import com.dts.restro.entity.KOT;
import com.dts.restro.entity.Party;
import com.dts.restro.entity.RestaurantTable;
import com.dts.restro.exception.ResourceNotFoundException;
import com.dts.restro.repository.BillRepository;
import com.dts.restro.repository.KOTRepository;
import com.dts.restro.repository.PartyRepository;
import com.dts.restro.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BillService {

    private static final double GST_RATE = 0.18;

    private final BillRepository billRepository;
    private final PartyRepository partyRepository;
    private final KOTRepository kotRepository;
    private final RestaurantTableRepository tableRepository;

    public BillService(BillRepository billRepository, PartyRepository partyRepository, KOTRepository kotRepository, RestaurantTableRepository tableRepository) {
        this.billRepository = billRepository;
        this.partyRepository = partyRepository;
        this.kotRepository = kotRepository;
        this.tableRepository = tableRepository;
    }

    public Bill settleParty(Long partyId, String paymentMode) {
        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new RuntimeException("Party not found"));

        List<KOT> kots = kotRepository.findByPartyId(partyId);

        double subtotal = kots.stream()
                .flatMap(kot -> kot.getItems().stream())
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        double gst = subtotal * GST_RATE;
        double total = subtotal + gst;

        Bill bill = new Bill();
        bill.setParty(party);
        bill.setSubtotal(subtotal);
        bill.setGst(gst);
        bill.setTotal(total);
        bill.setPaymentMode(paymentMode);
        bill.setPaidAt(LocalDateTime.now());

        bill = billRepository.save(bill);

        party.setStatus("PAID");
        partyRepository.save(party);

        RestaurantTable table = party.getTable();

        // CRITICAL FIX: Subtract the party's seats from occupiedSeats
        int currentOccupied = table.getOccupiedSeats() != 0 ? table.getOccupiedSeats() : 0;
        int newOccupied = currentOccupied - party.getOccupiedSeats();
        table.setOccupiedSeats(Math.max(0, newOccupied)); // Prevent negative

        // Remove party from list
        table.getParties().remove(party);

        // Update status based on remaining occupied seats
        if (table.getOccupiedSeats() == 0) {
            table.setStatus("EMPTY");
        } else if (table.getOccupiedSeats() < table.getCapacity()) {
            table.setStatus("PARTIALLY_OCCUPIED");
        } else {
            table.setStatus("FULL");
        }

        tableRepository.save(table);

        return bill;
    }

    public Bill getBillById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    public Bill getBillByParty(Long partyId) {
        return billRepository.findByPartyId(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found for party: " + partyId));
    }

    public List<Bill> getAllBills() {
        return billRepository.findAllByOrderByPaidAtDesc();
    }
}