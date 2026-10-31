# Linaje de datos y semántica point-in-time

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.1.0

## 1. Objetivo

Poder reconstruir qué dato se utilizó, de dónde provino, cómo se transformó y si estaba realmente disponible en el instante histórico de decisión. La hora de captura no sustituye la hora de publicación o disponibilidad.

## 2. Timestamps obligatorios

| Timestamp | Significado |
|---|---|
| event_time | Momento en que ocurrió el hecho económico/técnico |
| publication_time | Momento en que el proveedor publicó el dato |
| availability_time / knowledge_time | Primer momento demostrable en que el dato estuvo disponible para el sistema/usuario |
| capture_time | Momento en que el sistema lo capturó |
| processing_time | Momento en que se validó/transformó |
| decision_time | Momento de corte de la evaluación |

Todos los tiempos deben normalizarse a UTC y conservar el valor/origen original cuando sea necesario. Si publication_time o availability_time no se conocen, registrar DESCONOCIDO; no inferirlos a partir de capture_time.

Para datos on-chain, conservar block height, block timestamp, hash de bloque/transacción, red y semántica de finalización. Un timestamp de bloque no demuestra por sí solo cuándo el dato estuvo indexado o disponible para un observador.

## 3. Modelo de linaje

Cada dato normalizado o derivado debe poder remontarse a:
- source_id y versión del producto/dataset;
- identificador del registro original, URL/endpoint o archivo;
- periodo y parámetros de consulta;
- payload/raw reference permitido y su hash, o referencia/hash de evidencia si la licencia impide conservar payload;
- esquema y versión del parser;
- transformaciones, código/commit, configuración y parámetros;
- unidad, moneda, precisión, zona horaria y normalización;
- controles, reconciliaciones y resultados;
- timestamps originales y de procesamiento;
- licencia/permiso aplicable;
- identificador de evaluación que consumió el dato.

No almacenar datos cuyo almacenamiento esté prohibido. En ese caso conservar solo los metadatos, hashes y referencias que sí estén permitidos, sin reconstruir ni redistribuir el contenido restringido.

## 4. Reglas point-in-time

Para una evaluación con decision_time = t:
- admitir solo información con availability_time ≤ t;
- si availability_time es desconocido y la variable puede introducir look-ahead, excluirla o marcar evaluación no reproducible;
- no usar revisiones posteriores como si fueran valores originales;
- preservar versiones y fechas de revisión del proveedor;
- distinguir fecha del evento de fecha en que se conoció;
- no usar calendarios futuros, unlocks, anuncios o estados publicados después de t, salvo que fueran públicamente conocidos antes y exista evidencia temporal;
- cualquier dato derivado hereda la disponibilidad más tardía entre sus insumos y la fecha de procesamiento que corresponda.

Un valor corregido retrospectivamente debe guardarse como nueva versión con su propio availability_time. No reemplazar silenciosamente el valor que se conocía en t.

## 5. Linaje de derivados

Para cada métrica derivada registrar fórmula, unidades, variables de entrada, versión de código, parámetros, tratamiento de faltantes, ventana, método de agregación y errores conocidos. La misma entrada, versión y parámetros deben permitir reproducir el mismo resultado o documentar por qué no es determinista.

## 6. Cambios y correcciones

Registrar cambio de proveedor, endpoint, schema, método de agregación, contrato, ticker, red, unidad, timezone, backfill y corrección. Medir impacto sobre series históricas y evaluaciones previamente emitidas. No reescribir resultados históricos sin crear una nueva versión de evaluación.

## 7. Artefacto de reproducción

Un manifiesto de evaluación debe incluir decision_time, dataset/versiones, source_ids, hashes, rangos temporales, parámetros, commits, transformaciones, licencias y exclusiones. Si algún insumo no puede recuperarse por restricciones legales o técnicas, declarar el nivel de reproducibilidad alcanzable.
