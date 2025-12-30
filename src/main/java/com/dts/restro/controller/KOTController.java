package com.dts.restro.controller;

import com.dts.restro.entity.KOT;
import com.dts.restro.entity.KOTItem;
import com.dts.restro.service.KOTService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kot")
@CrossOrigin(origins = "http://localhost:3000")
public class KOTController {

    private final KOTService kotService;

    public KOTController(KOTService kotService) {
        this.kotService = kotService;
    }

    @PostMapping
    public ResponseEntity<KOT> createKOT(
            @RequestBody CreateKOTRequest request) {
        KOT kot = kotService.createKOT(request.tableId(), request.items());
        return ResponseEntity.ok(kot);
    }

    @GetMapping("/active")
    public List<KOT> getActiveKOTs() {
        return kotService.getActiveKOTs();
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<KOT> markAsReady(@PathVariable Long id) {
        KOT kot = kotService.markAsReady(id);
        return ResponseEntity.ok(kot);
    }

    //Billing
    @GetMapping("/table/{tableId}")
    public List<KOT> getKOTsForTable(@PathVariable Long tableId) {
        return kotService.getKOTsForTable(tableId);
    }

    @PatchMapping("/table/{tableId}/settle")
    public ResponseEntity<String> settleTable(@PathVariable Long tableId) {
        kotService.settleTable(tableId);
        return ResponseEntity.ok("Table settled");
    }
}

// Record for request body
record CreateKOTRequest(Long tableId, List<KOTItem> items) {}