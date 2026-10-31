# Propuesta de contrato: grafo de identidad y admisibilidad por finalidad

**ID:** IIE-IDENTITY-GRAPH-PURPOSE-001  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSAL / PENDING_REVIEW / NOT_APPROVED  
**Resuelve:** IDR-01, IDR-02 (parcialmente IDR-07, IDR-08, IDR-09)  
**Requisitos relacionados:** IIM-001, IIM-018, IIM-019  
**Issue coordinador:** #61

## 1. Objetivo

Definir un contrato conceptual verificable para:
1. entidades con identidad tipada y claves namespaced;
2. relaciones tipadas, temporales y sustentadas por evidencia;
3. afirmaciones atómicas cuya verdad puede ser evaluada por finalidad;
4. separación estricta entre identidad resuelta y aptitud para un uso.

Este documento es una propuesta para revisión. No modifica los requisitos IIM ni establece un esquema físico, API o implementación aprobados.

## 2. Principio arquitectónico

El sistema no debe producir un único booleano "asset valid". Debe producir resultados independientes y vinculados:

- **Entity identity:** qué entidad técnica/económica es, según una clave tipada.
- **Relationship resolution:** qué relación se afirma entre entidades, durante qué intervalo y con qué evidencia.
- **Evidence claim:** qué hecho concreto respalda cada fuente/evidencia y con qué calidad.
- **Purpose admissibility:** si la evidencia disponible basta para una finalidad explícita.
- **Decision eligibility:** resultado de módulos posteriores; nunca se deriva únicamente de identidad.

Una entidad puede estar RESOLVED aunque algunas relaciones o afirmaciones sigan UNKNOWN, DISPUTED o UNVERIFIED. No convertir el estado de una entidad en el estado de todas sus propiedades.

## 3. Catálogo de entidades

Cada entidad tiene un entity_id interno, globalmente único dentro del tipo/namespace definido y estable frente a cambios de parser. El ID no se deriva de ticker, nombre, endpoint o proveedor.

| entity_type | Clave natural conceptual | Notas |
|---|---|---|
| NETWORK | network_id | Instancia concreta de red/entorno/historia |
| ECONOMIC_ASSET | economic_asset_id | Derecho/exposición económica identificable; puede ser desconocido |
| ASSET_REPRESENTATION | representation_id | Token, activo nativo o representación protocolaria en una red |
| MARKET_INSTRUMENT | instrument_id | Contrato negociable con especificación completa |
| LISTING | listing_id | Episodio de admisión de un instrumento en un venue |
| VENUE | venue_id | Exchange/mercado/contraparte según tipología aprobada |
| PROJECT | project_id | Proyecto/protocolo asociado, no sustituto de activo |
| ACTOR | actor_id | Entidad técnica, organización, DAO, multisig u otra autoridad |
| SOURCE | source_id | Proveedor/producto/documento/endpoint como fuente de evidencia |

Los IDs internos son surrogate keys; las claves naturales compuestas se almacenan como identificadores externos tipados y verificables. Una clave natural no sustituye el ID interno cuando la entidad puede tener múltiples identificadores o cambiar de representación.

### 3.1 Reglas de separación

- Un proyecto no es un activo.
- Un actor/deployer no es automáticamente issuer, administrator, controller ni beneficiario.
- Una representación no es automáticamente el activo económico subyacente.
- Un listing no es un instrumento nuevo si solo se actualiza el estado del episodio; una nueva admisión debe generar un episodio versionado según reglas de venue.
- Una dirección, wallet, script, outpoint, mint authority o proxy admin no es por sí solo una entidad económica.
- Una representación puede existir sin que el activo económico subyacente sea identificable.
- No se exige que cada representación tenga un economic_asset_id resuelto.

## 4. Contrato de relación

Una relación es una afirmación dirigida entre endpoints tipados. No es una etiqueta informal ni una equivalencia implícita.

Campos mínimos propuestos:

| Campo | Semántica |
|---|---|
| relationship_id | Identificador estable de la afirmación/relación |
| relationship_version | Versión inmutable de su contenido |
| relationship_type | Vocabulario controlado |
| source_entity_ref | entity_type + entity_id |
| target_entity_ref | entity_type + entity_id |
| qualifiers | Datos tipados exigidos por el tipo de relación |
| valid_time | Intervalo en el que la relación aplica en el dominio |
| knowledge_time | Intervalo/instante en que el sistema conoció y conservó la afirmación |
| resolution_status | Estado de resolución de la afirmación |
| evidence_claim_refs | Referencias a afirmaciones/evidencias que la sustentan |
| source_method | Método, adaptador y versión |
| created_at / recorded_at | Tiempos de registro, no sustitutos de valid_time |

### 4.1 Vocabulario inicial y endpoints

| relationship_type | Source → Target | Qualifiers mínimos |
|---|---|---|
| REPRESENTS | ASSET_REPRESENTATION → ECONOMIC_ASSET | rol, proporción/condiciones si demostrables |
| WRAPS | ASSET_REPRESENTATION → ASSET_REPRESENTATION o ECONOMIC_ASSET | mecanismo, issuer, redemption terms |
| BRIDGES | ASSET_REPRESENTATION → ASSET_REPRESENTATION | bridge, origen/destino, lock/mint/burn, periodo |
| BACKED_BY | ASSET_REPRESENTATION o ECONOMIC_ASSET → ASSET_REPRESENTATION u otro colateral tipado | tipo de respaldo, composición, pesos si probados |
| MIGRATES_TO | ASSET_REPRESENTATION → ASSET_REPRESENTATION | ratio, ventana, elegibilidad, canje |
| FORKED_FROM | NETWORK o ASSET_REPRESENTATION → mismo tipo compatible | punto de divergencia y reglas |
| IMPLEMENTS | ASSET_REPRESENTATION/proxy → implementación tipada | code hash, mecanismo, bloque/slot |
| UPGRADED_TO | implementación/proxy → implementación/proxy | evento, autoridad, vigencia |
| ISSUED_BY | representación/instrumento → ACTOR | rol de emisión y evidencia |
| ADMINISTERED_BY | representación/contrato → ACTOR | authority type, alcance y periodo |
| CONTROLLED_BY | entidad técnica → ACTOR | tipo de control, evidencia y periodo |
| ASSOCIATED_WITH | PROJECT ↔ entidad tipada | naturaleza de asociación y fuente |
| LISTED_AS | MARKET_INSTRUMENT → LISTING | venue y periodo |
| BASE_ASSET | MARKET_INSTRUMENT → ASSET_REPRESENTATION | rol base |
| QUOTE_ASSET | MARKET_INSTRUMENT → ASSET_REPRESENTATION o unidad externa tipada | rol quote |

Este vocabulario es inicial. No se deben añadir relaciones genéricas como EQUIVALENT_TO sin definir qué equivalencia se afirma (identidad de representación, exposición, redención, precio, convertibilidad o derechos).

### 4.2 Cardinalidad y ciclos

No se aplicará una cardinalidad universal a todas las relaciones. Cada tipo tendrá reglas específicas:
- endpoints válidos;
- aridad y cardinalidad por intervalo;
- exclusividad temporal, si aplica;
- qualifiers obligatorios;
- ciclos permitidos, sospechosos o inválidos;
- evidencia mínima y condiciones de retiro.

Un ciclo de BACKED_BY puede ser legítimo o representar dependencias circulares que requieren riesgo; un ciclo de MIGRATES_TO en el mismo periodo puede ser sospechoso. El validador debe aplicar reglas por tipo, nunca rechazar ciclos de todo el grafo indiscriminadamente.

## 5. EvidenceClaim: afirmación atómica y trazable

Una fuente no demuestra "la identidad completa". Cada pieza de evidencia respalda una afirmación concreta.

Campos mínimos propuestos:
- claim_id y claim_version;
- subject_ref y predicate;
- object_ref/value tipado;
- claim_status: CONFIRMED, PARTIALLY_CONFIRMED, DISPUTED, UNVERIFIED, REJECTED, RETRACTED;
- valid_time y knowledge_time;
- source_id y source artifact/reference;
- source independence group cuando se conozca;
- observed_at, recorded_at, public/authorized availability cuando sea demostrable;
- method/adapter/version;
- confidence solo si existe calibración y definición aprobada; nunca sustituye status;
- limitations y contradiction_refs.

Ejemplos de claims separados:
- contrato desplegado en red X al bloque B;
- contrato tiene código con hash H en B;
- proxy apunta a implementación I en B;
- actor A puede actualizar el proxy en B;
- representación R tiene mecanismo de redención M;
- proveedor P declara reservas de cantidad Q;
- titular tiene derecho exigible a redención bajo condiciones C.

No inferir un claim desde otro. Existencia de contrato no demuestra estándar, derechos, solvencia, control actual ni legitimidad del emisor.

## 6. Estados independientes

### 6.1 EntityIdentityStatus

- RESOLVED
- RESOLVED_WITH_LIMITATIONS
- AMBIGUOUS
- UNVERIFIED
- CONFLICTED
- RETIRED_OR_MIGRATED

### 6.2 RelationshipResolutionStatus

- CONFIRMED
- PARTIALLY_CONFIRMED
- DISPUTED
- UNVERIFIED
- REJECTED
- RETIRED

### 6.3 EvidenceClaimStatus

- CONFIRMED
- PARTIALLY_CONFIRMED
- DISPUTED
- UNVERIFIED
- REJECTED
- RETRACTED

Los vocabularios de relación y claim pueden compartir términos, pero son objetos distintos. La traducción a estados de entidad solo se realiza cuando la afirmación afecta a la identidad de esa entidad; no propagar indiscriminadamente.

### 6.4 PurposeAdmissibilityStatus

- ADMISSIBLE
- ADMISSIBLE_WITH_LIMITATIONS
- INSUFFICIENT_EVIDENCE
- CONFLICTED
- NOT_APPLICABLE
- NOT_ASSESSED

El estado es siempre relativo a una finalidad, scope, contexto y tiempo concretos. NOT_APPLICABLE exige justificación explícita; no significa evidencia faltante.

## 7. Contrato de PurposeAssessment

Cada evaluación debe identificar:
- purpose_id y purpose_version;
- subject entity_ref;
- scope (red, representación, instrumento, venue, jurisdicción, actividad y tiempo pertinentes);
- required_claims y required_relationships;
- accepted source classes / evidence standards;
- freshness requirements y point-in-time cutoff;
- policy version;
- result status y limitations;
- blocking claim refs y missing evidence refs;
- evaluated_at y deterministic decision trace.

Finalidades iniciales candidatas:
- IDENTITY_DESCRIPTION: describir identidad técnica.
- ECONOMIC_LINKAGE: vincular representación a derecho/activo subyacente.
- CONTROL_ASSESSMENT: analizar autoridad/control.
- REDEMPTION_ASSESSMENT: analizar redención.
- BACKING_ASSESSMENT: analizar reservas/colateral.
- MARKET_INSTRUMENT_MAPPING: vincular instrumento y listing.
- HISTORICAL_RECONSTRUCTION: reconstruir lo conocido en fecha pasada.
- ANALYTICAL_SELECTION: aportar identidad y claims admisibles a selección analítica.
- OPERATIONAL_ROUTE: identificar representación/instrumento/ruta operativa; no equivale a autorización de trading.

Las finalidades deben versionarse y aprobarse por owner. No crear una finalidad implícita por omisión.

## 8. Reglas de composición y abstención

1. EntityIdentityStatus=RESOLVED no implica PurposeAdmissibilityStatus=ADMISSIBLE.
2. Un claim DISPUTED bloquea solo finalidades que lo requieren, salvo que comprometa la clave de identidad.
3. Un claim UNVERIFIED no puede convertirse en CONFIRMED por ausencia de contradicción.
4. Evidencia de metadata no demuestra derecho económico o control.
5. Un BRIDGES/WRAPS confirmado no demuestra backing, solvencia ni redemption.
6. Un listing vigente no demuestra liquidez ejecutable.
7. Una representación identificada puede seguir fuera de scope por falta de perfil de comportamiento.
8. Si falta un claim requerido, resultado mínimo INSUFFICIENT_EVIDENCE; no asumir favorable.
9. Si claims requeridos se contradicen, resultado CONFLICTED.
10. Un purpose assessment nunca reemplaza vetos de riesgo, elegibilidad, mandato o autorización operativa.

La composición debe ser determinista, explicable y conservar el conjunto exacto de claims/versions usado. Cualquier cambio de claim, scope, policy, source admissibility o temporalidad invalida el assessment dependiente.

## 9. Temporalidad y point-in-time

Se propone alinear las relaciones y claims con el marco lifecycle:
- valid_time: cuándo el hecho aplica en el dominio;
- public_availability_time: cuándo se puede demostrar que el dato era público;
- authorized_access_time: cuándo estaba disponible bajo acceso autorizado;
- observed_at: cuándo la fuente fue consultada/observada;
- recorded_at: cuándo el sistema registró la afirmación;
- ingested_at y processed_at: tiempos de pipeline;
- evaluated_at: tiempo de evaluación.

No sustituir un timestamp desconocido por block time, timestamp del proveedor ni tiempo de captura. Para reconstrucción histórica, solo se admiten claims cuya disponibilidad temporal sea demostrable para el cutoff y cuya procedencia sea admisible para esa finalidad.

La semántica de intervalos, precisión y límites temporales debe alinearse con el framework point-in-time antes de congelar el schema.

## 10. Ejemplos de separación correcta

| Caso | Identidad | Claim/relación | Finalidad |
|---|---|---|---|
| Token EVM con código probado, proxy admin desconocido | Representation RESOLVED | CONTROLLED_BY UNVERIFIED | IDENTITY_DESCRIPTION puede ser ADMISSIBLE; CONTROL_ASSESSMENT no |
| Wrapped token con bridge identificado, reservas no verificadas | Representation RESOLVED | BRIDGES CONFIRMED; BACKED_BY UNVERIFIED | IDENTITY_DESCRIPTION sí; BACKING_ASSESSMENT no |
| IBC voucher con denom local probado y trace ausente | Representation local RESOLVED | REPRESENTS/bridge linkage UNVERIFIED | Identidad local sí; ECONOMIC_LINKAGE no |
| Token Cardano con Asset ID exacto y metadata en conflicto | Representation RESOLVED | metadata claims DISPUTED | Identidad técnica sí; clasificación basada en metadata no |
| Mint Solana con program ID no soportado | Candidate identity potentially RESOLVED_WITH_LIMITATIONS | token behavior UNSUPPORTED | Descripción de cuenta limitada; transferibility assessment no |
| Contrato de mercado con ticker igual, vencimiento diferente | Instrument IDs distintos | contract specs separadas | No se fusionan por ticker |

## 11. Contratos y artefactos ejecutables que deberán diseñarse

No se crean en esta propuesta. Tras revisión, el trabajo de ingeniería podrá definir schemas versionados para:
1. EntityReference / EntityRecord.
2. RelationshipRecord.
3. EvidenceClaimRecord.
4. NetworkIdentityEvidenceRecord.
5. PurposeDefinition.
6. PurposeAssessment.
7. IdentityResolutionEvent / RelationshipResolutionEvent.
8. EvidenceDependency / invalidation links.

Cada schema requerirá:
- versionado y namespace estable;
- JSON Schema Draft 2020-12 o estándar aprobado;
- tipos exactos y unknown-field policy;
- invariantes cross-field;
- fixture set positivo/negativo;
- tests unitarios/property/adversariales;
- replay determinista;
- revisión independiente y aprobación.

## 12. Decisiones abiertas para revisión

| ID | Decisión | Propuesta | Estado |
|---|---|---|---|
| D-01 | IDs internos y claves naturales | IDs internos estables + claves naturales namespaced | PENDING_REVIEW |
| D-02 | Separación entidad/claim/relación/finalidad | Objetos y estados independientes | PENDING_REVIEW |
| D-03 | Finalidades MVP | IDENTITY_DESCRIPTION, ECONOMIC_LINKAGE, CONTROL_ASSESSMENT, MARKET_INSTRUMENT_MAPPING, ANALYTICAL_SELECTION | PENDING_REVIEW |
| D-04 | Spot vs derivados | No resolver aquí; requiere decisión de scope | OPEN |
| D-05 | Semántica temporal | Alinear con lifecycle/point-in-time antes de schema freeze | OPEN |
| D-06 | Cardinalidad por relación | Tabla normativa por relationship_type | OPEN |
| D-07 | Vocabularios y precedencia | No congelar sin revisión cruzada con decision-state model | OPEN |

## 13. Criterios de aceptación

- [ ] Aprobación conceptual del modelo entity/relationship/claim/purpose.
- [ ] Revisión de consistencia con estados canónicos, Scope Algebra y lifecycle/freshness.
- [ ] Reglas de cardinalidad por relación acordadas.
- [ ] Contrato temporal aprobado.
- [ ] Decisión de alcance spot/derivatives.
- [ ] Escenarios de aceptación y contraejemplos aprobados.
- [ ] Schemas diseñados en una fase posterior y revisados antes de implementarlos.
- [ ] Ningún requisito IIM se declara aprobado por la creación de este documento.

## 14. Estado

Esta propuesta responde conceptualmente a IDR-01 e IDR-02, pero no los cierra. No hay schemas, código, fixtures ni tests derivados. IIM-001, IIM-018 e IIM-019 mantienen sus estados previos hasta revisión y aprobación formal.
