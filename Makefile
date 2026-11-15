.PHONY: run rund stop setup schema teardown load gen

# Запускает проект в фоновом режиме.
run:
	@echo "Запускаю проект в фоновом режиме..."
	docker compose --env-file .env.local up -d --build

# Запускает проект в режиме отладки с просмотром всех логов.
rund:
	@echo "Запускаю проект..."
	docker compose --env-file .env.local up --build

# Останавливает контейнеры.
stop:
	@echo "Останавливаю проект..."
	docker compose --env-file .env.local down

# Настройка и прогрев системы.
# Создаст схему БД, сгенерирует и загрузит все датасеты в таблицы.
setup: schema gen load

# Очистка системы
teardown: .drop-ch

# Создаёт схему базы данных.
schema:
	@echo "Создаём таблицы в ClickHouse..."
	@for f in script/sql/ddl/*.table.sql; do \
		echo "Выполняю $$f..."; \
		./script/sh/run_sql.sh "$$f" || exit 1; \
	done

.drop-ch:
	@echo "Удаляем таблицы в ClickHouse..."
	@./script/sh/run_sql.sh script/sql/dml/drop_all.sql || true

# Загружает датасеты во все таблицы
load:
	@echo "Загружаем датасеты в ClickHouse..."
	@for t in dim_users dim_realty dim_gauges dim_tariffs fact_gauge_bindings fact_meter_readings fact_invoice; do \
		echo "Загружаем $$t..."; \
		docker compose --env-file .env.local exec -i clickhouse clickhouse-client \
			--user "$$CLICKHOUSE_USER" \
			--database "$$CLICKHOUSE_DATABASE" \
			--query "INSERT INTO $$t FORMAT CSV" < data/$$t.dataset.csv || exit 1; \
	done

# Генерирует большие датасеты в data/.
gen:
	@echo "Генерируем датасеты в data/..."
	mkdir -p data
	# тут будет вызов генератора