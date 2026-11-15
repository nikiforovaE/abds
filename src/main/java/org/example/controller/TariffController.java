package org.example.controller;

import org.example.repository.ClickHouseRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class TariffController {

    private final ClickHouseRepository repository;

    public TariffController(ClickHouseRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/tariffs")
    public ResponseEntity<?> getTariff(@RequestParam(required = false) String date) {
        if (date == null || date.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"date\" parameter"));
        }

        try {
            LocalDate.parse(date);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"date\" parameter"));
        }

        try {
            return ResponseEntity.ok(repository.getTariffByDate(date + " 00:00:00"));
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }
    }
}