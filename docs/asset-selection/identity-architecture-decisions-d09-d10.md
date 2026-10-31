# Resolución arquitectónica propuesta — D-09 y D-10

**ID:** IIE-IDENTITY-ARCH-DECISIONS-009  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED  
**Dependencias:** D-01..D-08; Identity Graph; Scope Algebra; FSA-01; Lifecycle; modelo canónico de decisiones; RACI/SoD  
**Issue:** #61

## 1. Propósito y límites

D-09 define cómo identificar de forma inequívoca un sujeto económico, su representación en una red concreta y los instrumentos/listings negociables relacionados. D-10 define el envelope integrado que determina si la información de identidad y sus claims son admisibles para una finalidad, scope, actividad y consumidor explícitos.

Este documento cierra el conjunto de diez decisiones a nivel de propuesta documental. No constituye aprobación institucional, schema freeze, implementación, pruebas ni validación empírica. No modifica requisitos IIM, documentos metodológicos canónicos, schemas, código, fixtures o tests.

## 2. D-09 — Resolución de identidad acotada por sujeto, red y representación

### 2.1 Principio

No existe una única identidad genérica de “moneda” que pueda utilizarse indistintamente para identidad económica, token on-chain, instrumento de mercado, listing y ruta operativa. La resolución produce identidades tipadas y relaciones explícitas, cada una con scope, tiempo, evidencia y estado propios.

La identidad de una entidad no se hereda automáticamente a otra relacionada:
- PROJECT no equivale a ECONOMIC_ASSET.
- ECONOMIC_ASSET no equivale a ASSET_REPRESENTATION.
- ASSET_REPRESENTATION no equivale a MARKET_INSTRUMENT.
- MARKET_INSTRUMENT no equivale a LISTING.
- LISTING no equivale a VENUE.
- NETWORK identifica una instancia y entorno concretos, no una familia genérica de chains.
- Ticker, nombre, logo o market-data provider ID no son claves de identidad suficientes.

### 2.2 Objetos y límites

| Objeto | Qué identifica | Atributos mínimos conceptuales |
|---|---|---|
| NETWORK | Instancia concreta de red/ledger y entorno | network_id, namespace, chain/genesis identity, environment, fork/history identity |
| ECONOMIC_ASSET | Unidad/derecho/exposición económica subyacente | economic_asset_id, asset class, issuer/obligation when relevant, defining rights/claims |
| ASSET_REPRESENTATION | Representación nativa o emitida en una red | representation_id, network_ref, canonical locator/type, deployment or native identifier |
| MARKET_INSTRUMENT | Contrato/activo negociable especificado | instrument_id, base/quote, contract type, settlement/delivery terms, multiplier/denomination |
| LISTING | Episodio de admisión de un instrumento en un venue | listing_id, instrument_ref, venue_ref, market identifier, quote/settlement, validity interval |
| VENUE | Mercado, exchange o contraparte | venue_id, legal/technical identity, jurisdiction and operational context |

El catálogo es conceptual; claves y subtipos definitivos requieren aprobación por dominio.

### 2.3 IdentitySubject e IdentityAssessment

IdentitySubject representa el sujeto exacto que se intenta resolver. IdentityAssessment es la evaluación versionada de su identidad, no un atributo mutable incrustado en el activo.

IdentitySubject incluye subject_id, subject_type/version, asserted identifiers y namespace, canonical entity ref si existe, network/environment, representation/instrument/listing refs según corresponda, identificadores naturales preservando el original, valid_time, scope y evidencia.

IdentityAssessment incluye:
- assessment_id/version y subject_ref/version;
- canonical_entity_ref/version;
- resultados separados para entity identity, network binding, representation identity, instrument mapping y listing mapping;
- estado por dimensión: RESOLVED, UNRESOLVED o CONFLICTED;
- scope_id/version y ScopeDefinition digest;
- valid_time, knowledge_cutoff y knowledge profile para evaluaciones históricas;
- claims requeridos, ClaimResolution y ClaimEvidence refs;
- EvidenceRecord, SourceAssessment y lineage;
- dependency graph snapshot;
- policy, predicate y adapter versions;
- reasons, limitations, conflicts, evaluated_at y lifecycle refs;
- provenance, evaluator/authority y reproducibility metadata.

No se comprime un conjunto de resultados dimensionales en un único RESOLVED si un vínculo material permanece UNRESOLVED o CONFLICTED. Un estado agregado para un consumidor solo puede derivarse mediante regla versionada que explicite dimensiones obligatorias.

### 2.4 Claves de representación por familia tecnológica

La identidad de representación usa una clave natural tipada adecuada a la familia de ledger, no un campo universal ambiguo como contract address.

| Familia | Identificador primario conceptual | Controles esenciales |
|---|---|---|
| EVM | Network instance + contract address; native asset usa definición nativa de red | chain/genesis identity, checksum, proxy/implementation, code hash, deployment block, token standard y upgrade history |
| Solana | Cluster/network instance + mint address | mint/freeze authority, token program ID/version, extensions, metadata provenance, deployment context |
| UTXO | Network instance + asset protocol/issuance identifier | outpoint/issuance/asset ID, reissuance/burn semantics, script/consensus rules, chain history |
| Cosmos/IBC | Chain instance + denom/trace path y origin context | chain ID + genesis identity, denom trace, channel/port path, upgrades/migrations |
| Cardano/eUTXO | Network instance + policy ID + asset name; native coin por separado | policy script/hash, asset name bytes, mint/burn policy, ledger era y network identity |
| Otros | Namespace-specific composite key bajo perfil versionado | perfil explícito, componentes obligatorios, canonicalization y validación |

Los ejemplos no constituyen cobertura completa ni prueban que los perfiles estén implementados.

### 2.5 Network identity, forks y entornos

Chain ID por sí solo puede no identificar una instancia de red suficientemente. Según tecnología, evaluar genesis hash o ancla equivalente, namespace/environment, protocolo/consensus identity, historial/fork identity, block anchor, upgrades, migraciones, finality/reorg assumptions y validez temporal.

Una bifurcación puede compartir historia parcial, pero debe representarse como instancia/historia diferenciada desde la divergencia relevante. No fusionar por chain name o chain ID. Un chain ID reutilizado no demuestra identidad de red.

### 2.6 Representaciones, wrappers, bridges y backing

Relaciones REPRESENTS, WRAPS, BRIDGES, BACKED_BY, MIGRATES_TO y REDEEMS_TO son edges tipados con sujeto/objeto, dirección semántica, cardinalidad, scope, tiempo, claims/evidence, lifecycle y condiciones de creación/cierre.

La existencia de WRAPS o BRIDGES no demuestra equivalencia económica, backing suficiente, solvencia, derecho legal de redención, disponibilidad de redención, convertibilidad 1:1 ni ausencia de riesgo de contraparte. Esas propiedades requieren claims y PurposeAssessments independientes. No heredar el estado del activo nativo al wrapped representation.

### 2.7 Proxies, upgrades y migraciones

El modelo distingue representación/deployment identity, proxy e implementation identity, upgrade/admin authority, code hash y configuration snapshot, effective block/time, migration source/destination y continuidad de derechos/estado económico.

Un upgrade no crea automáticamente un nuevo ECONOMIC_ASSET, pero puede cambiar la versión/estado de la representación y exigir nuevas evaluaciones de seguridad, control, tokenomics y operación. Una migración puede preservar, transformar o extinguir la representación anterior; requiere evidencia y regla de continuidad explícitas.

### 2.8 Instrumentos y listings

El mapping de mercado identifica base/underlying representation, quote asset/representation, instrument type (spot, perpetual, future, option, receipt u otro), multiplier, denomination, settlement, margin/collateral cuando aplique, venue, market identifier, listing episode, trading status, delisting/suspension y valid_time.

Un ticker compartido no fusiona instrumentos ni listings. Un activo puede tener varios instrumentos; un instrumento puede tener listings distintos por venue y periodo. Un relisting conserva historia y genera episodio/versionado según política. Identidad de instrumento/listing no demuestra liquidez suficiente ni ruta autorizada.

### 2.9 Resolución por dimensión y reglas de consumo

RESOLVED significa identidad/vínculo demostrado dentro del scope declarado; UNRESOLVED significa evidencia insuficiente; CONFLICTED significa evidencia admisible material contradictoria sin adjudicación. INVALID es error contractual y bloquea. STALE pertenece a freshness/lifecycle, no a IdentityStatus. NOT_APPLICABLE solo puede surgir de ApplicabilityAssessment por obligación.

La matriz de obligatoriedad se versiona por purpose/scope_kind/decision_type/activity. Por ejemplo:
- IDENTITY_DESCRIPTION puede requerir NETWORK + ASSET_REPRESENTATION.
- ECONOMIC_LINKAGE requiere ECONOMIC_ASSET ↔ ASSET_REPRESENTATION.
- MARKET_INSTRUMENT_MAPPING requiere representación/base/quote + instrument + listing/venue.
- ANALYTICAL_SELECTION_SUPPORT requiere identidad y mappings necesarios para la unidad de selección definida por metodología.
- ORE evalúa requisitos de ruta por separado y no hereda aprobación analítica.

Una dimensión opcional desconocida no bloquea automáticamente todos los usos; una dimensión obligatoria desconocida no puede ignorarse.

### 2.10 Escenarios D-09

| ID | Escenario | Resultado exigido |
|---|---|---|
| D09-01 | Dos tokens comparten ticker | Identidades separadas por red/representación |
| D09-02 | Mismo chain ID en redes distintas | Distinguir por genesis/network instance y entorno |
| D09-03 | Fork comparte historia previa | Historia diferenciada y temporalmente acotada |
| D09-04 | EVM proxy cambia implementation | Versionar implementation/config; reevaluar dependencias |
| D09-05 | Native asset sin contract address | Usar definición nativa de red |
| D09-06 | Token wrapped con bridge | Representación separada; no heredar backing/redemption |
| D09-07 | IBC denom con trace path | Identidad compuesta por chain context y denom trace |
| D09-08 | Solana mint bajo Token-2022 | Incluir program ID y extensiones relevantes |
| D09-09 | UTXO asset con reissuance | Aplicar perfil issuance/consensus; no modelo ERC-20 |
| D09-10 | Relistado tras delisting | Preservar listing episode e historia |
| D09-11 | Spot y perpetual comparten símbolo | Instrumentos distintos por términos/settlement |
| D09-12 | Migración con continuidad disputada | UNRESOLVED/CONFLICTED hasta probar continuidad |
| D09-13 | Entidad resuelta, network binding no | No emitir representación RESOLVED |
| D09-14 | Representación resuelta, vínculo económico desconocido | Puede admitirse descripción; no economic linkage |
| D09-15 | Fuente identifica contrato, code/deployment evidence difiere | CONFLICTED/review |
| D09-16 | Listing suspendido | Identidad de instrumento permanece; estado listing/operativo separado |

## 3. D-10 — PurposeAssessment integrado

### 3.1 Principio: evaluación contextual con ejes ortogonales

PurposeAssessment responde si identidades, claims y evidencias son admisibles para un uso exacto, scope, actividad, tiempo y consumidor. No es propiedad global del activo ni un booleano. No es SelectionDecision, RiskAssessment, VetoDecision, RouteEligibility ni autorización de ejecución.

Debe distinguir los ejes de D-01:
- scope_kind: clase del scope/dominio de decisión;
- decision_type: decisión consumidora;
- purpose: finalidad informacional/analítica;
- activity: actividad evaluada;
- scope: sujeto, representación, red, instrumento, venue, jurisdicción, tiempo y demás dimensiones.

OPERATIONAL_ROUTE no se registra como purpose. Es dominio/scope/decision consumer según catálogo canónico. ANALYTICAL_SELECTION_SUPPORT se registra según matriz aprobada, sin duplicar implícitamente purpose y decision_type.

### 3.2 Catálogo inicial de purposes propuesto

| Purpose | Alcance informacional | No autoriza por sí mismo |
|---|---|---|
| IDENTITY_DESCRIPTION | Describir sujeto, red y representación | Economic linkage o inversión |
| ECONOMIC_LINKAGE | Sustentar relación representación–activo económico | Backing, solvencia o redención |
| CONTROL_ASSESSMENT | Evaluar autoridad, control y derechos administrativos | Seguridad absoluta o legalidad |
| BACKING_ASSESSMENT | Evaluar reservas/colateral y cobertura | Redención efectiva o solvencia |
| REDEMPTION_ASSESSMENT | Evaluar mecanismos y condiciones de redención | Ejecución/custodia |
| MARKET_INSTRUMENT_MAPPING | Relacionar activo, instrumento, listing y venue | Liquidez suficiente o ruta autorizada |
| HISTORICAL_RECONSTRUCTION | Reconstruir información con perfil/cutoff explícitos | Conocimiento público no demostrado |
| ANALYTICAL_SELECTION_SUPPORT | Proveer inputs de identidad/evidencia a selección | Selección final ni recomendación personalizada |

El catálogo debe versionarse, tener owner y criterios de entrada/salida. Los propósitos adicionales requieren aprobación; no se infieren por omisión.

### 3.3 PurposeAssessment envelope

Cada expediente debe contener:
1. IdentityAssessment refs/versiones y dimensiones resueltas.
2. Context: subject, scope_kind, decision_type, purpose, activity y ScopeDefinition/version/digest.
3. Time: valid_time, knowledge_cutoff, knowledge profile, freshness policy y snapshot.
4. Applicability: catálogo completo y versionado de obligaciones; ApplicabilityAssessment por obligación.
5. Claims: ClaimEnvelope/ClaimResolution refs, PredicateContract y versiones.
6. Evidence: EvidenceRecord, SourceAssessment, ClaimEvidenceAssessment y lineage.
7. Logic: FSA-01 MembershipEvaluation refs y proof bundles.
8. Dependencies: grafo versionado, DependencyEdge y completeness/impact status.
9. Coverage: expected, applicable, excluded, unresolved obligations y residuals.
10. Outcome: purpose admissibility, limitations, blockers y reason codes.
11. Lifecycle: workflow, freshness y lifecycle como ejes separados.
12. Governance: policy versions, evaluator, reviewer, authority, SoD y approvals.
13. Reproducibility: input manifest, engine/adapter versions, digest, timestamps y replay reference.

### 3.4 Estados separados

| Eje | Vocabulario propuesto | Semántica |
|---|---|---|
| Assessment workflow | NOT_STARTED, IN_PROGRESS, COMPLETE, REVIEW_REQUIRED | Progreso del trabajo |
| Purpose admissibility | ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, INSUFFICIENT_EVIDENCE, CONFLICTED, NOT_ASSESSED | Conclusión para el propósito |
| Applicability | APPLICABLE, NOT_APPLICABLE, UNDETERMINED, INVALID | Resultado por obligación; INVALID es error contractual |
| Freshness | FRESH, APPROACHING_EXPIRY, STALE, UNKNOWN, NOT_APPLICABLE | Vigencia temporal |
| Lifecycle | ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN | Vigencia del registro |
| Scope validation | VALID, REVIEW_REQUIRED, INCOMPLETE, REJECTED_INVALID_INPUT | Validez/cobertura del scope |
| Dependency completeness | COMPLETE, INCOMPLETE, UNRESOLVED, INVALID | Cobertura del grafo |
| Claim resolution | CONFIRMED, REFUTED, UNKNOWN, CONFLICTED, NOT_ASSESSED, RETRACTED | Resolución epistemológica |
| Membership logic | TRUE, FALSE, UNKNOWN; validity INVALID separado | Pertenencia FSA-01 |

NOT_APPLICABLE no es resultado de PurposeAdmissibility; solo pertenece a ApplicabilityAssessment por obligación. STALE no es IdentityStatus ni ClaimResolutionStatus. INVALID no es conclusión factual ni resultado de admisibilidad.

### 3.5 Completitud de obligaciones y regla de vacuidad

Para cada combinación purpose + scope_kind + decision_type + activity + subject type, un catálogo versionado define el universo esperado de obligaciones. Cada obligación debe tener exactamente una ApplicabilityAssessment válida: APPLICABLE, NOT_APPLICABLE con justificación positiva, UNDETERMINED o INVALID.

Las obligaciones no pueden desaparecer. El envelope demuestra catálogo completo y ausencia de obligaciones sin evaluar.

- Si existe obligación APPLICABLE no satisfecha o con evidencia insuficiente, no emitir ADMISSIBLE.
- Si existe obligación UNDETERMINED, no emitir ADMISSIBLE; usar INSUFFICIENT_EVIDENCE o REVIEW_REQUIRED según policy.
- Si existe conflicto material no resuelto, resultado CONFLICTED/REVIEW_REQUIRED; no promediarlo.
- INVALID en obligación requerida/envelope invalida el expediente y bloquea consumo.
- Si todas son NOT_APPLICABLE, no emitir ADMISSIBLE por vacuidad. Solo se permite ASSESSMENT_NOT_REQUIRED como process outcome separado si una policy explícita lo autoriza; no es PurposeAdmissibility ni aprobación favorable.
- Si catálogo/coverage no es completo, no se demuestra que las obligaciones estén satisfechas.

### 3.6 Reglas de composición

ADMISSIBLE requiere simultáneamente:
1. Scope y envelope válidos y completos.
2. Identidad resuelta en todas las dimensiones obligatorias.
3. Applicability resuelta para todo el catálogo esperado.
4. Cada obligación aplicable satisfecha con claims/evidencia admisibles y frescos.
5. Claims requeridos sin conflictos materiales ni retractaciones sin sustitución.
6. Dependencias materiales completas y evaluadas.
7. Membership/proof obligations válidas cuando el purpose dependa de ellas.
8. Ausencia de restricciones legales, de licencia o política bloqueantes.
9. Trazabilidad y reproducibilidad suficientes.
10. Governance, autoridad y segregación de funciones satisfechas.

ADMISSIBLE_WITH_LIMITATIONS solo se permite cuando la policy enumera limitaciones, scope, impacto, owner, expiry/review trigger y usos permitidos/prohibidos. No oculta incumplimientos obligatorios.

INSUFFICIENT_EVIDENCE representa datos/cobertura/claims insuficientes. CONFLICTED representa contradicción material admisible no adjudicada. NOT_ASSESSED significa que no se ha emitido evaluación concluida.

### 3.7 Matriz mínima de combinaciones

| Condición | Outcome permitido |
|---|---|
| Scope válido, catálogo completo, obligaciones satisfechas, dependencias completas, sin conflictos | ADMISSIBLE, sujeto a governance |
| Condición anterior con limitaciones permitidas y explícitas | ADMISSIBLE_WITH_LIMITATIONS |
| Obligación aplicable sin evidencia suficiente | INSUFFICIENT_EVIDENCE |
| Applicability UNDETERMINED en obligación requerida | No ADMISSIBLE; INSUFFICIENT_EVIDENCE/REVIEW_REQUIRED |
| Claim material CONFLICTED | CONFLICTED |
| Scope INCOMPLETE/REVIEW_REQUIRED | No ADMISSIBLE |
| Dependency graph incompleto/material | No ADMISSIBLE; revisión/evidencia insuficiente |
| Input requerido INVALID | Envelope inválido; no consumible |
| Todas las obligaciones NOT_APPLICABLE | No ADMISSIBLE; process outcome separado solo si autorizado |
| Workflow NOT_STARTED/IN_PROGRESS | NOT_ASSESSED |
| Input obligatorio STALE | No ADMISSIBLE para ese use/cutoff |
| Lifecycle SUPERSEDED/INVALIDATED/WITHDRAWN | No consumible como evaluación actual |
| Obligación opcional desconocida | Solo tolerable con regla explícita y limitación |

### 3.8 Contrato de consumo por decisión

| Consumidor | Qué puede consumir | Qué no puede inferir |
|---|---|---|
| Identity description | IDENTITY_DESCRIPTION admisible | Economic linkage/backing |
| Economic linkage | ECONOMIC_LINKAGE admisible | Solvencia/redención |
| Analytical Selection | ANALYTICAL_SELECTION_SUPPORT admisible + identidad requerida | Riesgo aceptable, valoración atractiva o decisión final |
| Risk Assessment | Claims/assessments relevantes con scope/freshness | Ausencia de riesgo por ausencia de evidencia |
| Veto Decision | Evidencia/predicates aplicables al veto y autoridad | Levantamiento por otro purpose |
| ORE / Route Eligibility | Identidad exacta de representación/instrument/venue/route y assessments operativos | Heredar elegibilidad analítica como autorización |
| Portfolio/Execution | Outputs autorizados y controles de riesgo/ruta | Interpretar PurposeAssessment como orden/autorización |

PurposeAssessment es input de consumidores; nunca reemplaza sus propias decisiones y controles.

### 3.9 Escenarios D-10

| ID | Escenario | Resultado exigido |
|---|---|---|
| D10-01 | Identity description resuelta, economic linkage desconocido | Admitir descripción si satisface su catálogo; no linkage |
| D10-02 | Analytical support admisible, riesgo no evaluado | No inferir SelectionDecision favorable |
| D10-03 | Obligación aplicable sin evidencia | INSUFFICIENT_EVIDENCE |
| D10-04 | Obligación NOT_APPLICABLE justificada | Excluir solo esa obligación y conservarla |
| D10-05 | Todas las obligaciones NOT_APPLICABLE | No ADMISSIBLE por vacuidad |
| D10-06 | Applicability UNDETERMINED | No resultado favorable |
| D10-07 | Catálogo esperado incompleto | Envelope INCOMPLETE |
| D10-08 | Claim requerido CONFLICTED | Purpose CONFLICTED/review |
| D10-09 | Assessment FRESH pero lifecycle SUPERSEDED | No consumible como actual |
| D10-10 | Assessment COMPLETE pero input obligatorio STALE | No ADMISSIBLE para ese uso/cutoff |
| D10-11 | Scope analítico válido, route scope inválido | No autoriza ORE |
| D10-12 | Backing assessment satisfecho | No inferir redemption o solvency |
| D10-13 | Limitación afecta una representación | Limitar exactamente ese scope |
| D10-14 | Obligación opcional no evaluada | Tolerar solo con regla explícita y limitación |
| D10-15 | Envelope con referencia inválida | INVALID; bloquear consumo |
| D10-16 | Mismos snapshots/policies/engine | Resultado reproducible |
| D10-17 | Policy cambia | Nueva evaluación; preservar anterior |
| D10-18 | Evaluador aprueba su propia excepción | Bloquear por SoD salvo emergencia autorizada |

## 4. Integración D-01 a D-10

| Decisión | Contrato | Dependencias principales |
|---|---|---|
| D-01 | Separación scope_kind / decision_type / purpose / activity | Scope Algebra, canonical decision model |
| D-02 | IdentityStatus vs freshness/lifecycle | Lifecycle, canonical state model |
| D-03 | ApplicabilityAssessment | Taxonomy, obligations catalog |
| D-04 | ClaimEnvelope / PredicateContract | FSA-01, claims |
| D-05 | Source/Evidence/Assessments | Data governance, legal, security |
| D-06 | Adapter to FSA-01 | PredicateContract, Scope Algebra |
| D-07 | Point-in-time temporal model | Evidence, lifecycle, historical replay |
| D-08 | Dependency propagation | Lifecycle, Scope Algebra, RACI |
| D-09 | Scoped entity/representation/instrument identity | Identity graph, chain profiles, D-07 |
| D-10 | Integrated PurposeAssessment envelope | All above, decision consumers, RACI |

## 5. Riesgos y decisiones humanas pendientes

1. Aprobar taxonomía y claves por familia de red, incluidas forks, migrations, proxies y native assets.
2. Asignar owners para network identity registry y entity resolution adjudication.
3. Resolver alcance de economic asset para stablecoins, wrapped assets, governance tokens, receipts, LP tokens, derivatives y claims contractuales.
4. Aprobar matriz de dimensiones obligatorias por purpose/scope/decision/activity.
5. Aprobar catálogo de obligaciones y su versionado.
6. Definir autoridad para adjudicar claims conflictivos y excepciones.
7. Aprobar política de limitaciones, expiry y uso downstream.
8. Definir perfiles históricos y licencias para snapshots.
9. Aprobar matriz de compatibilidad de estados.
10. Revisar con expertos de redes/activos, Data Governance, Risk, Security, Legal y Operations.

## 6. Criterios de aprobación

- [ ] Catálogo de entidades y relaciones aprobado.
- [ ] Network identity y perfiles por familia tecnológica aprobados.
- [ ] Identidad de forks, proxies, upgrades y migrations aprobada.
- [ ] Distinción económica/representación/instrument/listing aprobada.
- [ ] Obligaciones y dimensiones requeridas por purpose/scope aprobadas.
- [ ] Vocabularios ortogonales y matriz de estados aprobados.
- [ ] Regla de vacuidad y catálogo completo aprobados.
- [ ] Contratos de consumo AE/ORE/Risk/Veto/Portfolio aprobados.
- [ ] RACI/SoD y owners asignados.
- [ ] D09-01..16 y D10-01..18 convertidos en fixtures/tests.
- [ ] Schemas versionados y validados después de aprobación.
- [ ] Implementación, CI y pruebas independientes completadas antes de declarar IMPLEMENTED/TESTED.

## 7. Estado final

**PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED.**

D-09 y D-10 quedan desarrolladas documentalmente con 34 escenarios. El bloque D-01..D-10 está documentado, pero no aprobado institucionalmente ni implementado. No se modificaron documentos canónicos, requisitos IIM, schemas, código, fixtures ni tests. No se ejecutaron pruebas ni validación formal/empírica.
