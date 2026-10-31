# EPIC-01 — Matriz de trazabilidad requisito a evidencia

**ID:** IIE-TRACE-EPIC01-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** INCOMPLETE / NOT_APPROVED  
**Issue coordinador:** #61  
**Plan:** [EPIC-01 closure plan](../roadmap/epic-01-foundational-closure-plan.md)

## 1. Propósito y semántica

Esta matriz permite seguir cada requisito fundacional desde su intención hasta evidencia ejecutable. La presencia de un caso documental no significa que exista fixture, test automatizado, ejecución o validación independiente.

Estados de evidencia:
- DOCUMENTED: política/contrato/caso escrito.
- FIXTURE_DEFINED: entrada reproducible definida.
- TEST_IMPLEMENTED: existe test ejecutable en el repositorio.
- TEST_EXECUTED: existe evidencia de ejecución identificable.
- INDEPENDENTLY_REVIEWED: revisión independiente documentada.
- EMPIRICALLY_VALIDATED: validación con datos reales y protocolo adecuado.

Estos estados no se infieren unos de otros. Un test sintético verifica comportamiento del software, no verdad de red ni validez económica.

## 2. Crosswalk por requisito

| Requisito | Artefactos normativos/documentales | Casos documentales relacionados | Cobertura ejecutable observada | Evidencia y brecha |
|---|---|---|---|---|
| IIM-001 | asset-identity-resolution; identity-profile-fixture-contract; perfiles EVM/Solana, Cosmos/IBC, UTXO y Cardano/eUTXO | AS-001..AS-006, AS-015..AS-018, AS-024..AS-057, AS-063..AS-066 | Harness offline EVM/Solana; fixtures sintéticos EVM case normalization, Solana mint y checkpoint ausente | Issue #19 registra 9 unit tests OK en PR #31. Falta ejecución CI actual, revisión independiente y cobertura ejecutable de perfiles adicionales. |
| IIM-019 | asset-identity-resolution; perfiles de red y contrato de relaciones | AS-004..AS-006, AS-016..AS-018, AS-023..AS-032, AS-035..AS-057, AS-063..AS-066 | Parcial: claves EVM/Solana; no cubre de forma ejecutable toda la semántica de relaciones | Wrappers/bridges, proxies/upgrades, migraciones, forks, IBC multi-hop y relaciones temporales requieren fixtures/tests adicionales y revisión independiente. |
| IIM-020 | universe-construction-policy; eligibility-and-screening | AS-007..AS-014, AS-019..AS-023, AS-025..AS-028, AS-058..AS-062 | No se identifica un constructor de universo ni pruebas de snapshots históricos en el harness de identidad | Falta enumeración verificable, reconciliación de cobertura, universo histórico, activos desaparecidos y prueba de survivorship bias. |
| IIM-018 | data-quality-policy; data-lineage-and-point-in-time | DQ-001..DQ-022 (según catálogo vigente), AS-020 y casos temporales aplicables | No se identifica suite ejecutable integral de calidad/point-in-time | Faltan pruebas automáticas de look-ahead, revisiones, late data, gaps, conflictos, timestamp UNKNOWN y replay/reconstruction. |
| IIM-002 | data-source-register; data-quality-policy; identity-profile-fixture-contract | Casos de provenance/permisos del contrato de fixtures y casos DQ pertinentes | El harness comprueba bloqueo sintético de permisos insuficientes para fixture capturado | No hay expedientes probatorios de proveedores concretos revisados. No se autoriza captura/retención por el mero test sintético. |

La enumeración DQ-001..DQ-022 debe cotejarse con el archivo de casos de calidad cuando esté disponible en main; el rango es una referencia de cobertura documental reportada en el historial de Issue #19, no una afirmación de que todos los casos estén implementados.

## 3. Inventario del harness existente

Artefactos observados:
- tests/identity_fixtures/schema/identity-fixture.schema.json
- tests/identity_fixtures/harness.py
- tests/identity_fixtures/test_harness.py
- tests/identity_fixtures/fixtures/ (fixtures sintéticos descritos en README)
- tests/identity_fixtures/README.md
- requirements-dev.txt

Alcance que declara el README:
- EVM: validación de dirección de 20 bytes, canonicalización case-insensitive, clave namespaced por red y evidencia sintética de presencia de código.
- Solana: decodificación Base58 a 32 bytes, representación canónica, clave namespaced y evidencia sintética de mint/program owner.
- Validación JSON Schema y estados PASS/FAIL/INCONCLUSIVE/BLOCKED/NOT_RUN.

Límites declarados:
- No RPC/explorers.
- No verifica estado real de red, estándares de token, consenso/finalidad ni equivalencia económica.
- No aprueba fuentes ni permisos.
- Fixtures sintéticos no son evidencia real.

## 4. Registro de pruebas y ejecución

| Evidencia | Estado conocido | Interpretación permitida |
|---|---|---|
| PR #31 / Issue #19 | Historial registra nueve pruebas unitarias OK y dos fixtures PASS + un INCONCLUSIVE esperado | Evidencia local histórica reportada; no sustituye CI actual ni revisión independiente |
| PR #32 | Issue #19 lo describió como abierto y pendiente de ejecución | No contar sus cambios como integrados/probados sin verificar estado actualizado |
| Workflow .github/workflows/ci.yml | Workflow versionado para ejecutar suites identity_fixtures y contracts | Configuración presente; resultado de Actions debe verificarse en GitHub antes de marcar TEST_EXECUTED para esta CI |
| Pruebas de perfiles Cosmos/IBC, UTXO, Cardano | No identificadas en el harness observado | No implementadas/evidenciadas como cobertura ejecutable en el alcance inspeccionado |
| Pruebas de universo y point-in-time | No identificadas como suite ejecutable integral en el alcance inspeccionado | Pendientes |

## 5. Gaps priorizados

| Gap | Requisito | Prioridad | Acción |
|---|---|---|---|
| E01-G01 | IIM-001/019 | P0 | Verificar CI actual y convertir casos de identidad prioritarios en cobertura ejecutable |
| E01-G02 | IIM-001/019 | P0 | Revisión independiente de claves, cardinalidades, relaciones y estados |
| E01-G03 | IIM-020 | P0 | Definir universo inicial, método de enumeración y manifest de cobertura |
| E01-G04 | IIM-002 | P0 | Expedientes concretos de permisos por fuente/producto/operación/finalidad |
| E01-G05 | IIM-018 | P0 | Suite de pruebas point-in-time, late data, revisiones y look-ahead |
| E01-G06 | IIM-019 | P1 | Fixtures para wrappers, bridges, proxies, forks, migraciones y relaciones temporales |
| E01-G07 | IIM-001/019 | P1 | Estrategia ejecutable y criterios de admisión para perfiles adicionales |

## 6. Reglas para actualizar esta matriz

- Añadir test ID, ruta, fixture ID, comando, commit, run URL y resultado antes de marcar TEST_EXECUTED.
- Para revisión independiente, enlazar revisor, alcance, fecha, findings y resolución.
- Para validación empírica, enlazar dataset/snapshot permitido, metodología, protocolo, periodo y resultados.
- No inferir aprobación IIM desde merge de PR.
- No inferir permiso desde acceso público o endpoint gratuito.
- No convertir ausencia de evidencia en evidencia de ausencia.
- Mantener la trazabilidad de pruebas negativas y abstenciones.

## 7. Estado de salida

La matriz está incompleta y pendiente de revisión. No cambia los estados del registro de requisitos. IIM-001, IIM-019, IIM-020 e IIM-018 continúan REVISION_REQUIRED; IIM-002 continúa BLOCKED para aprobar fuentes concretas.
