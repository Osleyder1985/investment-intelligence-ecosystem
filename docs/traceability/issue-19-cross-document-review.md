# Registro de revisión cruzada fundacional — Issue #19

**Estado:** revisión técnica-documental asistida; no constituye aprobación metodológica independiente.  
**Fecha de revisión:** 2026-10-31 (UTC)  
**Rama de trabajo:** `docs/issue-19-cross-document-consistency`  
**Base:** `main` en `df32d5e5a9ed584d1f096c8c55e085b8b7e07fb5`  
**Issue:** [#19](https://github.com/Osleyder1985/investment-intelligence-ecosystem/issues/19)  
**PR de aclaraciones anterior:** [#22](https://github.com/Osleyder1985/investment-intelligence-ecosystem/pull/22)

## 1. Alcance

Se contrastaron los contratos v0.2.0 de identidad, universo, fuentes, calidad y linaje/point-in-time con la matriz de trazabilidad, el registro de brechas, los casos AS/DQ y la plantilla de revisión. La auditoría se limitó al contenido versionado accesible en este repositorio.

No se inspeccionaron expedientes reales de proveedores, términos contractuales concretos, datos históricos, nodos, repositorios de implementación externos ni resultados de ejecución.

## 2. Hallazgos de consistencia

| ID | Hallazgo | Impacto | Acción incorporada en esta rama |
|---|---|---|---|
| XCR-01 | La sección heredada de identidad describía `chain_id` como atributo clave y permitía un marcador nativo dentro del campo de dirección, mientras la sección v0.2.0 exige `network_id` namespaced y discriminador tipado separado. | Riesgo de claves incompatibles y falsa dirección de contrato. | Alinear la sección de identificadores y corroboración con `network_id`, atributos externos namespaced e identificador de representación por familia de red. |
| XCR-02 | La construcción de snapshots aún usaba `canonical_asset_id` junto a `representation_id` sin declarar el tipo del primero. | Riesgo de mezclar activo económico, representación e instrumento. | Sustituir la lista por IDs tipados: `economic_asset_id`, `network_id`, `representation_id`, `instrument_id`, `listing_id` y `project_id` cuando correspondan. |
| XCR-03 | AS-003 conservaba `contract_address=NATIVE_ASSET`, en conflicto con el modelo tipado. | Caso documental aceptaría una representación ambigua o una dirección ficticia. | Actualizar AS-003 y armonizarlo con AS-016. |
| XCR-04 | La semántica antigua de `availability_time/knowledge_time` podía colapsar disponibilidad pública, acceso autorizado e ingestión interna. | Riesgo de look-ahead y de confundir reconstrucción retrospectiva con replay operacional. | Separar `public_availability_time`, `authorized_availability_time`, `system_ingestion_time`, `capture_time` y `processing_time`; definir `availability_time` solo como alias derivado con modalidad explícita. |
| XCR-05 | AS-020 y el registro de eventos de membresía no explicaban suficientemente la vía/población a la que se refiere `knowledge_time`. | Riesgo de retrotraer conocimiento. | Aclarar la vía/población de conocimiento y distinguirla de la ingestión interna. |

## 3. Decisión por requisito

| Requisito | Decisión | Estado permitido tras esta revisión | Motivo y evidencia pendiente |
|---|---|---|---|
| IIM-001 | REVISION_REQUIRED | DOCUMENTED; SPECIFICATION_DRAFTED | Las claves tipadas y namespaces están mejor definidos. Falta cerrar cardinalidades, restricciones de unicidad, estados/transiciones y reglas de resolución completas por familia de red con ejemplos verificables. |
| IIM-019 | REVISION_REQUIRED | DOCUMENTED; SPECIFICATION_DRAFTED | Las entidades y relaciones están identificadas, pero falta contrato interoperable completo para cardinalidades, temporalidad, relaciones multi-colateral, proxy/implementación y migraciones. |
| IIM-020 | REVISION_REQUIRED | DOCUMENTED; SPECIFICATION_DRAFTED | Se describen cobertura, eventos y tiempos, pero no se ha demostrado completitud para un alcance real ni reconstrucción histórica con evidencia suficiente de activos desaparecidos. |
| IIM-018 | REVISION_REQUIRED | DOCUMENTED; SPECIFICATION_DRAFTED | Se separan las modalidades temporales y controles por variable, pero falta cerrar controles obligatorios por finalidad, propagación formal de estados y validación con datos reales. |
| IIM-002 | BLOCKED para aprobar fuentes concretas | DOCUMENTED; SPECIFICATION_DRAFTED | No existen expedientes probatorios concretos examinados con términos/versiones y permisos por operación, finalidad, periodo y usuario autorizado. Mantener fuentes/operaciones no verificadas en TERMS_PENDING o RESTRICTED. |

**Ningún requisito pasa a `SPECIFIED`.** La revisión documental no satisface por sí sola el requisito de aprobación trazable por la gobernanza del proyecto.

## 4. Límites y no-conclusiones

- No se aprueba proveedor, producto de datos, endpoint, licencia ni operación de captura/retención/exportación.
- No se emite opinión jurídica.
- No se declara que los casos AS/DQ estén automatizados o ejecutados.
- No se declara completitud de un universo histórico real.
- No se declara validación empírica ni capacidad predictiva.
- No se aprueba un esquema físico de base de datos ni una implementación.
- Los cambios de esta rama son armonización documental y deben revisarse mediante PR antes de integrarse en `main`.

## 5. Criterios pendientes para una futura aprobación

1. Definir cardinalidades y restricciones de unicidad por entidad/relación, incluidas relaciones multi-subyacente y temporalidad.
2. Completar reglas verificables de resolución para familias de red y transiciones de proxy, fork, migración y redenominación.
3. Definir evidencia mínima y límites de cobertura para cada tipo/finalidad de universo; demostrar reconstrucción de fallidos/deslistados cuando exista fuente.
4. Formalizar tabla de controles obligatorios por variable/fuente/finalidad, reglas de propagación de UNKNOWN/QUARANTINED y condiciones de abstención.
5. Crear expedientes concretos de fuentes con evidencia verificable de términos y permisos para cada operación prevista.
6. Completar un registro de aprobación por requisito con revisor competente, decisión, fecha, versión, alcance, desviaciones y evidencia.

## 6. Resultado

La rama corrige contradicciones detectadas y actualiza la trazabilidad. No cierra el Issue #19 ni autoriza implementación, activación de fuentes, captura de datos o validación empírica.