CREATE TABLE IF NOT EXISTS energosbyt.fact_meter_readings
(
    realty_id    UInt64,  -- Идентификатор объекта
    user_id      UInt64,  -- Идентификатор собственника
    tariff_id    UInt64,  -- Идентификатор тарифа
    gauge_id     UInt64,  -- Идентификатор счётчика
    reading_date Date,    -- Дата показания
    day_kw       UInt32,  -- Показание дневного счётчика, кВт·ч
    night_kw     UInt32,  -- Показание ночного счётчика, кВт·ч
    created_at   DateTime -- Время передачи показания, побеждает последняя запись
) ENGINE = ReplacingMergeTree(created_at)
ORDER BY (realty_id, reading_date);