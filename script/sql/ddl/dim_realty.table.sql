CREATE TABLE IF NOT EXISTS energosbyt.dim_realty
(
    id         UInt64,  -- Snowflake ID
    address    String,  -- Адрес полностью
    square     UInt32,  -- Площадь, м²
    type       String,  -- flat — квартира, nrcp — нежилое помещение
    number     String,  -- Номер квартиры (для нежилых помещений пусто)
    rooms      UInt8,   -- Количество комнат
    floor      UInt8,   -- Этаж
    porch      UInt8,   -- Подъезд
    created_at DateTime -- Дата создания записи
) ENGINE = MergeTree()
ORDER BY id;