# Uso, prioridades y expectativas sobre la IA

Análisis en Power BI de encuestas a adultos de Estados Unidos. Los datos se preparan con Python y pandas.

## Abrir en Power BI Desktop

1. Clona el repositorio o descarga y extrae su ZIP. En ambos casos, entra en la carpeta `PowerBL`.
2. Abre `Uso de IA.pbip` con una versión de Power BI Desktop compatible con proyectos PBIP y el formato de informe PBIR. Conserva las carpetas `Uso de IA.Report` y `Uso de IA.SemanticModel` junto al archivo del proyecto.
3. Si Power BI muestra el aviso de que el modelo aún no tiene datos cargados, pulsa **Actualizar ahora**. Si los gráficos quedan vacíos, pulsa **Inicio → Actualizar**. El modelo incluye los datos y se puede cargar sin Internet.

[Documentación de proyectos de Power BI](https://learn.microsoft.com/es-es/power-bi/developer/projects/projects-overview)

## Resultados

- **Uso personal.** 669 de 2.015 encuestados seleccionaron ChatGPT y 642 seleccionaron Google Gemini. Con los pesos de la encuesta, las estimaciones son 32,57 % y 31,09 %. Para el trabajo, las cifras son 252 y 204 en una base de 1.291 adultos con empleo. Cada persona podía indicar varios servicios. [Epoch AI e Ipsos, octubre de 2026](https://epoch.ai/data/polling/2026-10)
- **Prioridades declaradas.** El 76 % considera muy o extremadamente importante poder distinguir el contenido generado por IA del creado por personas. El 61 % quiere más control sobre el uso de IA en su vida. Son respuestas a dos preguntas distintas. [Transparencia](https://www.pewresearch.org/science/2025/09/17/how-americans-view-ai-and-its-impact-on-people-and-society/) · [Control](https://www.pewresearch.org/science/2025/09/17/ai-in-americans-lives-awareness-experiences-and-attitudes/)
- **Expectativas a 20 años.** Para la sociedad, un 40 % espera un efecto negativo y un 16 % uno positivo, una diferencia de 24 puntos porcentuales. Para la vida personal, el 31 % espera un efecto negativo y el 23 % uno positivo, una diferencia de 8 puntos porcentuales. Las dos diferencias son descriptivas. [Pew Research Center, febrero de 2026](https://www.pewresearch.org/chart/americans-predict-ais-impact-on-society-them-will-be-more-negative-than-positive/)

## Datos y reproducción

Abre una terminal en `PowerBL`. Se requiere Python con pandas.

```powershell
python -m pip install pandas
python python/prepare_usage.py --salida datos/usage.csv
python python/prepare_attitudes.py --salida datos/attitudes.csv
```

**`datos/usage.csv`** contiene siete herramientas con nombre propio, en los ámbitos personal y laboral. Cada conteo corresponde a quienes seleccionaron la opción. La base incluye a quienes recibieron la pregunta, incluso si la dejaron sin responder. La estimación divide la suma de los pesos de quienes seleccionaron la herramienta por la suma de los pesos de la base. El script contrasta conteos, bases y estimaciones con los resultados oficiales e incorpora sus intervalos de confianza del 90 %. Porcentajes e intervalos están en escala de 0 a 100. Para trabajar sin conexión después de descargar los archivos, indica las rutas mediante `--microdatos` y `--resultados-oficiales`. [Microdatos de Ipsos](https://epoch.ai/data/polling/polling_on_ai_usage_oct_2026_microdata.csv) · [Diccionario](https://epoch.ai/data/polling/polling_on_ai_usage_oct_2026_microdata_codebook.csv) · [Resultados oficiales](https://epoch.ai/data/polling/polling_on_ai_usage_oct_2026.csv)

**`datos/attitudes.csv`** contiene estimaciones ponderadas y redondeadas que Pew publicó para junio de 2025 (prioridades, 5.023 entrevistados) y febrero de 2026 (expectativas, 5.119 entrevistados). `fecha` indica el último día de campo y `url` apunta a la publicación de cada fila. La comparación usa resultados agregados de encuestas distintas.

Los scripts regeneran los CSV. El informe usa tablas integradas en el modelo semántico. Para incorporar datos nuevos al informe, actualiza las tablas `Uso`, `Prioridades` y `Expectativas` que correspondan en el modelo semántico y pulsa **Inicio → Actualizar** en Power BI Desktop.
