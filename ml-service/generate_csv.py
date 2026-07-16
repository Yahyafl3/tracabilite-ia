"""Génère le CSV synthétique sans dépendances ML (stdlib uniquement)."""
from __future__ import annotations

import csv
import random
from pathlib import Path

SECTORS = ["SERVICES", "INDUSTRIE", "COMMERCE", "TECH", "AGRICULTURE"]
OUTPUT = Path(__file__).resolve().parent / "data" / "synthetic_credit_dataset.csv"
N_SAMPLES = 5000
RANDOM_STATE = 42


def sector_bonus(sector: str) -> float:
    return {
        "TECH": 0.20,
        "AGRICULTURE": -0.15,
        "COMMERCE": 0.05,
    }.get(sector, 0.0)


def main() -> None:
    random.seed(RANDOM_STATE)
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)

    rows = []
    for _ in range(N_SAMPLES):
        sector = random.choice(SECTORS)
        monthly_income = random.randint(4000, 35000)
        amount = monthly_income * random.uniform(0.5, 4.0)
        company_age = random.randint(0, 24)
        payment_incidents = random.randint(0, 5)
        debt_ratio = max(0.05, min(0.95, random.gauss(0.25, 0.15)))
        score = (
            (monthly_income / max(amount, 1.0)) * 2.0
            + company_age * 0.05
            - payment_incidents * 0.45
            - debt_ratio * 2.5
            + sector_bonus(sector)
            + random.gauss(0, 0.15)
        )
        approved = 1 if score > 0.55 else 0
        rows.append(
            {
                "amount": round(amount, 2),
                "monthlyIncome": monthly_income,
                "companyAgeYears": company_age,
                "paymentIncidents": payment_incidents,
                "debtRatio": round(debt_ratio, 4),
                "sector": sector,
                "approved": approved,
                "dataset_source": "synthetic",
            }
        )

    with OUTPUT.open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=rows[0].keys())
        writer.writeheader()
        writer.writerows(rows)

    print(f"Dataset synthétique généré: {OUTPUT} ({len(rows)} lignes)")


if __name__ == "__main__":
    main()
