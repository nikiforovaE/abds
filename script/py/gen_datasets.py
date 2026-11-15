#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import csv
import random
from datetime import datetime, timedelta
from pathlib import Path

SEED = 42
DATA_DIR = Path(__file__).resolve().parents[2] / "data"
OPEN_VALID_TO = datetime(2099, 1, 1, 0, 0, 0)
NOW = datetime(2026, 10, 31)

START_DATE = datetime(2023, 11, 1)
END_DATE = datetime(2026, 10, 31)

# Версии тарифов (SCD 2)
STEPS = [datetime(2023, 11, 1), datetime(2025, 1, 1), datetime(2026, 7, 1)]
TARIFFS = {
    1: ("День–Ночь", 500, 620),
    2: ("Базовый", 600, 720),
}

GAUGE_MODELS = [
    ("Инкотекс Меркурий 230", "Россия"),
    ("Тайпит НЕВА МТ 324", "Россия"),
    ("Tuya ZMAi-90", "China"),
]


def build_realties():
    rows = []
    streets = [
        "г. Москва, улица Пушкина, дом Колотушкина",
        "г. Москва, проспект Мира, дом Кефира",
        "г. Москва, улица Есенина, дом Каруселина"
    ]

    rid = 1
    # 50 нежилых помещений (nrcp)
    for _ in range(50):
        rows.append({
            "id": rid,
            "address": random.choice(streets),
            "square": random.randint(80, 200),
            "type": "nrcp",
            "number": "",
            "floor": 1,
            "porch": 1,
            "commercial": True
        })
        rid += 1

    # 950 квартир (flat)
    while rid <= 1000:
        porch = random.randint(1, 6)
        floor = random.randint(2, 22)
        flat_idx = random.randint(1, 8)
        rows.append({
            "id": rid,
            "address": random.choice(streets),
            "square": random.randint(35, 95),
            "type": "flat",
            "number": f"{porch}{floor:02d}{flat_idx}",
            "floor": floor,
            "porch": porch,
            "commercial": False
        })
        rid += 1

    return rows


SURNAMES = [
    "Смирнов", "Иванов", "Кузнецов", "Соколов", "Попов", "Лебедев",
    "Козлов", "Новиков", "Морозов", "Волков", "Петров", "Егоров",
    "Павлов", "Семёнов", "Голубев", "Виноградов", "Богданов", "Воробьёв",
    "Фёдоров", "Михайлов", "Беляев", "Тарасов", "Белов", "Комаров",
    "Орлов", "Киселёв", "Макаров", "Андреев", "Ковалёв", "Ильин",
]
MALE_NAMES = ["Александр", "Сергей", "Дмитрий", "Андрей", "Максим", "Иван", "Артём", "Николай"]
FEMALE_NAMES = ["Елена", "Ольга", "Мария", "Наталья", "Ирина", "Татьяна", "Светлана", "Анна"]
MALE_PATRONYMICS = ["Александрович", "Сергеевич", "Дмитриевич", "Андреевич", "Иванович"]
FEMALE_PATRONYMICS = ["Александровна", "Сергеевна", "Дмитриевна", "Андреевна", "Ивановна"]


def fmt(dt: datetime) -> str:
    return dt.strftime("%Y-%m-%d %H:%M:%S")


def gen_phone(used: set) -> str:
    while True:
        phone = "+79" + "".join(random.choices("0123456789", k=9))
        if phone not in used:
            used.add(phone)
            return phone


def step_index(dt: datetime) -> int:
    index = 0
    for i, start in enumerate(STEPS):
        if start <= dt:
            index = i
    return index


def tariff_price(tariff_id: int, index: int):
    _, d0, d1 = TARIFFS[tariff_id]
    day = round(d0 + (d1 - d0) * index / max(1, len(STEPS) - 1))
    night = round(day / 2) if tariff_id == 1 else 0
    return day, night


def build_tariffs():
    rows = []
    for tariff_id, (name, _, _) in TARIFFS.items():
        for i, start in enumerate(STEPS):
            end = STEPS[i + 1] if i + 1 < len(STEPS) else OPEN_VALID_TO
            day, night = tariff_price(tariff_id, i)
            rows.append([tariff_id, name, day, night, fmt(start), fmt(end), fmt(start), fmt(start)])
    return rows


def consumption(square: int, commercial: bool, vacant: bool):
    base = square * (6.0 if commercial else 3.5)
    total = base * random.uniform(0.7, 1.3) * (0.15 if vacant else 1.0)
    return round(total * 0.6 / 30), round(total * 0.4 / 30)


def write_csv(name: str, header, rows):
    path = DATA_DIR / name
    with open(path, "w", encoding="utf-8", newline="") as f:
        out = csv.writer(f, lineterminator="\n")
        out.writerow(header)
        out.writerows(rows)
    print(f"Сгенерировано: {name} ({len(rows)} строк)")


def main() -> None:
    random.seed(SEED)
    DATA_DIR.mkdir(parents=True, exist_ok=True)

    # 1. dim_users (1000 строк, SCD 1 & SCD 3)
    users_rows = []
    used_phones = set()
    used_fio = set()
    for uid in range(1, 1001):
        while True:
            female = random.random() < 0.5
            surname = random.choice(SURNAMES)
            fio = " ".join([
                surname + "а" if female else surname,
                random.choice(FEMALE_NAMES if female else MALE_NAMES),
                random.choice(FEMALE_PATRONYMICS if female else MALE_PATRONYMICS),
            ])
            if fio not in used_fio:
                used_fio.add(fio)
                break

        created_at = datetime(2023, 10, 1, 10, 0, 0) + timedelta(seconds=uid)
        phone = gen_phone(used_phones)
        phone_prev = ""
        updated_at = created_at

        if random.random() < 0.3:
            change_at = datetime(2024, random.randint(1, 12), random.randint(1, 28), 10, 0, 0) + timedelta(seconds=uid)
            phone_prev = phone
            phone = gen_phone(used_phones)
            updated_at = change_at

        users_rows.append([uid, fio, phone, phone_prev, fmt(created_at), fmt(updated_at)])
    write_csv("dim_users.dataset.csv", ["id", "fio", "phone", "phone_prev", "created_at", "updated_at"],
              users_rows)
    user_ids = [r[0] for r in users_rows]

    # 2. dim_realty (1000 строк)
    realties = build_realties()
    realty_rows = [
        [r["id"], r["address"], r["square"], r["type"], r["number"], r["floor"], r["porch"], "2023-10-01 10:00:00"]
        for r in realties
    ]
    write_csv("dim_realty.dataset.csv", ["id", "address", "square", "type", "number", "floor", "porch", "created_at"],
              realty_rows)

    # 3. dim_tariffs (SCD 2)
    write_csv("dim_tariffs.dataset.csv",
              ["id", "name", "price_day_kwph", "price_night_kwph", "valid_from", "valid_to", "created_at",
               "updated_at"], build_tariffs())

    # 4. Счетчики, привязки, показания по дням и счета по месяцам
    gauges = []
    bindings = []
    readings = []
    invoices = []
    next_gauge_id = 1001

    days_list = []
    curr = START_DATE
    while curr <= END_DATE:
        days_list.append(curr)
        curr += timedelta(days=1)

    for idx, r in enumerate(realties):
        rid = r["id"]
        owner = user_ids[idx % len(user_ids)]
        tariff_id = 1 if r["commercial"] or random.random() < 0.8 else 2

        model, country = random.choice(GAUGE_MODELS)
        gauges.append([rid, model, country, "2023-10-01 00:00:00"])
        bindings.append([rid, rid, "2023-10-01 00:00:00", 1, 1])

        version = 1
        replacement = None
        if random.random() < 0.12:
            change = datetime(2024, random.randint(6, 11), random.randint(1, 28), 10, 0, 0)
            new_gid = next_gauge_id
            next_gauge_id += 1
            model2, country2 = random.choice(GAUGE_MODELS)
            gauges.append([new_gid, model2, country2, fmt(change)])
            bindings.append([rid, rid, fmt(change), -1, version])
            version += 1
            bindings.append([new_gid, rid, fmt(change), 1, version])
            replacement = (change, new_gid)

        day_cum = random.randint(2000, 6000)
        night_cum = random.randint(1000, 3000)

        monthly_day = 0
        monthly_night = 0
        current_month = START_DATE.month

        for d in days_list:
            gauge_id = replacement[1] if replacement and d >= replacement[0] else rid
            vacant = random.random() < 0.08
            d_c, n_c = consumption(r["square"], r["commercial"], vacant)
            day_cum += d_c
            night_cum += n_c
            monthly_day += d_c
            monthly_night += n_c

            reading_date = d.strftime("%Y-%m-%d")

            if random.random() < 0.03:
                wrong = d.replace(hour=10, minute=0, second=0)
                fixed = d.replace(hour=18, minute=0, second=0)
                readings.append([rid, owner, tariff_id, gauge_id, reading_date, day_cum + 40, night_cum, fmt(wrong)])
                readings.append([rid, owner, tariff_id, gauge_id, reading_date, day_cum, night_cum, fmt(fixed)])
            else:
                created = d.replace(hour=14, minute=30, second=0)
                readings.append([rid, owner, tariff_id, gauge_id, reading_date, day_cum, night_cum, fmt(created)])

            if d.month != current_month or d == END_DATE:
                prev_d = d - timedelta(days=1)
                y, m = prev_d.year, prev_d.month
                issued = datetime(y, m, 27, 10, 0, 0)
                ny, nm = (y + 1, 1) if m == 12 else (y, m + 1)
                deadline = datetime(ny, nm, 10).strftime("%Y-%m-%d")

                index = step_index(issued)
                day_price, night_price = tariff_price(tariff_id, index)
                price = monthly_day * day_price + monthly_night * night_price

                invoices.append(
                    [rid, owner, tariff_id, gauge_id, monthly_day, monthly_night, price, deadline, fmt(issued)])

                monthly_day = 0
                monthly_night = 0
                current_month = d.month

    write_csv("dim_gauges.dataset.csv", ["id", "name", "country", "created_at"], gauges)
    write_csv("fact_gauge_bindings.dataset.csv", ["gauge_id", "realty_id", "created_at", "sign", "version"], bindings)
    write_csv("fact_meter_readings.dataset.csv",
              ["realty_id", "user_id", "tariff_id", "gauge_id", "reading_date", "day_kw", "night_kw", "created_at"],
              readings)
    write_csv("fact_invoice.dataset.csv",
              ["realty_id", "user_id", "tariff_id", "gauge_id", "day_kw", "night_kw", "price", "deadline",
               "created_at"], invoices)
    print("Генерация всех датасетов успешно завершена!")


if __name__ == "__main__":
    main()
