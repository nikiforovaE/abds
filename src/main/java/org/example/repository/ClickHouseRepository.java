package org.example.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class ClickHouseRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClickHouseRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean realtyExists(Long realtyId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count() FROM dim_realty WHERE id = ?", Integer.class, realtyId);
        return count != null && count > 0;
    }

    public boolean userExists(Long userId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count() FROM dim_users FINAL WHERE id = ?", Integer.class, userId);
        return count != null && count > 0;
    }

    public boolean gaugeExists(Long gaugeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count() FROM dim_gauges WHERE id = ?", Integer.class, gaugeId);
        return count != null && count > 0;
    }

    public boolean tariffExists(Long tariffId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count() FROM dim_tariffs FINAL WHERE id = ?", Integer.class, tariffId);
        return count != null && count > 0;
    }

    public Map<String, Object> getUser(Long id) {
        String sql = "SELECT id, fio, phone, phone_prev FROM dim_users FINAL WHERE id = ? LIMIT 1";
        return jdbcTemplate.queryForMap(sql, id);
    }

    public Map<String, Object> getUserForUpdate(Long id) {
        String sql = "SELECT id, fio, phone, phone_prev, " +
                "formatDateTime(created_at, '%Y-%m-%d %H:%i:%S') AS created_at " +
                "FROM dim_users FINAL WHERE id = ? LIMIT 1";
        return jdbcTemplate.queryForMap(sql, id);
    }

    public void insertUserVersion(Long id, String fio, String phone, String phonePrev, String createdAt) {
        String sql = "INSERT INTO dim_users (id, fio, phone, phone_prev, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, now())";
        jdbcTemplate.update(sql, id, fio, phone, phonePrev, createdAt);
    }

    public Map<String, Object> getRealty(Long id) {
        String sql = "SELECT id, address, square, type, number, rooms, floor, porch " +
                "FROM dim_realty WHERE id = ? LIMIT 1";
        return jdbcTemplate.queryForMap(sql, id);
    }

    public Map<String, Object> getActiveGaugeBinding(Long realtyId) {
        String sql = "SELECT gauge_id, version, sum(sign) AS s " +
                "FROM fact_gauge_bindings FINAL " +
                "WHERE realty_id = ? GROUP BY gauge_id, version HAVING s > 0 LIMIT 1";
        return jdbcTemplate.queryForMap(sql, realtyId);
    }

    public void insertGaugeBinding(Long gaugeId, Long realtyId, int sign, Long version) {
        String sql = "INSERT INTO fact_gauge_bindings (gauge_id, realty_id, created_at, sign, version) " +
                "VALUES (?, ?, now(), ?, ?)";
        jdbcTemplate.update(sql, gaugeId, realtyId, sign, version);
    }

    public void insertReading(Long realtyId, Long userId, Long tariffId, Long gaugeId,
                              String readingDate, Integer dayKw, Integer nightKw) {
        String sql = "INSERT INTO fact_meter_readings " +
                "(realty_id, user_id, tariff_id, gauge_id, reading_date, day_kw, night_kw, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, now())";
        jdbcTemplate.update(sql, realtyId, userId, tariffId, gaugeId, readingDate, dayKw, nightKw);
    }

    public List

    getReadings(Long realtyId, String dateFrom, String dateTo) {
        String sql = "SELECT toString(reading_date) AS reading_date, user_id, tariff_id, gauge_id, day_kw, night_kw " +
                "FROM fact_meter_readings FINAL " +
                "WHERE realty_id = ? AND reading_date >= ? AND reading_date <= ? " +
                "ORDER BY reading_date ASC";
        return jdbcTemplate.queryForList(sql, realtyId, dateFrom, dateTo);
    }

    public Long countInvoices(Long realtyId) {
        return jdbcTemplate.queryForObject(
                "SELECT count() FROM fact_invoice WHERE realty_id = ?", Long.class, realtyId);
    }

    public List<Map<String, Object>>

    getInvoices(Long realtyId, Integer limit, Integer offset) {
        String sql = "SELECT tariff_id, gauge_id, day_kw, night_kw, price, " +
                "toString(deadline) AS deadline, " +
                "formatDateTime(created_at, '%Y-%m-%d %H:%i:%S') AS created_at " +
                "FROM fact_invoice WHERE realty_id = ? " +
                "ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, realtyId, limit, offset);
    }

    public Map<String, Object> getTariffByDate(String dateTimeStr) {
        String sql = "SELECT id, name, price_day_kwph, price_night_kwph, " +
                "formatDateTime(valid_from, '%Y-%m-%d %H:%i:%S') AS valid_from, " +
                "formatDateTime(valid_to, '%Y-%m-%d %H:%i:%S') AS valid_to " +
                "FROM dim_tariffs FINAL " +
                "WHERE valid_from <= ? AND valid_to > ? LIMIT 1";
        return jdbcTemplate.queryForMap(sql, dateTimeStr, dateTimeStr);
    }
}