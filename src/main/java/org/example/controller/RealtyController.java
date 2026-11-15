package org.example.controller;

import org.example.repository.ClickHouseRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class RealtyController {

    private final ClickHouseRepository repository;

    public RealtyController(ClickHouseRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/realty/{id}")
    public ResponseEntity<?> getRealty(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(repository.getRealty(id));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }
    }

    @PostMapping("/realty/{id}/gauges")
    public ResponseEntity<?> postGaugeBinding(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        if (body == null || !(body.get("gauge_id") instanceof Number gaugeNum)) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"gauge_id\" field"));
        }
        long gaugeId = gaugeNum.longValue();
        if (gaugeId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"gauge_id\" field"));
        }

        if (!repository.realtyExists(id) || !repository.gaugeExists(gaugeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }

        long currentVersion = 0L;
        Long activeGaugeId = null;
        try {
            Map<String, Object> active = repository.getActiveGaugeBinding(id);
            currentVersion = ((Number) active.get("version")).longValue();
            activeGaugeId = ((Number) active.get("gauge_id")).longValue();
        } catch (EmptyResultDataAccessException ignored) {
        }

        if (activeGaugeId != null) {
            repository.insertGaugeBinding(activeGaugeId, id, -1, currentVersion);
        }

        repository.insertGaugeBinding(gaugeId, id, 1, currentVersion + 1);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}