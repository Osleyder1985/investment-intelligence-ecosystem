# Hallazgos de revisión metodológica — Issue #19

**Alcance:** IIM-001, IIM-002, IIM-018, IIM-019 e IIM-020.  
**Base revisada:** rama `main`, después de incorporar el PR #20.  
**Naturaleza:** revisión técnica documental asistida; no es aprobación independiente, dictamen jurídico ni validación empírica.  
**Estado global:** REVISION_REQUIRED / BLOCKED según el requisito.  
**Issue:** [#19](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/19).

## 1. Conclusión ejecutiva

Los documentos existentes presentan una base conceptual coherente: el ticker no es identidad, las representaciones se mantienen separadas, los universos son snapshots, la calidad depende del uso, los faltantes no se convierten en cero y la disponibilidad temporal no se infiere de la captura.

Sin embargo, la documentación todavía no define con precisión suficiente algunos contratos necesarios para una implementación interoperable y reproducible. En particular:

1. falta un modelo explícito de identidad con namespaces y cardinalidades entre activo económico, instrumento/representación, red, proyecto y mercado;
2. falta precisar cómo se demuestra la cobertura y la incompletitud del universo histórico;
3. faltan reglas operativas para resolver identidades de redes heterogéneas y contratos actualizables;
4. la aprobación real de fuentes requiere evidencia concreta de términos, permisos, versión, jurisdicción y uso, no solo la estructura de un registro;
5. los timestamps point-in-time necesitan una semántica más estricta para datos públicos, acceso autorizado, ingestión interna y reconstrucción retrospectiva.

No se recomienda asignar SPECIFIED a ninguno de los cinco requisitos en esta revisión.

## 2. Registro de decisión por requisito

| Requisito | Decisión de revisión | Motivo principal | Estado recomendado |
|---|---|---|---|
| IIM-001 | REVISION_REQUIRED | Modelo de identidad y claves técnicas insuficientemente formalizados para redes heterogéneas y representaciones complejas | DOCUMENTED; SPECIFICATION_DRAFTED |
| IIM-019 | REVISION_REQUIRED | Requiere cerrar cardinalidades, namespaces, cambios de contrato y reglas de corroboración | DOCUMENTED; SPECIFICATION_DRAFTED |
| IIM-020 | REVISION_REQUIRED | Falta contrato de cobertura/completitud del universo y semántica temporal completa de membresía | DOCUMENTED; SPECIFICATION_DRAFTED |
| IIM-002 | BLOCKED para aprobación de fuentes concretas | No se ha aportado evidencia por proveedor/producto de permisos, términos y alcance real | DOCUMENTED; SPECIFICATION_DRAFTED; fuentes individuales TERMS_PENDING/CANDIDATE hasta revisión |
| IIM-018 | REVISION_REQUIRED | Los principios de calidad son adecuados, pero faltan contratos operativos por variable/fuente y tratamiento formal de disponibilidad | DOCUMENTED; SPECIFICATION_DRAFTED |

Estas decisiones describen el estado de la documentación revisada. No prueban que no existan fuentes o implementaciones externas. Tampoco sustituyen una revisión legal de términos.

## 3. Hallazgos sobre identidad — IIM-001 e IIM-019

### 3.1 Separar entidades y relaciones

La documentación enumera correctamente Economic asset, Native asset, Token representation, Wrapped/bridged representation, Market instrument, Listing y Project/protocol. Falta convertir esta enumeración en un modelo explícito de entidades y relaciones.

Debe precisarse que:

- un activo económico puede no tener una representación tokenizada;
- un activo económico puede tener varias representaciones simultáneas;
- una representación puede depender de más de un componente o colateral;
- un proyecto/protocolo no es necesariamente el emisor ni el titular de derechos del token;
- un instrumento de mercado es una cotización negociable de una representación en un venue, no una identidad del activo;
- las relaciones pueden cambiar y deben tener vigencia temporal.

No se debe utilizar un único `canonical_asset_id` para representar indistintamente el activo económico y cada token negociable. Definir IDs separados para las entidades que el modelo realmente necesite, con relaciones explícitas y versionadas. El esquema final debe evitar tanto la fusión de activos distintos como el doble conteo de exposiciones relacionadas.

### 3.2 Namespace de red e identificadores técnicos

`chain_id` por sí solo no es una clave universal para todas las redes. La especificación debe definir un `network_id` interno namespaced, con identificadores externos opcionales y versionados. Debe poder distinguir redes que reutilicen identificadores numéricos, redes privadas/testnet/mainnet, forks y cambios de génesis.

La dirección de contrato debe normalizarse según las reglas de la red, no con una normalización global asumida. Deben contemplarse, según corresponda, EVM, Solana/mint, UTXO, Cosmos/denom y otros esquemas de identificación. Para activos nativos, usar un tipo de identificador nativo propio de la red; no simular una dirección de contrato.

### 3.3 Contratos proxy, actualizaciones y autoridad

La identidad no debe depender solo de la dirección del contrato. Para contratos actualizables/proxy, registrar proxy, implementación observada, mecanismo de actualización, administrador/autoridad y vigencia. Un cambio de implementación puede alterar derechos o comportamiento sin cambiar la dirección. Conservar eventos y evidencia de cada transición.

### 3.4 Corroboración y confianza

La regla de verificar con explorador/nodo independiente es útil, pero no siempre dos fuentes son realmente independientes: pueden compartir proveedor de infraestructura o indexador. Registrar independencia conocida, método de verificación, bloque/estado de referencia, finalización/reorganización y evidencia archivada o hash permitido.

La documentación oficial y el estado on-chain responden preguntas distintas: la primera puede describir intención/derechos; la segunda puede probar despliegue y comportamiento observable. Ninguna debe sustituir automáticamente a la otra.

### 3.5 Salida y relaciones

Formalizar cardinalidades, claves, restricciones de unicidad, vigencia temporal, estados y transiciones. Incluir la fuente de cada relación (wrapper, bridge, successor, migration, fork, proxy implementation) y no inferir equivalencia económica por similitud de ticker o precio.

## 4. Hallazgos sobre universo y screening — IIM-020

### 4.1 Universo descubierto no equivale a universo completo

Un `DISCOVERY_UNIVERSE` construido desde proveedores puede ser solo la cobertura de esos proveedores. Cada snapshot debe indicar alcance de búsqueda, fuentes, cobertura conocida, exclusiones, fallos de consulta y limitaciones. No afirmar exhaustividad si no hay método y evidencia para sostenerla.

### 4.2 Tiempo de decisión y vigencia

Separar explícitamente:
- `decision_time`: instante al que se refiere la decisión;
- `snapshot_created_at`: momento en que se generó el snapshot;
- `knowledge_cutoff`: límite de información admisible;
- `valid_time`: periodo en que la membresía o característica era efectiva;
- `availability_time`: cuándo la evidencia pudo conocerse;
- `capture_time`: cuándo el sistema la obtuvo.

Una reconstrucción histórica puede crearse después de la fecha de decisión, pero no puede presentar datos descubiertos después como si fueran conocidos entonces.

### 4.3 Entrada, salida y desaparición

Definir eventos de membresía (candidate discovered, included, limited, quarantined, excluded, delisted, migrated, retired, unresolved) con fecha efectiva, fecha de conocimiento, motivo, evidencia y fuente. La ausencia de un activo en un proveedor no prueba que el activo no existiera ni que hubiera sido deslistado.

### 4.4 Cobertura y supervivencia

El universo histórico debe conservar activos fallidos, abandonados, insolventes, migrados y deslistados cuando exista evidencia. Si no hay evidencia suficiente para reconstruirlos, declarar cobertura incompleta y sesgo residual; no imputar una lista completa. Distinguir precio último observado, precio ejecutable, recuperación/redención y pérdida final: un último precio no equivale a valor de salida ni a cero.

### 4.5 Elegibilidad por finalidad

Mantener resultados separados por finalidad. La investigación puede continuar con incertidumbre; la valoración requiere inputs y derechos suficientes; la operación requiere ruta y permisos vigentes; el backtest requiere disponibilidad temporal demostrable y tratamiento de activos desaparecidos. No propagar una elegibilidad de una finalidad a otra.

## 5. Hallazgos sobre fuentes y licencias — IIM-002

El registro propuesto contempla los campos adecuados, pero no hay en los artefactos revisados un expediente de fuente concreta que demuestre permisos efectivos.

Para cada proveedor y producto/dataset, antes de aprobarlo para un uso, conservar:

- identidad del proveedor, producto, entidad contratante y jurisdicción relevante;
- URL y copia/hash o referencia verificable de los términos aplicables, versión y fecha de vigencia;
- permiso separado para consultar, capturar, almacenar, conservar, transformar, entrenar/modelar, visualizar, exportar y redistribuir;
- finalidad, usuario autorizado, límites, atribución y obligaciones de borrado/retención;
- campos, activos, mercados, endpoints, frecuencia y periodos cubiertos;
- restricciones técnicas, cuotas y condiciones de autenticación cuando afecten disponibilidad;
- evidencia de evaluación y fecha de reevaluación.

El acceso técnico o una API gratuita no demuestra permiso para almacenar, redistribuir o usar comercialmente. Si los términos son ambiguos o no están disponibles, mantener TERMS_PENDING para los usos afectados. La plantilla documental no es una autorización. Este registro no emite conclusión jurídica.

## 6. Hallazgos sobre calidad, linaje y point-in-time — IIM-018

### 6.1 Contrato de admisibilidad contextual

Los estados ACCEPTED, ACCEPTED_WITH_LIMITATIONS, QUARANTINED, REJECTED y UNKNOWN son adecuados como vocabulario inicial. Falta definir cómo se determina, por variable y uso, qué controles son obligatorios, cuáles son sus resultados, qué evidencia los sustenta y qué decisión downstream bloquean.

No se recomienda crear umbrales universales de completitud, desviación, latencia o discrepancia sin calibración y justificación por uso.

### 6.2 Semántica de disponibilidad

Distinguir como mínimo:
- disponibilidad pública demostrable;
- disponibilidad para un usuario/cuenta con permisos;
- ingestión efectiva por el sistema bajo evaluación;
- captura posterior para reconstrucción retrospectiva.

`availability_time` debe indicar qué población de observadores y qué vía de acceso se consideran. Si no puede demostrarse el momento de disponibilidad, usar UNKNOWN y excluirlo de una afirmación histórica que dependa de él.

### 6.3 Datos on-chain

Registrar red, bloque, hash, altura, timestamp de bloque, estado de finalización, reorganizaciones, momento de indexación y proveedor/nodo. El timestamp de bloque no prueba por sí mismo la hora de disponibilidad de un indexador ni la ingestión interna.

### 6.4 Derivados y correcciones

Un dato derivado debe heredar la disponibilidad más tardía de sus insumos y respetar el tiempo de procesamiento necesario. Documentar cómo se manejan ventanas, agregaciones, cierres de vela, revisiones, backfills y eventos tardíos. Las correcciones no deben reescribir lo que se conocía en una fecha anterior.

### 6.5 Reproducibilidad y restricciones de almacenamiento

Cuando los términos impidan conservar payloads, especificar qué metadatos, hashes, parámetros y evidencias de validación sí pueden conservarse. Un hash prueba integridad de un objeto disponible, no demuestra por sí solo su origen, veracidad ni permiso de uso.

## 7. Acciones necesarias para cerrar la revisión

| Acción | Requisito | Evidencia de cierre |
|---|---|---|
| Formalizar modelo de entidades, claves, namespaces, cardinalidades y relaciones temporales | IIM-001, IIM-019 | Especificación versionada y casos límite revisados |
| Especificar redes heterogéneas, activos nativos, proxy/upgrade y eventos de migración | IIM-001, IIM-019 | Reglas de resolución y ejemplos verificables |
| Definir contrato de snapshot, cobertura, conocimiento y eventos de membresía | IIM-020 | Esquema temporal y casos de reconstrucción histórica |
| Crear expedientes reales de fuentes y evaluar permisos por uso | IIM-002 | Evidencia de términos/versiones y decisiones por fuente; puntos no resueltos en TERMS_PENDING |
| Formalizar controles de admisibilidad por variable/uso y semántica de disponibilidad | IIM-018 | Tabla de controles, decisiones, estados UNKNOWN y casos de look-ahead |
| Actualizar casos documentales con escenarios de redes heterogéneas, proxies, universo incompleto, revisiones y permisos | IIM-001, 002, 018, 019, 020 | Casos añadidos; aún no declarar tests ejecutados |

## 8. Aprobación y límites

Este documento registra hallazgos y decisiones de revisión preliminar, no aprobación metodológica formal. Los cinco requisitos permanecen DOCUMENTED / SPECIFICATION_DRAFTED. IIM-002 queda además bloqueado para aprobar fuentes concretas hasta disponer de evidencia de términos y permisos.

Para cerrar el Issue #19 se requiere revisar los documentos corregidos, resolver o delimitar cada cuestión, completar registros individuales con revisor y fecha, y actualizar la matriz/registro de requisitos. La aprobación interna no es certificación ISO ni validación empírica.

No se han inspeccionado aquí contratos de proveedores concretos, nodos, datos históricos ni el repositorio de implementación; por tanto, no se afirma que sus permisos, cobertura o comportamiento estén comprobados.
