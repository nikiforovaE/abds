CREATE TABLE IF NOT EXISTS energosbyt.dim_tariffs
(
    id               UInt64,   -- Snowflake ID
    name             String,   -- Название тарифа
    price_day_kwph   UInt32,   -- Цена кВт·ч в дневное время, копейки (SCD 2)
    price_night_kwph UInt32,   -- Цена кВт·ч в ночное время, копейки (SCD 2)
    valid_from       DateTime, -- Начало действия версии тарифа
    valid_to         DateTime, -- Конец действия версии тарифа (2099-01-01 00:00:00 у открытой версии)
    created_at       DateTime, -- Дата создания записи
    updated_at       DateTime  -- Версия строки для ReplacingMergeTree
) ENGINE = ReplacingMergeTree(updated_at)
ORDER BY (id, valid_from);