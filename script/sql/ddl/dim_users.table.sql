CREATE TABLE IF NOT EXISTS energosbyt.dim_users
(
    id         UInt64,   -- Snowflake ID
    fio        String,   -- ФИО (SCD 1: перезаписывается)
    phone      String,   -- Текущий телефон (SCD 3)
    phone_prev String,   -- Предыдущий телефон, пусто если его не было (SCD 3)
    created_at DateTime, -- Дата создания записи
    updated_at DateTime  -- Версия строки для ReplacingMergeTree
) ENGINE = ReplacingMergeTree(updated_at)
ORDER BY id;