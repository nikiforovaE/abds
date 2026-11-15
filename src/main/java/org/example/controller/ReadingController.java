package org.example.controller;

import org.example.repository.ClickHouseRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
public class ReadingController {

    private final ClickHouseRepository repository;

    public ReadingController(ClickHouseRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/realty/{id}/readings")
    public ResponseEntity<?> postReading(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"user_id\" field"));
        }

        if (!(body.get("user_id") instanceof Number userNum) || userNum.longValue() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"user_id\" field"));
        }
        if (!(body.get("gauge_id") instanceof Number gaugeNum) || gaugeNum.longValue() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"gauge_id\" field"));
        }
        if (!(body.get("tariff_id") instanceof Number tariffNum) || tariffNum.longValue() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"tariff_id\" field"));
        }
        if (!(body.get("reading_date") instanceof String readingDate)) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"reading_date\" field"));
        }
        try {
            LocalDate.parse(readingDate);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"reading_date\" field"));
        }
        if (!(body.get("day_kw") instanceof Number dayNum) || dayNum.intValue() < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"day_kw\" field"));
        }
        if (!(body.get("night_kw") instanceof Number nightNum) || nightNum.intValue() < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"night_kw\" field"));
        }

        long userId = userNum.longValue();
        long gaugeId = gaugeNum.longValue();
        long tariffId = tariffNum.longValue();

        if (!repository.realtyExists(id)
                || !repository.userExists(userId)
                || !repository.gaugeExists(gaugeId)
                || !repository.tariffExists(tariffId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }

        repository.insertReading(id, userId, tariffId, gaugeId, readingDate, dayNum.intValue(), nightNum.intValue());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/realty/{id}/readings")
    public ResponseEntity<?> getReadings(
            @PathVariable Long id,
            @RequestParam(required = false) String date_from,
            @RequestParam(required = false) String date_to) {

        LocalDate from;
        LocalDate to;
        try {
            if (date_from == null) throw new IllegalArgumentException();
            from = LocalDate.parse(date_from);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"date_from\" parameter"));
        }

        try {
            if (date_to == null) throw new IllegalArgumentException();
            to = LocalDate.parse(date_to);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"date_to\" parameter"));
        }

        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"date_from\" parameter"));
        }

        if (!repository.realtyExists(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }

        List<Map<String, Object>> readings = repository.getReadings(id, date_from, date_to);
        return ResponseEntity.ok(Map.of(
                "readings", readings,
                "count", readings.size()
        ));
    }
}