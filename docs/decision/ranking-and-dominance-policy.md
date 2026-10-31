# Política de ranking, dominancia y no comparabilidad

**Estado:** Propuesta metodológica; pendiente de validación empírica. **Versión:** 0.1.0

## 1. Principio

El ranking ordena evidencia comparativa; no transforma por sí mismo una evaluación en orden de inversión. No existe una puntuación universal adecuada para todos los criptoactivos, tipologías, horizontes, usuarios y mercados.

Hasta disponer de fundamento, calibración y validación independientes, quedan deshabilitados score total compuesto, pesos subjetivos o iguales por defecto, normalizaciones escogidas para mejorar posiciones, umbrales arbitrarios, conversión de etiquetas cualitativas a números sin semántica validada y orden total cuando existan trade-offs no resueltos.

## 2. Filtros no compensables

Aplicar gates antes de comparar:
1. VETO_ACTIVE: excluido de candidatos favorables dentro del alcance.
2. VETO_REVIEW_REQUIRED: separado en revisión; no aparece como oportunidad favorable confirmada.
3. RISK_UNKNOWN material: evaluación incompleta o condicional.
4. Identidad no resuelta, datos críticos no admisibles o elegibilidad fallida: bloquea comparación dependiente.
5. Valoración no estimable: no clasificar por descuento cuantificado; puede conservarse en lista de investigación si la finalidad lo permite.

Ninguna ventaja en descuento, momentum, capitalización, actividad o narrativa compensa estos bloqueos.

## 3. Dominancia

La dominancia solo se afirma si los activos son comparables y la evidencia permite una comparación consistente en dimensiones materiales.

A domina a B si:
- A no es peor que B en todas las dimensiones materiales evaluadas con dirección y significado comparables;
- A es mejor que B en al menos una dimensión material;
- no existe dimensión material desconocida o no comparable que pueda invertir la relación;
- ninguno tiene gate/veto que invalide la comparación;
- la conclusión es robusta a incertidumbres y escenarios relevantes, o se declara explícitamente su alcance limitado.

No declarar dominancia desde métricas de unidades distintas sin regla validada. La dominancia puede ser parcial y no transitiva con incertidumbre; documentar relaciones por pares.

## 4. Ranking parcial y grupos

Cuando no exista orden total, producir grupos de estado (bloqueados, revisión requerida, incompletos, no comparables, candidatos condicionales y oportunidades respaldadas), relaciones de dominancia (A domina B, B domina A o sin dominancia demostrable), tiers cualitativos solo si criterios definidos antes de observar resultados son reproducibles, y lista de investigación para activos no clasificables.

Activos incomparables no se fuerzan a un orden común. Ausencia de dominancia no implica equivalencia.

## 5. Condiciones para habilitar agregación cuantitativa

Una futura puntuación compuesta requiere cambio metodológico separado y versionado que demuestre:
1. constructo que pretende medir y finalidad;
2. población/universo y horizontes aplicables;
3. definición y dirección de variables;
4. fundamento empírico de escalas, normalización y pesos;
5. tratamiento de correlación, redundancia, dependencia, faltantes y outliers;
6. sensibilidad a decisiones metodológicas;
7. calibración en datos de entrenamiento y congelación del protocolo;
8. validación temporal independiente/out-of-sample, incluidos regímenes distintos;
9. benchmarks, costes, liquidez, capacidad y reglas simples de referencia;
10. controles de look-ahead bias, survivorship bias, leakage, data snooping y falsos descubrimientos;
11. estabilidad, reproducibilidad, explicabilidad y condiciones de invalidez;
12. monitorización de degradación y política de recalibración.

Los datos usados para seleccionar variables/pesos no pueden ser la única prueba de desempeño. Congelar protocolo y parámetros antes del periodo de evaluación independiente. No declarar eficacia predictiva antes de completar pruebas.

## 6. Horizonte y universo

Mantener rankings distintos por horizonte y snapshot del universo. No mezclar activos con definiciones temporales, mercados o timestamps incompatibles. Universos históricos preservan activos desaparecidos o deslistados; un ranking actual no es reconstrucción histórica.

## 7. Reproducibilidad

Conservar ranking_id, versión de política, timestamp UTC, horizonte, universe_snapshot_id, criterios de inclusión, identidad/representación, mercados/pares/monedas, IDs/versiones de evaluaciones y modelos, gates/vetos, valores y estados de evidencia por dimensión, reglas de compatibilidad/dominancia, pares no comparables, cambios de posición, hashes/manifiestos de datos, código/parámetros y motivo de ejecución.

Cambios en universo, fuente, modelo, horizonte, gate, dimensión o política producen nueva versión y explicación. No reescribir rankings históricos.

## 8. Interpretación para el usuario

Mostrar: «clasificación comparativa informativa; no es una orden de compra/venta». Primero bloqueos y riesgos críticos; después oportunidades y limitaciones. Una posición superior solo significa que la evidencia disponible bajo esa versión del protocolo la sitúa por delante en dimensiones comparables; no garantiza mayor rentabilidad futura.
