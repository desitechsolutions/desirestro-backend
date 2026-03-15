package com.dts.restro.order.controller;

import com.dts.restro.order.entity.Party;
import com.dts.restro.order.service.PartyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parties")
@CrossOrigin(origins = "http://localhost:3000")
public class PartyController {

    private final PartyService partyService;

    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }

    @PostMapping("/table/{tableId}")
    public Party createParty(@PathVariable Long tableId, @RequestBody CreatePartyRequest request) {
        return partyService.createParty(tableId, request.occupiedSeats());
    }
    @GetMapping("/table/{tableId}")
    public List<Party> getPartiesByTable(@PathVariable Long tableId) {
        return partyService.getPartiesByTable(tableId);
    }

    @GetMapping("/active")
    public List<Party> getAllActiveParties() {
        return partyService.getAllActiveParties();
    }

    @GetMapping("/{partyId}")
    public ResponseEntity<Party> getPartyById(@PathVariable Long partyId) {
        return ResponseEntity.ok(partyService.getPartyById(partyId));
    }
}

record CreatePartyRequest(int occupiedSeats) {}