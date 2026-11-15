-- Загрузка data/dim_users.dataset.csv в dim_users. Данные приходят через stdin.
-- person_no из CSV становится id.
INSERT INTO energosbyt.dim_users (id, fio, phone, phone_prev, created_at, updated_at)
SELECT person_no, fio, phone, phone_prev, created_at, updated_at
FROM input('person_no UInt64, fio String, phone String, phone_prev String, created_at DateTime, updated_at DateTime') FORMAT CSVWithNames;