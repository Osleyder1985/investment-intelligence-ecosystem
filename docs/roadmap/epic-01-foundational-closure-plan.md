# EPIC-01 — Plan de cierre fundacional: identidad, universo y fuentes

**ID:** IIE-EPIC-01-CLOSURE-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / PENDING_REVIEW / NOT_APPROVED  
**Plan padre:** [Plan Maestro](master-methodology-implementation-plan.md)  
**Backlog:** [Backlog MVP](mvp-executable-backlog.md)  
**Issue coordinador:** [#19](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/19)

## 1. Propósito

Convertir los contratos documentales de identidad, universo, fuentes y calidad/point-in-time en especificaciones revisables y en una base ejecutable acotada. Este plan no declara aprobados IIM-001, IIM-002, IIM-018, IIM-019 ni IIM-020.

## 2. Baseline observado en main

La revisión estática de los artefactos disponibles confirma:

- IIM-001 e IIM-019 tienen un modelo de entidades y claves namespaced, relaciones temporales y perfiles documentales para EVM/Solana, Cosmos SDK/IBC, UTXO Bitcoin-like y Cardano/eUTXO.
- Existe un contrato de fixtures v0.1.0 y un harness offline implementado para el alcance inicial EVM/Solana.
- El archivo tests/identity_fixtures/test_harness.py contiene nueve pruebas unitarias. Issue #19 registra evidencia de ejecución local de nueve pruebas OK y dos fixtures PASS más un INCONCLUSIVE esperado en PR #31. Esta evidencia es histórica y no sustituye la ejecución CI actual ni revisión independiente.
- PR #32 fue descrito en Issue #19 como abierto y pendiente de ejecución; sus cambios no se consideran integrados ni probados hasta verificar su estado.
- IIM-020 tiene política de universos por finalidad, snapshots, eventos de membresía y prevención de survivorship bias, pero no hay demostración de exhaustividad histórica sobre un universo real.
- IIM-002 dispone de un registro de fuentes y una política de permisos separados por operación; no se han examinado expedientes probatorios concretos suficientes para aprobar fuentes.
- IIM-018 separa disponibilidad pública/autorizada, ingestión, captura y procesamiento, pero falta validación real de controles point-in-time y su aplicación por finalidad.

La inspección es documental/estática; no implica ejecución de tests, llamadas RPC, auditoría legal ni validación empírica.

## 3. Matriz de cierre por requisito

| Requisito | Evidencia existente | Brecha que bloquea cierre | Estado conservador |
|---|---|---|---|
| IIM-001 | Modelo tipado; perfiles de red; harness sintético EVM/Solana | Revisión independiente, cobertura ejecutable por perfil, casos de relaciones complejas y evidencia de red capturada permitida | REVISION_REQUIRED |
| IIM-019 | Representaciones y relaciones tipadas/temporales | Validar wrappers, bridges, proxies/upgrades, migraciones, forks y relaciones multi-hop con casos reproducibles | REVISION_REQUIRED |
| IIM-020 | Política de universo, snapshots, eventos, cobertura y sesgo de supervivencia | Método verificable de enumeración, reconciliación de fuentes, activos desaparecidos, snapshots históricos y cuantificación de cobertura residual | REVISION_REQUIRED |
| IIM-018 | Calidad, linaje, timestamps, replay y reconstrucción point-in-time | Controles automatizados por variable/finalidad, late data, revisiones, backfills, conflictos y validación histórica | REVISION_REQUIRED |
| IIM-002 | Registro de fuentes y matriz conceptual de permisos | Expedientes concretos de términos/licencia, proveedor/producto, operación, finalidad, usuario, periodo, retención y redistribución | BLOCKED para aprobar fuentes concretas |

No elevar requisitos a SPECIFIED por la existencia de documentos o fixtures.

## 4. Work packages y orden de ejecución

### WP-01 — Baseline y trazabilidad
- Reconciliar registro IIM, matriz de trazabilidad, Issue #19, backlog MVP y artefactos actuales.
- Corregir afirmaciones obsoletas sobre ausencia de código o pruebas.
- Crear crosswalk requisito → decisión → perfil → caso → fixture → test → evidencia.
- Registrar la diferencia entre evidencia local histórica, ejecución CI actual y revisión independiente.

**Aceptación:** no hay contradicciones de estado ni afirmaciones de test no respaldadas.

### WP-02 — Revisión semántica de identidad
- Revisar cardinalidades y unicidad de claves namespaced.
- Revisar relaciones de equivalencia económica sin fusionar representaciones.
- Revisar wrappers, bridges, proxies/implementations, forks, migraciones, redenominaciones y relaciones temporales.
- Revisar resolución de red mediante genesis/checkpoints y casos ambiguos.
- Identificar perfiles fuera del alcance inicial; no extrapolar un perfil de familia a otra.

**Aceptación:** contrato de identidad revisado por perfil y relaciones, con casos positivos, negativos, ambiguos y de conflicto.

### WP-03 — Cobertura ejecutable de perfiles
- Mantener EVM/Solana como primer alcance del harness, sin presentarlo como soporte universal.
- Diseñar estrategia de adaptadores/fixtures para Cosmos/IBC, UTXO Bitcoin-like y Cardano/eUTXO.
- Convertir casos AS prioritarios en fixtures deterministas.
- Separar SYNTHETIC, CAPTURED y REDACTED_CAPTURE.
- Exigir permiso documentado antes de almacenar fixtures capturados.
- Agregar pruebas de regresión, propiedades y casos adversariales por perfil.

**Aceptación:** cada perfil habilitado para MVP tiene cobertura declarada y tests reproducibles; los perfiles no implementados quedan explícitamente fuera de alcance.

### WP-04 — Universo y survivorship bias
- Definir un alcance inicial acotado de redes, entidades, venues y periodo.
- Definir fuentes de descubrimiento y mecanismos de enumeración.
- Registrar query, filtros, páginas/cursors, cuotas, fallos, cobertura y watermark.
- Reconciliar fuentes independientes y deduplicar por identidad tipada.
- Conservar activos deslistados, fallidos, migrados, retirados y sin mercado.
- Distinguir ausencia de proveedor de inexistencia o exclusión.
- Construir al menos un snapshot reproducible y un procedimiento de reconstrucción histórica; etiquetar cobertura incompleta si no puede demostrarse exhaustividad.

**Aceptación:** manifiesto de universo con límites explícitos, conteos reconciliados, eventos de membresía y sesgo residual cuantificado o declarado desconocido.

### WP-05 — Permisos y admisibilidad de fuentes
- Crear inventario de fuentes candidatas sin activar captura por defecto.
- Por proveedor/producto, verificar términos vigentes y versión, identidad contractual, jurisdicción declarada y fecha de consulta.
- Evaluar separadamente consulta, captura, almacenamiento, retención, transformación, modelado, visualización, exportación y redistribución.
- Registrar cobertura real, límites, cuotas, metodología, revisiones y SLA si aplica.
- Mantener TERMS_PENDING/RESTRICTED cuando falte evidencia; solicitar revisión legal cuando el caso lo requiera.

**Aceptación:** cada fuente usada en el MVP tiene expediente por operación/finalidad y responsable de revisión. No inferir permiso por ser API pública o gratuita.

### WP-06 — Calidad y point-in-time
- Definir controles obligatorios por campo, finalidad, horizonte y modalidad.
- Separar REPLAY_OPERATIONAL de RECONSTRUCTION_POINT_IN_TIME.
- Validar event time, public/authorized availability, ingestion, capture y processing.
- Definir tratamiento de revisiones, backfills, late-arriving data, gaps, outliers, conflictos y cambios de metodología.
- Aplicar propagación de estados y abstención cuando timestamps críticos sean UNKNOWN o exista look-ahead.

**Aceptación:** pruebas automatizadas de leakage temporal, revisiones y datos tardíos; snapshots reproducibles con linaje completo.

### WP-07 — Revisión independiente y decisión formal
- Preparar paquete de revisión con artefactos, casos, decisiones y evidencia.
- Obtener revisión por perfiles técnicos y de datos independientes de la autoría.
- Resolver findings críticos/altos o mantenerlos explícitamente abiertos.
- Emitir una decisión separada por requisito, con aprobador, versión, alcance, fecha y evidencia.
- Actualizar registro y matriz únicamente tras aprobación autorizada.

**Aceptación:** acta/registro de decisión por requisito. No se exige aprobar todo: BLOCKED/REVISION_REQUIRED es resultado válido si la evidencia no basta.

## 5. Alcance inicial recomendado del MVP

Para evitar una falsa promesa de cobertura universal, el primer MVP debería:
- limitar explícitamente redes/perfiles a aquellos que el harness realmente implemente y pruebe;
- comenzar con universo pequeño y reproducible;
- usar fixtures sintéticos para pruebas de lógica;
- incorporar datos reales solo cuando el permiso y la procedencia estén demostrados;
- conservar activos ambiguos o no cubiertos como UNKNOWN/QUARANTINED, no forzarlos al ranking;
- separar elegibilidad de investigación, valoración y operación.

La selección del universo concreto y de las tipologías requiere decisión explícita; este documento no fija tickers ni umbrales de liquidez/capitalización.

## 6. Dependencias y bloqueos

- WP-01 precede a la aceptación de los demás paquetes.
- WP-02 y WP-03 deben preceder a habilitar perfiles adicionales.
- WP-04 depende de alcance y fuentes de descubrimiento definidos.
- WP-05 bloquea toda captura/retención/redistribución que requiera permiso no probado.
- WP-06 depende de contratos temporales y de datos coherentes.
- WP-07 cierra el ciclo de revisión, no sustituye la implementación ni validación empírica.

## 7. Criterios globales de salida de EPIC-01

EPIC-01 no se cierra hasta que:
1. Los cinco requisitos tengan decisión explícita y trazable.
2. Los perfiles incluidos en el MVP tengan cobertura y límites ejecutables declarados.
3. El universo tenga un alcance verificable y cobertura reportada.
4. Las fuentes usadas tengan permisos demostrados para sus operaciones y finalidades.
5. La calidad y disponibilidad point-in-time tengan controles ejecutados.
6. Los hallazgos de revisión independiente estén resueltos o aceptados formalmente con riesgo residual.
7. Se actualicen registro, matriz, backlog y Issue #19 con evidencia.
8. Ningún estado supere lo que la evidencia demuestra.

## 8. Fuera de alcance

- Afirmar que todos los criptoactivos o redes están cubiertos.
- Aprobar proveedores sin revisar términos y permisos concretos.
- Ejecutar scraping, RPC o capturas no autorizadas.
- Declarar ausencia/exhaustividad histórica sin evidencia de cobertura.
- Declarar conformidad ISO, validación empírica o preparación para trading real.

## 9. Decisión solicitada

Revisar este plan como descomposición de EPIC-01. La aceptación del plan no aprueba los requisitos IIM ni autoriza el uso de fuentes. Cada WP debe cerrarse con sus propias evidencias y revisiones.
