CREATE TABLE IF NOT EXISTS energosbyt.fact_invoice
(
    realty_id  UInt64,  -- Идентификатор объекта
    user_id    UInt64,  -- Идентификатор собственника
    tariff_id  UInt64,  -- Идентификатор тарифа
    gauge_id   UInt64,  -- Идентификатор счётчика
    day_kw     UInt32,  -- Потреблённые кВт·ч дневного тарифа
    night_kw   UInt32,  -- Потреблённые кВт·ч ночного тарифа
    price      UInt64,  -- Сумма счёта, копейки
    deadline   Date,    -- Срок оплаты
    created_at DateTime -- Дата выставления счёта
) ENGINE = MergeTree()
ORDER BY (realty_id, created_at);