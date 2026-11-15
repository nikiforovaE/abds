package org.example.controller;

import org.example.repository.ClickHouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class InvoiceController {

    private final ClickHouseRepository repository;

    public InvoiceController(ClickHouseRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/realty/{id}/invoices")
    public ResponseEntity<?> getInvoices(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset) {

        if (limit < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"limit\" parameter"));
        }
        if (offset < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"offset\" parameter"));
        }

        if (!repository.realtyExists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }

        Long totalCount = repository.countInvoices(id);
        List<Map<String, Object>> invoices = repository.getInvoices(id, limit, offset);

        return ResponseEntity.ok(Map.of(
                "invoices", invoices,
                "count", totalCount != null ? totalCount : 0
        ));
    }
}