CREATE TABLE IF NOT EXISTS energosbyt.fact_gauge_bindings
(
    gauge_id   UInt64,   -- Идентификатор счётчика
    realty_id  UInt64,   -- Идентификатор объекта недвижимости
    created_at DateTime, -- Дата создания привязки
    sign       Int8,     -- 1 — установка, -1 — аннулирование
    version    UInt64    -- Версия привязки
) ENGINE = VersionedCollapsingMergeTree(sign, version)
ORDER BY (realty_id, gauge_id, version);