package com.dts.restro.order.controller;

import com.dts.restro.order.dto.CreateKOTRequest;
import com.dts.restro.order.entity.KOT;
import com.dts.restro.inventory.service.InventoryService;
import com.dts.restro.order.service.KOTService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kot")
@CrossOrigin(origins = "http://localhost:3000")
public class KOTController {

    private final KOTService kotService;
    private final InventoryService inventoryService;

    public KOTController(KOTService kotService, InventoryService inventoryService) {
        this.kotService = kotService;
        this.inventoryService = inventoryService;
    }

    @PostMapping("/party/{partyId}")
    public ResponseEntity<KOT> createKOT(@PathVariable Long partyId, @RequestBody CreateKOTRequest request) {
        KOT kot = kotService.createKOT(partyId, request.getItems());
        return ResponseEntity.ok(kot);
    }

    @GetMapping("/active")
    public List<KOT> getActiveKOTs() {
        return kotService.getActiveKOTs();
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<KOT> markAsReady(@PathVariable Long id) {
        KOT kot = kotService.markAsReady(id);
        // AUTO-DEDUCT INVENTORY
        inventoryService.deductFromKOT(kot);
        return ResponseEntity.ok(kot);
    }
    @GetMapping("/party/{partyId}")
    public List<KOT> getKOTsByParty(@PathVariable Long partyId) {
        return kotService.getKOTsByParty(partyId);
    }

    @GetMapping("/ready")
    public List<KOT> getReadyKOTs() {
        return kotService.getReadyKOTs();
    }

    @PatchMapping("/{id}/served")
    public ResponseEntity<KOT> markAsServed(@PathVariable Long id) {
        KOT kot = kotService.markAsServed(id);
        return ResponseEntity.ok(kot);
    }
}