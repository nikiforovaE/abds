package org.example.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController {

    private final JdbcTemplate jdbcTemplate;

    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {
        String sql = "SELECT id, fio, phone, phone_prev FROM dim_users FINAL WHERE id = ? LIMIT 1";
        try {
            Map<String, Object> user = jdbcTemplate.queryForMap(sql, id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newFio = body.get("fio");
        String newPhone = body.get("phone");

        if (newFio == null && newPhone == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "invalid \"fio\" field"));
        }

        String checkSql = "SELECT id, fio, phone, phone_prev, created_at FROM dim_users FINAL WHERE id = ? LIMIT 1";
        try {
            Map<String, Object> existing = jdbcTemplate.queryForMap(checkSql, id);
            String currentFio = (String) existing.get("fio");
            String currentPhone = (String) existing.get("phone");

            String fioToSave = (newFio != null) ? newFio : currentFio;
            String phoneToSave = currentPhone;
            String phonePrevToSave = (String) existing.get("phone_prev");
            if (phonePrevToSave == null) phonePrevToSave = "";

            if (newPhone != null) {
                if (!newPhone.equals(currentPhone)) {
                    phonePrevToSave = currentPhone;
                    phoneToSave = newPhone;
                }
            }

            String insertSql = "INSERT INTO dim_users (id, fio, phone, phone_prev, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, now())";
            jdbcTemplate.update(insertSql, id, fioToSave, phoneToSave, phonePrevToSave, existing.get("created_at"));

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Not found"));
        }
    }
}