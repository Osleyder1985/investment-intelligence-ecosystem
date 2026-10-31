# Registro de revisión cruzada fundacional — Issue #19

**Estado:** revisión técnica-documental asistida; no constituye aprobación metodológica independiente.  
**Fecha de revisión:** 2026-10-31 (UTC)  
**Rama de trabajo inicial:** `docs/issue-19-cross-document-consistency`  
**Iteración de cardinalidades:** `docs/issue-19-identity-cardinality-contract`  
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

## 7. Iteración de cardinalidades y restricciones — borrador v0.3.0

**Alcance añadido:** sección 11 de `asset-identity-resolution.md` y casos AS-024 a AS-032 en `universe-test-cases.md`.

| ID | Decisión documental | Límite pendiente |
|---|---|---|
| XCR-06 | Se especifica que una representación pertenece a una sola instancia de red; una red puede contener múltiples representaciones. | Validar reglas de identidad y normalización por adaptador de red. |
| XCR-07 | Se separan instrumento y episodio de listing; un listing referencia un instrumento y pueden existir múltiples episodios históricos. | Precisar contratos específicos de venues, mercados spot/derivados y reutilización de IDs. |
| XCR-08 | Se evita imponer relación 1:1 entre activo económico y representación; se admiten composiciones multi-subyacente con pesos UNKNOWN si no hay evidencia. | Definir semántica y medición de derechos/participaciones para cada clase de activo. |
| XCR-09 | Se formalizan aristas con tipo, endpoints tipados, vigencia, conocimiento, evidencia y estado. | Acordar vocabulario controlado, serialización y reglas de compatibilidad entre versiones. |
| XCR-10 | Se separan emisor, administrador, propietario y controlador; se permiten ciclos solo según semántica del tipo de relación. | Probar detección de conflictos/ciclos con grafos reales y revisar excepciones por protocolo. |

### Decisión por requisito tras esta iteración

- **IIM-001: REVISION_REQUIRED.** El borrador define restricciones conceptuales, pero no cierra adaptadores ni casos verificables por todas las familias de red.
- **IIM-019: REVISION_REQUIRED.** El borrador aporta cardinalidades y semántica relacional, pero no constituye todavía un contrato interoperable aprobado ni validado.
- **IIM-020, IIM-018: REVISION_REQUIRED.** Esta iteración no resuelve completitud histórica ni controles point-in-time.
- **IIM-002: BLOCKED para aprobar fuentes concretas.** No se aportaron expedientes probatorios de términos y permisos.

### No-conclusiones

No se definió esquema físico de base de datos, algoritmo universal de matching, umbrales de confianza, adaptadores productivos ni cardinalidades empíricas para todos los protocolos. AS-024 a AS-032 son casos de diseño; no se automatizaron ni ejecutaron. No se realizó validación con datos reales, revisión jurídica ni aprobación independiente. Los cinco requisitos conservan sus estados anteriores.


## 8. Perfiles EVM y Solana — borrador v0.1.0

**Artefacto:** [network-identity-profiles-evm-solana](../asset-selection/network-identity-profiles-evm-solana.md).  
**Casos añadidos:** AS-033 a AS-042.

| ID | Regla documentada | Límite pendiente |
|---|---|---|
| XCR-11 | EVM: chain_id no es global; se combina con genesis, checkpoints, endpoint y evidencia de fork. | Probar resolución ante clones, endpoints inconsistentes y redes con historia compartida. |
| XCR-12 | EVM: representación token por network_id + 20 bytes; EIP-55 es formato/validación, no identidad distinta. | Fixtures de checksum, direcciones no-contract y comportamiento no estándar. |
| XCR-13 | EVM: proxy separado de implementación, con observaciones temporales de upgrade. | Fixtures de patrones proxy, admin y upgrades; no existe detector universal validado. |
| XCR-14 | Solana: mint por network_id + public key de 32 bytes, verificando cuenta, inicialización y owner program. | Fixtures para Token Program clásico, Token-2022 y cuentas inválidas. |
| XCR-15 | Solana: SOL nativo separado de WSOL; extensiones Token-2022 y autoridades son atributos críticos versionados. | Parser de extensiones completo y validación de comportamiento por versión de programa. |

### Decisión por requisito

- **IIM-001: REVISION_REQUIRED.** EVM y Solana cuentan con perfiles documentales; no se han validado adaptadores ni cubierto todas las familias de red.
- **IIM-019: REVISION_REQUIRED.** Las relaciones y atributos de red están mejor concretados, pero falta revisión independiente y validación interoperable de las relaciones complejas.
- **IIM-020 e IIM-018: REVISION_REQUIRED.** Esta iteración no demuestra cobertura histórica ni controles point-in-time.
- **IIM-002: BLOCKED para aprobar fuentes concretas.** No se aportan expedientes de permisos/términos de proveedores.

### No-conclusiones

Los perfiles son borradores de especificación, no software ejecutable. AS-033 a AS-042 no se han automatizado ni ejecutado. No se probaron endpoints, snapshots ni parsers; no se hizo revisión jurídica ni aprobación metodológica independiente. No se afirma cobertura completa de EVM o Solana.
