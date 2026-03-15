package com.dts.restro.controller;

import com.dts.restro.entity.Bill;
import com.dts.restro.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "http://localhost:3000")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping
    public List<Bill> getAllBills() {
        return billService.getAllBills();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bill> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    @GetMapping("/party/{partyId}")
    public ResponseEntity<Bill> getBillByParty(@PathVariable Long partyId) {
        return ResponseEntity.ok(billService.getBillByParty(partyId));
    }

    @PatchMapping("/party/{partyId}")
    public ResponseEntity<Bill> settleParty(@PathVariable Long partyId, @RequestBody SettleRequest request) {
        Bill bill = billService.settleParty(partyId, request.paymentMode());
        return ResponseEntity.ok(bill);
    }
}

record SettleRequest(String paymentMode) {}
