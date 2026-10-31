# Linaje de datos y semántica point-in-time

**Estado:** Propuesta metodológica; pendiente de revisión y validación. **Versión:** 0.2.1

## 1. Objetivo

Poder reconstruir qué dato se utilizó, de dónde provino, cómo se transformó y si estaba realmente disponible en el instante histórico de decisión. La hora de captura no sustituye la hora de publicación o disponibilidad.

## 2. Timestamps obligatorios

| Timestamp | Significado |
|---|---|
| event_time | Momento en que ocurrió el hecho económico/técnico |
| publication_time | Momento en que el proveedor publicó el dato |
| public_availability_time | Primera disponibilidad pública demostrable por una vía identificada |
| authorized_availability_time | Primera disponibilidad demostrable para una cuenta/usuario autorizado concreto |
| system_ingestion_time | Momento en que el sistema bajo evaluación incorporó el dato |
| knowledge_time | Momento en que una evidencia pudo conocerse por una población y vía declaradas; no sustituye los timestamps específicos anteriores |
| capture_time | Momento en que el sistema lo capturó |
| processing_time | Momento en que se validó/transformó |
| decision_time | Momento de corte de la evaluación |

Todos los tiempos deben normalizarse a UTC y conservar el valor/origen original cuando sea necesario. Si `publication_time`, `public_availability_time` o `authorized_availability_time` no se conocen, registrar UNKNOWN; no inferirlos a partir de `capture_time`. Para evaluar qué conocía el sistema concreto, exigir `system_ingestion_time` y disponibilidad interna demostrables. `availability_time` solo puede usarse como alias derivado si identifica expresamente la modalidad (pública o autorizada) y no se confunde con ingestión.

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

Para una evaluación con decision_time = t, distinguir dos modalidades:

- REPLAY_OPERATIONAL: solo se admite información que el sistema concreto había ingerido y tenía disponible antes de t.
- RECONSTRUCTION_POINT_IN_TIME: puede utilizarse información publicada y demostrablemente accesible antes de t aunque se capture después, pero debe identificarse como reconstrucción retrospectiva y no como reproducción de lo que el sistema realmente procesó entonces.

En ambas modalidades:
- admitir solo información cuya disponibilidad pública o autorizada pertinente esté demostrada en o antes de t; en `REPLAY_OPERATIONAL`, exigir además `system_ingestion_time` y disponibilidad interna anteriores a t;
- si la disponibilidad pertinente es UNKNOWN y la variable puede introducir look-ahead, excluirla o marcar evaluación no reproducible;
- no usar revisiones posteriores como si fueran valores originales;
- preservar versiones y fechas de revisión del proveedor;
- distinguir fecha del evento de fecha en que se conoció;
- no usar calendarios futuros, unlocks, anuncios o estados publicados después de t, salvo que fueran públicamente conocidos antes y exista evidencia temporal;
- cualquier dato derivado hereda la disponibilidad más tardía entre sus insumos y la fecha de procesamiento que corresponda.

Un valor corregido retrospectivamente debe guardarse como nueva versión con su propio timestamp de disponibilidad pública o autorizada, según corresponda. No reemplazar silenciosamente el valor que se conocía en t.

## 5. Linaje de derivados

Para cada métrica derivada registrar fórmula, unidades, variables de entrada, versión de código, parámetros, tratamiento de faltantes, ventana, método de agregación y errores conocidos. La misma entrada, versión y parámetros deben permitir reproducir el mismo resultado o documentar por qué no es determinista.

## 6. Cambios y correcciones

Registrar cambio de proveedor, endpoint, schema, método de agregación, contrato, ticker, red, unidad, timezone, backfill y corrección. Medir impacto sobre series históricas y evaluaciones previamente emitidas. No reescribir resultados históricos sin crear una nueva versión de evaluación.

## 7. Artefacto de reproducción

Un manifiesto de evaluación debe incluir decision_time, dataset/versiones, source_ids, hashes, rangos temporales, parámetros, commits, transformaciones, licencias y exclusiones. Si algún insumo no puede recuperarse por restricciones legales o técnicas, declarar el nivel de reproducibilidad alcanzable.


## 8. Semántica de disponibilidad por modalidad (aclaración v0.2.0)

No usar un único timestamp ambiguo para representar todas las formas de conocimiento. Cuando aplique, conservar:

- `public_availability_time`: primera disponibilidad pública demostrable por una vía identificada;
- `authorized_availability_time`: primera disponibilidad demostrable para una cuenta/usuario autorizado concreto;
- `system_ingestion_time`: momento en que el sistema bajo evaluación incorporó el dato;
- `capture_time`: momento en que se obtuvo una copia o respuesta;
- `processing_time`: momento en que terminó la validación/transformación.

Cada timestamp debe tener fuente de evidencia, precisión, zona original, método de determinación y estado de confianza. Si la fecha no puede probarse, registrar UNKNOWN, no estimarla desde el timestamp de captura.

En `REPLAY_OPERATIONAL`, exigir que el sistema concreto hubiera ingerido y tenido disponible el dato antes de decision_time. En `RECONSTRUCTION_POINT_IN_TIME`, admitir evidencia publicada/accesible antes del corte aunque se capture después, siempre que el modo retrospectivo y sus límites se declaren. No describir una reconstrucción como replay real del sistema.

## 9. Disponibilidad de datos on-chain y datos derivados

Para datos on-chain separar event/block time, bloque y hash, estado de finalización, reorganización, disponibilidad pública/autorizada del nodo o indexador y `system_ingestion_time`. El timestamp de bloque no prueba cuándo un proveedor terminó de indexar ni cuándo el sistema lo consumió.

Para agregaciones, velas y derivados, documentar cierre de ventana, retrasos, revisiones, backfills y regla de publicación. La disponibilidad del derivado no puede ser anterior a la disponibilidad de sus insumos ni al momento en que el cálculo estuvo terminado y disponible. Conservar valores preliminares y finales como versiones diferentes cuando el proveedor los revise.

## 10. Evidencia y reproducibilidad restringida

El manifiesto debe declarar si conserva payload, hash, referencia externa o solo metadatos, y por qué esa conservación está permitida. Un hash sin el objeto fuente ni evidencia de origen solo permite comprobaciones limitadas. Si los términos impiden conservar el insumo, describir qué parte del resultado puede reproducirse y qué parte depende de una fuente externa no preservada.
