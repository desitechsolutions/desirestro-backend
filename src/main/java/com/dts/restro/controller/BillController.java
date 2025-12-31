package com.dts.restro.controller;

import com.dts.restro.entity.Bill;
import com.dts.restro.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "http://localhost:3000")
public class BillController {

    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @PatchMapping("/party/{partyId}")
    public ResponseEntity<Bill> settleParty(@PathVariable Long partyId, @RequestBody SettleRequest request) {
        Bill bill = billService.settleParty(partyId, request.paymentMode());
        return ResponseEntity.ok(bill);
    }
}

record SettleRequest(String paymentMode) {}