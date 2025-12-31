package com.dts.restro.controller;

import com.dts.restro.entity.Party;
import com.dts.restro.service.PartyService;
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
        return partyService.getPartiesByTable(tableId); // You'll need this in service
    }
}

record CreatePartyRequest(int occupiedSeats) {}