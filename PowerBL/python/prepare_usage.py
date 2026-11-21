"""Prepara el uso declarado de herramientas de IA a partir de Epoch AI / Ipsos."""

from argparse import ArgumentParser
from pathlib import Path

import pandas as pd


POLL_URL = "https://epoch.ai/data/polling/2026-10"
FILE_URL = "https://epoch.ai/data/polling/polling_on_ai_usage_oct_2026"
WEIGHT_COLUMN = "weight_PID_Vote_CDG"
SERVICES = [
    (1, "ChatGPT"),
    (2, "Claude"),
    (3, "Google Gemini"),
    (4, "Microsoft Copilot"),
    (5, "Grok"),
    (6, "Meta AI"),
    (7, "Perplexity"),
]
QUESTIONS = [
    ("Personal", "CDG2", "q2_personal_services"),
    ("Trabajo", "CDG4", "q4_work_services"),
]


def prepare_usage(microdata: pd.DataFrame, published: pd.DataFrame) -> pd.DataFrame:
    if microdata["respondent_id"].isna().any() or not microdata["respondent_id"].is_unique:
        raise ValueError("El identificador de cada participante debe ser único y estar presente.")
    weights = microdata[WEIGHT_COLUMN]
    if weights.isna().any() or not weights.gt(0).all():
        raise ValueError("Todos los participantes deben tener un peso positivo.")

    benchmarks = published.loc[published["demographic"].eq("full_sample")].set_index(
        ["question", "response"]
    )
    if not benchmarks.index.is_unique:
        raise ValueError("La publicación contiene resultados duplicados para la misma pregunta y opción.")
    records = []
    for scope, prefix, question in QUESTIONS:
        eligible = microdata[f"{prefix}_1"].notna()
        base = int(eligible.sum())
        weighted_base = weights.loc[eligible].sum()

        for suffix, name in SERVICES:
            answers = microdata[f"{prefix}_{suffix}"]
            if not answers.notna().equals(eligible) or not answers.loc[eligible].isin([-1, 0, 1]).all():
                raise ValueError(f"Códigos de respuesta inesperados para {scope} / {name}.")
            # La base incluye el código -1, que indica que la pregunta se hizo y quedó sin respuesta.
            reported_use = answers.eq(1)
            count = int(reported_use.sum())
            weighted_rate = 100 * weights.loc[reported_use].sum() / weighted_base
            benchmark = benchmarks.loc[(question, name)]
            if count != benchmark["n"] or base != benchmark["n_base"]:
                raise ValueError(f"Los conteos difieren de la publicación para {scope} / {name}.")
            if abs(weighted_rate - benchmark["pct"]) > 0.00501:
                raise ValueError(f"El porcentaje difiere de la publicación para {scope} / {name}.")

            records.append(
                {
                    "herramienta": name,
                    "ambito": scope,
                    "encuestados": count,
                    "base": base,
                    "porcentaje": round(weighted_rate, 2),
                    "ic90_inf": benchmark["ci_lo"],
                    "ic90_sup": benchmark["ci_hi"],
                }
            )

    output = pd.DataFrame(records)
    personal_ranking = (
        output.loc[output["ambito"].eq("Personal")]
        .sort_values("porcentaje", ascending=False)["herramienta"]
        .tolist()
    )
    order = {name: index for index, name in enumerate(personal_ranking, start=1)}
    output["orden"] = output["herramienta"].map(order)
    output = output.sort_values(["orden", "ambito"])
    return output[["herramienta", "ambito", "encuestados", "base", "porcentaje", "orden", "ic90_inf", "ic90_sup"]]


def main() -> None:
    parser = ArgumentParser(description=__doc__, epilog=f"Fuente y metodología: {POLL_URL}")
    parser.add_argument("--microdatos", default=f"{FILE_URL}_microdata.csv")
    parser.add_argument("--resultados-oficiales", default=f"{FILE_URL}.csv")
    parser.add_argument("--salida", type=Path, default=Path(__file__).with_name("usage.csv"))
    args = parser.parse_args()

    microdata = pd.read_csv(args.microdatos)
    published = pd.read_csv(args.resultados_oficiales, keep_default_na=False)
    usage = prepare_usage(microdata, published)
    usage.to_csv(args.salida, index=False, encoding="utf-8-sig")
    print(f"{len(usage)} filas verificadas con los resultados oficiales. {args.salida}")


if __name__ == "__main__":
    main()
