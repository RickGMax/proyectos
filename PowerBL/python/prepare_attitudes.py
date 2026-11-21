"""Prepara estimaciones publicadas por Pew Research Center sobre IA.

Las encuestas de adultos de EE. UU. se realizaron del 9 al 15 de junio de
2025 y del 17 al 23 de febrero de 2026. La columna fecha registra el último
día de campo. La base es el tamaño publicado de cada muestra completa.
Los porcentajes están ponderados y redondeados en las fuentes. Las dos
prioridades provienen de preguntas distintas, sin un ranking de opciones.
"""

from argparse import ArgumentParser
from pathlib import Path

import pandas as pd


SOURCE = "Pew Research Center"
RECOGNITION_URL = (
    "https://www.pewresearch.org/science/2025/09/17/"
    "how-americans-view-ai-and-its-impact-on-people-and-society/"
)
CONTROL_URL = (
    "https://www.pewresearch.org/science/2025/09/17/"
    "ai-in-americans-lives-awareness-experiences-and-attitudes/"
)
EXPECTATIONS_URL = (
    "https://www.pewresearch.org/chart/"
    "americans-predict-ais-impact-on-society-them-will-be-more-negative-than-positive/"
)
COLUMNS = ["seccion", "indicador", "grupo", "porcentaje", "base", "fuente", "fecha", "url", "orden"]


def prepare_attitudes() -> pd.DataFrame:
    priorities = pd.DataFrame(
        [
            ("Distinguir contenido hecho con IA es muy o extremadamente importante", 76, RECOGNITION_URL, 1),
            ("Quieren más control sobre el uso de IA en sus vidas", 61, CONTROL_URL, 2),
        ],
        columns=["indicador", "porcentaje", "url", "orden"],
    ).assign(
        seccion="Prioridades",
        grupo="Adultos de EE. UU.",
        base=5023,
        fecha="2025-06-15",
    )

    responses = ["Negativo", "Positivo", "Mixto", "No sabe"]
    expectations = pd.DataFrame(
        [("Sociedad", 40, 16, 31, 13), ("Vida personal", 31, 23, 27, 19)],
        columns=["grupo", *responses],
    ).melt(id_vars="grupo", var_name="indicador", value_name="porcentaje")
    expectations = expectations.assign(
        seccion="Expectativas",
        base=5119,
        fecha="2026-02-23",
        url=EXPECTATIONS_URL,
        orden=expectations["indicador"].map({response: i for i, response in enumerate(responses, 1)}),
    )

    attitudes = pd.concat([priorities, expectations], ignore_index=True).assign(fuente=SOURCE)
    attitudes["base"] = attitudes["base"].astype("Int64")
    return attitudes[COLUMNS]


def compare_expectations(attitudes: pd.DataFrame) -> pd.DataFrame:
    by_group = attitudes.loc[attitudes["seccion"].eq("Expectativas")].pivot(
        index="grupo", columns="indicador", values="porcentaje"
    )
    comparison = by_group[["Negativo", "Positivo"]].copy()
    comparison["Negativo menos positivo (p.p.)"] = comparison["Negativo"] - comparison["Positivo"]
    return comparison


def main() -> None:
    parser = ArgumentParser(description=__doc__)
    parser.add_argument("--salida", type=Path, default=Path(__file__).with_name("attitudes.csv"))
    options = parser.parse_args()
    attitudes = prepare_attitudes()
    attitudes.to_csv(options.salida, index=False, encoding="utf-8-sig")
    print(compare_expectations(attitudes).to_string())


if __name__ == "__main__":
    main()
