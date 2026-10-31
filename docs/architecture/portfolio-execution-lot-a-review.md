# Revisión técnica del lote A — IIM-025, IIM-027 e IIM-028

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_RECOMMENDATIONS_ONLY  
**Issue de seguimiento:** #35  
**Baseline revisada:** propuesta IIM-025 a IIM-037 v0.1.0 y matriz de revisión v0.1.0

> Esta revisión es técnica-documental asistida. No es aprobación del propietario, dictamen de riesgo, revisión independiente ni validación empírica. Los IDs siguen siendo provisionales y las decisiones formales permanecen PENDING_REVIEW.

## 1. Conclusión ejecutiva

El lote A identifica correctamente tres capacidades necesarias, pero las formulaciones iniciales necesitan mayor precisión antes de incorporarse al registro.

| ID | Recomendación técnica | Motivo principal |
|---|---|---|
| IIM-025 | REVISE | El mandato debe distinguir objetivos, restricciones, autoridad, política de inversión y vigencia; no debe mezclarlo con preferencias operativas ni con la cartera concreta. |
| IIM-027 | REVISE | "Exposición" requiere ontología, unidades, denominadores, reglas de look-through, tratamiento de derivados/wrappers y cobertura de datos. |
| IIM-028 | REVISE | Debe separar métricas de riesgo, límites duros, presupuestos, jerarquía, excepciones y comportamiento fail-closed. |

No se recomienda fijar todavía porcentajes máximos, VaR, drawdown, límites por activo, thresholds de liquidez o concentración. Estos valores dependen del mandato, horizonte, calidad de datos, tolerancia al riesgo, metodología y aprobación de gobernanza.

## 2. IIM-025 — Mandato de cartera

### 2.1 Problemas de la formulación inicial

La formulación actual enumera horizonte, moneda base, restricciones, universo, benchmark y límites. Falta explicitar:
- quién tiene autoridad para aprobar, modificar, suspender y revocar el mandato;
- objetivos y criterios de éxito medibles, sin prometer retornos;
- distinción entre restricciones duras y preferencias blandas;
- horizonte de inversión frente a horizonte de liquidez/rescate;
- perfil de pasivos, necesidades de caja y compromisos, si existen;
- política de apalancamiento, derivados, staking, lending, rehypothecation y exposición DeFi, incluso si la política inicial los prohíbe;
- jurisdicción, restricciones legales y de elegibilidad;
- procedimiento para cambios, expiración, suspensión y revisión;
- relación entre mandato, cartera, subcarteras y cuentas/venues.

### 2.2 Redacción propuesta

**IIM-025 — Definir y gobernar mandatos de inversión versionados para cada cartera y subcartera, incluyendo autoridad aprobadora, objetivos, horizonte, moneda de referencia, universo elegible, benchmark cuando aplique, necesidades de liquidez, restricciones duras y preferencias, facultades permitidas/prohibidas, jurisdicción, vigencia y proceso de modificación, suspensión y revocación. Toda propuesta de cartera y toda evaluación de cumplimiento debe referenciar una versión vigente e inmutable del mandato; la ausencia, ambigüedad, expiración o conflicto del mandato debe impedir la aprobación de nuevas asignaciones.**

### 2.3 Criterios de aceptación recomendados

1. Cada mandato tiene ID estable, versión, estado, fecha efectiva, expiración/revisión, autor, aprobador y registro de cambios.
2. Se distingue mandato aprobado de borrador y de mandato suspendido/revocado.
3. Cada restricción se clasifica como HARD_CONSTRAINT o SOFT_PREFERENCE, con prioridad y política de resolución.
4. Los objetivos incluyen horizonte y métricas/criterios observables; no se expresan como rentabilidad garantizada.
5. Las prohibiciones y permisos para spot, derivados, apalancamiento, staking, lending, DeFi, puentes y rehypothecation se expresan explícitamente.
6. Las necesidades de liquidez y restricciones de retiro/rescate se distinguen del horizonte de inversión.
7. Toda propuesta y evaluación conserva el snapshot exacto del mandato usado.
8. Si no hay mandato vigente o hay conflicto no resuelto, el sistema se abstiene de proponer aprobación de asignación.

### 2.4 Decisión recomendada

**REVISE** antes de aceptar para registro. El alcance es necesario, pero necesita incluir gobernanza, facultades operativas y estados del mandato.

## 3. IIM-027 — Exposición y concentración

### 3.1 Problemas de la formulación inicial

La enumeración por activo, red, protocolo, sector, venue y contraparte es un buen inicio, pero no define qué significa exposición ni cómo agregarla. Riesgos a resolver:
- nominal de posición, valor de mercado, exposición delta-equivalente, exposición económica y exposición contingente no son intercambiables;
- wrappers, tokens bridged, recibos de staking, LP tokens, vault shares y colateral pueden generar look-through y doble conteo;
- activos con múltiples representaciones pueden tener riesgo común de emisor, puente, custodio o contrato;
- dependencias compartidas (oráculos, validadores, sequencers, bridges, stablecoins, custodios, clientes de software) generan concentración transversal;
- exposición bruta, neta, colateralizada y contingente requieren tratamientos diferenciados;
- denominador, moneda de conversión, timestamp, precios, FX y método de agregación deben quedar definidos;
- falta política para mappings incompletos, relaciones inferidas y datos UNKNOWN;
- falta distinguir concentración observada de concentración estimada o proxy.

### 3.2 Redacción propuesta

**IIM-027 — Calcular y conservar snapshots reproducibles de exposiciones y concentraciones de cartera, directas e indirectas, usando una taxonomía versionada de activos, instrumentos, redes, emisores, protocolos, contratos, puentes, custodios, venues, contrapartes y dependencias críticas. Cada métrica debe declarar tipo de exposición, unidad, denominador, moneda, timestamp, fuente, método de agregación, cobertura, look-through aplicado y tratamiento de incertidumbre; las relaciones desconocidas o no verificadas deben permanecer explícitas y no convertirse en exposición cero.**

### 3.3 Criterios de aceptación recomendados

1. Define tipos separados para exposición nominal, valor de mercado, exposición económica, delta-equivalente, bruta, neta, colateral y contingente, solo cuando sean aplicables.
2. Cada resultado especifica numerador, denominador, moneda, conversión, timestamp y método.
3. Taxonomías y relaciones tienen ID, versión, procedencia, vigencia y confianza/estado de verificación.
4. Reglas de look-through definen cuándo descomponer wrappers, LP tokens, vaults, staking receipts, bridges y colateral.
5. Evita doble conteo y documenta las reglas de consolidación; no asume que dos representaciones son independientes.
6. Agrega concentraciones por dependencias compartidas cuando exista evidencia suficiente; no infiere causalidad o exposición cuantitativa sin soporte.
7. Diferencia valor medido, estimado, inferido y desconocido.
8. Informa cobertura y exposición no clasificada; UNKNOWN no se interpreta como cero ni como diversificación.
9. Conserva snapshot reproducible de posiciones, mappings, precios, FX, reglas y versión del cálculo.
10. Permite reconstruir cambios entre snapshots y atribuirlos a movimientos, precios, cambios de clasificación o correcciones de datos.

### 3.4 Decisión recomendada

**REVISE** antes de aceptar para registro. El requisito necesita un modelo semántico de exposición y reglas explícitas contra doble conteo y falsa diversificación.

## 4. IIM-028 — Presupuestos y límites de riesgo

### 4.1 Problemas de la formulación inicial

"Presupuesto de riesgo" y "límite duro" no son sinónimos. La formulación debe diferenciar:
- métrica de riesgo (medición);
- apetito/tolerancia al riesgo (decisión de gobernanza);
- presupuesto (asignación de capacidad de riesgo);
- límite (condición de bloqueo o escalado);
- alerta (señal no necesariamente bloqueante);
- excepción temporal (aprobación explícita y acotada).

También falta especificar:
- jerarquía de límites: cartera, subcartera, activo, factor, protocolo, venue y contraparte;
- evaluación pre-trade/pre-allocation y post-trade/post-allocation;
- datos faltantes, stale, discrepantes o cobertura insuficiente;
- conflictos entre límites y reglas de mayor severidad;
- quién puede aprobar excepciones y cómo expiran;
- controles de drift y breach, escalado, reducción de riesgo y recuperación;
- riesgo de modelo y límites de validez de las métricas.

### 4.2 Redacción propuesta

**IIM-028 — Aplicar políticas versionadas de apetito, presupuestos y límites de riesgo a nivel de cartera y sus componentes, separando métricas de medición, alertas, límites duros y excepciones autorizadas. Evaluar el cumplimiento antes y después de cada asignación o cambio de exposición, considerando límites jerárquicos y dependencias entre riesgos; bloquear o abstenerse cuando un límite duro se incumpla, la evidencia sea insuficiente o la medición no sea válida. Toda excepción debe tener autoridad, justificación, alcance, controles compensatorios, vigencia y auditoría explícitos, y nunca podrá anular controles legales, de seguridad o vetos no dispensables.**

### 4.3 Criterios de aceptación recomendados

1. Cada política y límite tiene ID, versión, autoridad, alcance, vigencia, unidad, fuente, método y severidad.
2. Se distingue límite duro, presupuesto, alerta, objetivo y métrica informativa.
3. La jerarquía define precedencia y resolución de conflictos; los límites más restrictivos prevalecen salvo política formalmente aprobada.
4. Evalúa tanto propuesta como cartera resultante, incluyendo costes y exposiciones indirectas relevantes.
5. Los estados UNKNOWN, STALE, INVALID, INSUFFICIENT_COVERAGE y CONFLICTING_DATA no se interpretan como PASS.
6. Incumplir un límite duro bloquea la asignación afectada y produce razón auditable.
7. Las excepciones requieren autorización separada del proceso que propone la asignación; deben ser temporales, acotadas y revocables.
8. No se permite excepción a vetos no dispensables, prohibiciones legales, controles de seguridad o condiciones de mandato marcadas como no derogables.
9. Define acciones ante breach: bloqueo, alerta, escalado, reducción o revisión humana, sin ejecutar automáticamente una liquidación no autorizada.
10. Documenta limitaciones de modelo, cobertura, sensibilidad y condiciones en que una métrica deja de ser válida.

### 4.4 Decisión recomendada

**REVISE** antes de aceptar para registro. Es crítico separar medición, política, excepción y autoridad de bloqueo.

## 5. Dependencias y límites de responsabilidad

| Requisito | Consume | No debe asumir |
|---|---|---|
| IIM-025 | Identidad/universo, datos admisibles, gobernanza y jurisdicción | Que el ranking de activos define el mandato o autoriza una operación. |
| IIM-027 | Posiciones, identidad canónica, mappings, precios/FX y linaje | Que ticker, red o categoría aislada describen toda la exposición económica. |
| IIM-028 | Perfil de riesgo IIM-016, vetos IIM-017, exposiciones IIM-027, escenarios IIM-030 | Que un score agregado puede compensar un límite duro o un dato inválido. |

Separación de responsabilidades:
- **INTELLIGENCE:** produce identidad, datos, mappings, mediciones de riesgo y exposición con incertidumbre.
- **DECISION:** comprueba elegibilidad, mandato, vetos y cumplimiento de gates; se abstiene cuando la evidencia no basta.
- **PORTFOLIO:** propone asignaciones y controla exposiciones/presupuestos a nivel de cartera.
- **EXECUTION:** aplica autorizaciones y controles operativos antes de transmitir órdenes.
- **VALIDATION & LEARNING:** valida metodologías, calibración, estabilidad, integridad temporal y resultados.
- **GOVERNANCE transversal:** aprueba mandatos, políticas, límites, excepciones y autoridades.

## 6. Decisiones que no deben tomarse todavía

No fijar sin análisis y aprobación:
- porcentajes máximos por activo, red, protocolo, venue o contraparte;
- límites de drawdown, VaR, CVaR, volatilidad o pérdida;
- niveles de liquidez mínima o slippage máximo;
- presupuesto de riesgo por estrategia;
- umbrales de correlación o concentración;
- tolerancias de excepciones.

Los valores deben derivarse de objetivos, horizonte, evidencia histórica y escenarios, disponibilidad/calidad de datos, liquidez/capacidad, costes, restricciones y aprobación del órgano responsable. La validación debe incluir sensibilidad y pruebas de estrés, y no limitarse a optimizar sobre datos históricos.

## 7. Orden de implementación recomendado (posterior a aprobación)

1. Esquema de mandato versionado y validación de vigencia/autoridad.
2. Registro versionado de posiciones y taxonomía de exposición.
3. Motor de agregación con cobertura, UNKNOWN y prevención de doble conteo.
4. Registro de políticas de riesgo y límites, con jerarquía y autoridad.
5. Evaluación pre/post asignación con razones y abstención.
6. Pruebas de conformidad, casos adversariales, datos faltantes y escenarios.
7. Solo después, integrar el optimizador de cartera; no habilitar ejecución real.

## 8. Estado de la revisión

- IIM-025: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-027: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-028: **PENDING_REVIEW** — recomendación técnica: REVISE.

La recomendación no modifica el registro oficial ni sustituye la decisión de aprobación del Issue #34. Se requiere revisión del propietario y, por la criticidad de límites de riesgo, revisión independiente de riesgo/gobernanza antes de aceptación formal.
