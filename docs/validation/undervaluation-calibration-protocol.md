# Protocolo de calibración y validación de infravaloración

**Estado:** Protocolo de diseño; pendiente de ejecución y aprobación de criterios.  
**Versión:** 0.1.0

## 1. Objetivo

Determinar si las medidas de brecha y las reglas de clasificación aportan información reproducible y útil fuera de la muestra, y si una categoría de infravaloración extrema puede definirse sin sobreajuste.

## 2. Unidad experimental y timestamp

Cada observación debe ser una evaluación de un activo en un instante de decisión t, con:

- identidad de activo, red y contrato;
- universo de activos elegibles conocido en t;
- datos disponibles en t, con timestamps de publicación y captura;
- modelo, parámetros y versión del protocolo disponibles en t;
- precio y liquidez ejecutables observables en t;
- horizonte de evaluación fijado antes de observar resultados futuros.

Solo pueden usarse datos que estaban disponibles en el instante de decisión. La fecha de un evento debe distinguirse de la fecha en que el mercado o el proveedor lo hizo disponible.

## 3. Construcción del universo histórico

- Reconstruir qué activos existían y eran elegibles en cada fecha.
- Incluir activos posteriormente deslistados, fallidos, migrados o desaparecidos.
- Conservar cambios de ticker, contrato, red, redenominación y migraciones.
- No seleccionar retrospectivamente solo activos que sobreviven o que hoy tienen datos completos.
- Registrar exclusiones, su fecha y la evidencia disponible en el momento.
- Tratar los datos no disponibles como faltantes, no como ceros.

## 4. Separación temporal

Separar cronológicamente desarrollo, calibración, validación y prueba final. Los límites y reglas se fijan antes de analizar resultados. Aplicar walk-forward: en cada ventana, entrenar/calibrar solo con información anterior al instante de evaluación y evaluar en el periodo posterior.

No ajustar parámetros, definiciones ni umbrales mirando el conjunto de prueba. Si se cambia el protocolo tras ver resultados, crear nueva versión y nuevo periodo de prueba no contaminado.

## 5. Calibración de la categoría extrema

La categoría permanecerá NOT_CALIBRATED hasta cumplir todas las condiciones:

1. Definir previamente qué resultado futuro se pretende distinguir: rendimiento neto, comportamiento relativo a benchmark, recuperación de precio, supervivencia u otro resultado; no intercambiarlos.
2. Elegir horizontes y benchmarks antes de examinar resultados.
3. Estimar distribución histórica de la métrica solo con datos de entrenamiento.
4. Evaluar estabilidad entre ventanas temporales, tipologías y regímenes de mercado.
5. Medir falsos positivos, falsos negativos, cobertura y calibración si se emiten probabilidades.
6. Controlar selección múltiple y falsos descubrimientos cuando se prueban muchos activos, modelos o umbrales.
7. Incorporar costes, spread, slippage, impacto, liquidez, restricciones y posibilidad de no ejecución.
8. Comparar contra reglas base simples y benchmarks pertinentes.
9. Documentar fallos, resultados no concluyentes y sensibilidad.
10. Aprobar umbrales solo tras cumplir criterios de aceptación previamente definidos y reproducibles.

No se fija aquí un cuantil, porcentaje, muestra mínima ni probabilidad: esos valores requieren justificación empírica específica y no deben elegirse arbitrariamente.

## 6. Sesgos y amenazas a la validez

Controlar expresamente:

- Look-ahead bias y uso de información publicada después de t.
- Survivorship bias por omitir activos desaparecidos.
- Data leakage entre entrenamiento, calibración y prueba.
- Selection bias al elegir activos, modelos o periodos ganadores.
- Multiple testing y data snooping.
- Cambios de metodología de proveedores y revisiones históricas.
- Sesgo de liquidez, mercados inactivos y precios no ejecutables.
- Dependencia entre observaciones de un mismo activo y entre activos correlacionados.
- Sesgo de disponibilidad de datos y cobertura desigual entre tipologías.

## 7. Métricas y reporte

Definir cada métrica antes de calcularla, con fórmula, unidad, horizonte y limitaciones. Según el objetivo pueden incluir rendimiento neto, retorno relativo al benchmark, drawdown, supervivencia, tasa de falsos descubrimientos, precisión/recall de una clasificación y cobertura de datos. No seleccionar métricas por producir resultados favorables.

Reportar tamaño de muestra, activos excluidos, ventanas, distribución por tipología, costes, intervalos de incertidumbre y resultados negativos. No convertir desempeño retrospectivo en garantía futura.

## 8. Condiciones de no aprobación

No aprobar una categoría extrema si:

- solo funciona en el periodo de desarrollo;
- depende de unos pocos activos o de una sola tipología sin justificar generalización;
- desaparece al incluir activos fallidos/deslistados;
- no supera una regla base pertinente;
- los costes o la liquidez eliminan la ventaja observada;
- existe leakage o selección retrospectiva no corregible;
- la incertidumbre impide sostener la conclusión.

## 9. Artefactos reproducibles

Cada experimento debe registrar protocolo y versión, código/commit, datasets y hashes, timestamps, universo, parámetros, particiones temporales, benchmarks, costes, métricas, resultados, exclusiones, logs y revisión. El conjunto de prueba debe permanecer separado y su uso quedar auditado.

## 10. Estado de salida

Hasta completar validación independiente, la salida de la metodología será EXTREME_UNDERVALUATION_NOT_CALIBRATED. El sistema puede reportar la brecha descriptiva y la conclusión condicionada, pero no usar la etiqueta extrema como clasificación activa.
