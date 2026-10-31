# X-09 — Álgebra formal de scopes y partición atómica

**ID:** IIE-METH-SCOPE-ALGEBRA-013  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Ámbito:** Modelo semántico de scopes, operaciones de conjuntos, relaciones, dependencias y validación de particiones para composición analítica y operativa.  
**Dependencias:** X-05 (documento 10), X-06 (documento 11), X-09 (documentos 14, 16, 17 y 18), M-03 (documento 04), X-07 (documento 12).  
**No alcance:** No crea JSON Schemas, código, políticas legales, reglas de autorización ni requisitos IIM.

## 1. Dictamen

X-06 exige scopes explícitos y unidades atómicas homogéneas; X-09 INV-33 exige que las unidades cubran exactamente el requested_scope y sean disjuntas. X-05 define dimensiones y propagación mínima, pero todavía no existe un álgebra formal común que permita demostrar esas propiedades.

Esta propuesta define un modelo de scope como conjunto tipado de puntos de un espacio de decisión, con dimensiones explícitas, semántica de valores desconocidos, operaciones parciales y validación de particiones. Las operaciones solo son válidas si los operandos comparten semántica de dimensión y versiones de normalización compatibles.

**Principio central:** ausencia de una dimensión, identificador o relación no significa ALL, ANY ni conjunto vacío. Significa UNRESOLVED, salvo que se declare explícitamente NOT_APPLICABLE o un dominio universal aprobado.

Resultados posibles de una operación:
- RESOLVED: el conjunto resultante está determinado.
- EMPTY: se ha demostrado que el conjunto es vacío.
- UNRESOLVED: no puede determinarse por identidad, jerarquía, unidad, alias, relación o evidencia insuficiente.
- INVALID: el input viola el contrato estructural o semántico.

UNRESOLVED nunca puede tratarse como EMPTY ni como ausencia de afectación.

## 2. Definiciones

| Término | Definición |
|---|---|
| Scope | Conjunto tipado de situaciones u objetos a los que se refiere una evaluación, regla, evidencia o decisión |
| Requested scope | Alcance exacto solicitado por el consumidor; identidad y versión inmutables |
| Atomic scope | Región mínima no vacía y homogénea respecto de inputs, reglas y consecuencias materiales |
| Scope dimension | Eje tipado que restringe el conjunto: sujeto, finalidad/actividad, ruta, jurisdicción, tamaño, tiempo, entorno, etc. |
| Scope predicate | Condición normalizada que determina pertenencia a un scope |
| Scope relation | Relación tipada entre dos scopes |
| Dependency edge | Relación causal/operativa explícita entre objetos o dimensiones, con evidencia, tipo y versión |
| Derived scope | Nuevo scope creado mediante operación explícita y decision record separado |
| Universe | Dominio válido para una dimensión y contexto/versionado determinados |

Un scope no es una etiqueta textual, un nombre de activo ni un booleano. La comparación requiere identificadores canónicos y dominios tipados.

## 3. Modelo canónico

### 3.1 Identidad y metadatos

Cada Scope futuro debe tener, como mínimo:

- scope_id estable, globalmente único e inmutable;
- scope_schema_version y normalization_version;
- scope_kind: ANALYTICAL, OPERATIONAL_ROUTE, RISK, VETO, EVIDENCE, VALUATION u otro catálogo aprobado;
- decision_type y purpose o activity según corresponda;
- subject_selector con IDs canónicos y tipo de objeto;
- dimensions con valor, unidad, operador y semántica;
- valid_time y, cuando aplique, observed_at/recorded_at;
- environment cuando afecte el alcance;
- parent_scope_id/derived_from solo cuando exista relación derivativa explícita;
- provenance: issuer, policy/rule version, evidence refs y snapshot;
- completeness y resolution_status por dimensión;
- digest semántico reproducible.

El scope_id no cambia si se actualiza evidencia externa; se crea una nueva versión/registro de evaluación y, cuando cambie el predicado, un nuevo scope_id o versión según política aprobada. La regla exacta de identidad/versionado requiere aprobación de Architecture.

### 3.2 Dimensiones

| Dimensión | Ejemplos | Semántica obligatoria |
|---|---|---|
| Subject | canonical_asset_id, representation_id, contract_address+chain_id, protocol_id, issuer_id, venue_id, account_id | Tipo de objeto + identificador canónico; ticker no basta |
| Representation | native, wrapped, bridged, synthetic, receipt, derivative | Relación explícita con underlying/issuer/bridge; no herencia automática |
| Purpose | DISCOVERY, RESEARCH, CLASSIFICATION, FUNDAMENTALS, TOKENOMICS, TECHNOLOGY, MARKET_ANALYSIS, RISK_ASSESSMENT, VALUATION, COMPARISON, BACKTEST | Enum versionado; no intercambiable con activity |
| Activity | MARKET_DATA, TRADE, DEPOSIT, WITHDRAWAL, TRANSFER, CUSTODY, REDEMPTION | Acción concreta; una actividad no implica otra |
| Route | venue, counterparty, instrument, account/subaccount, custodian, network, bridge, wallet, endpoint | Tupla con cada componente requerido por la actividad |
| Jurisdiction | jurisdiction_id, person/entity, conduct/activity | Fuente competente, sujeto y conducta; no inferir por ubicación de servidor |
| Quantitative constraints | size, notional, currency, side, leverage, fee, slippage | Tipo, unidad, moneda, inclusividad y normalización |
| Temporal | valid_from, valid_to, event_time, evaluation_time | Intervalos con timezone y precisión explícitas |
| Environment | RESEARCH, SIMULATION, PAPER, PRODUCTION | Entorno independiente; no promover entre entornos |
| Data context | source_id, feed_id, methodology_version, model_version | Contexto de evidencia/consumidor cuando sea material |
| Dependency | object IDs y edge IDs causales | Grafo versionado; no inferir por similitud |

Cada dimensión se declara como:
- CONSTRAINED: predicado conocido.
- UNCONSTRAINED_BY_SCOPE: incluye todos los valores del universo aprobado de esa dimensión.
- NOT_APPLICABLE: dimensión sin significado para este scope_kind, con justificación.
- UNRESOLVED: valor o cobertura desconocidos.

UNCONSTRAINED_BY_SCOPE no significa universal fuera del universo, contexto, finalidad o vigencia declarados. Null, campo omitido, cadena vacía o * no representan ALL.

### 3.3 Espacio de decisión y firmas

Conceptualmente, un scope es un subconjunto de un producto cartesiano tipado:

S ⊆ D_subject × D_purpose/activity × D_route × D_jurisdiction × D_constraints × D_time × D_environment

No todas las dimensiones aplican a todos los scope_kind. La firma de dimensiones y el universo deben estar fijados por catálogo. Dos scopes con firmas distintas no son automáticamente comparables ni combinables.

Para scope analítico, route puede ser NOT_APPLICABLE. Para scope operacional, la ruta concreta requerida no puede omitirse ni convertirse en unconstrained. Purpose y activity son ejes distintos.

## 4. Canonicalización y comparación

### 4.1 Identificadores y alias

1. Ticker, nombre, slug, address sin chain y nombre de exchange no son claves canónicas.
2. Alias se resuelve mediante registro versionado a ID canónico, con evidencia, vigencia y estado.
3. Alias ambiguo, reutilizado o conflictivo produce UNRESOLVED/INVALID, no unión silenciosa.
4. Contrato requiere chain/network ID + address normalizada + tipo de objeto.
5. Relación parent/child solo existe si el registro de identidad la declara.
6. Cambios de canonicalización deben permitir replay histórico.

### 4.2 Relaciones entre scopes

- EQUAL: predicados equivalentes en todas las dimensiones aplicables.
- CONTAINS(A,B): todo punto de B pertenece a A.
- WITHIN(A,B): A está contenido en B.
- DISJOINT: intersección demostrablemente vacía.
- OVERLAPS: intersección demostrablemente no vacía y ninguno contiene al otro.
- PARTIAL_ORDER: relación jerárquica conocida, sin prueba de contención completa.
- UNRESOLVED: comparación depende de dimensión o relación no resuelta.
- INVALID: contratos incompatibles o malformados.

La comparación solo devuelve DISJOINT si puede probar que al menos una dimensión común y relevante es disjunta y que identidades/contextos son compatibles. La ausencia de coincidencia textual no prueba disjunción.

### 4.3 Tipos y operadores

- Conjuntos enumerados: operaciones sobre IDs canónicos.
- Identificadores jerárquicos: contención solo con árbol/relación versionada; prefijos no implican ancestro.
- Intervalos numéricos: unidad, precisión, inclusividad y moneda; normalizar antes de operar.
- Intervalos temporales: convención half-open [start,end) propuesta; timezone y calendario explícitos.
- Tamaño/notional: side, moneda y unidad; no comparar USD con unidades nativas sin conversión fijada en snapshot.
- Predicate expressions: solo operadores permitidos por lenguaje versionado; no código arbitrario.

## 5. Operaciones algebraicas

Las operaciones son parciales: solo producen conjunto exacto cuando todos los operandos son comparables y resolubles.

### 5.1 Intersección

A ∩ B representa puntos incluidos en ambos scopes.
- EMPTY solo si la intersección es demostrablemente vacía.
- RESOLVED si la región común está determinada.
- UNRESOLVED si una dimensión material no puede compararse.
- La intersección conserva ambos orígenes, reglas, causas y lineage.

### 5.2 Unión

A ∪ B representa puntos incluidos en A o B.
- Puede conservarse como unión disjunta de componentes.
- No ampliar a ancestro común salvo equivalencia demostrada y universo cerrado.
- Unión de scopes de distintos purpose/activity no produce scope homogéneo.
- La unión no implica misma elegibilidad ni consecuencia.

### 5.3 Diferencia

A \ B representa puntos de A que no pertenecen a B.
- Solo es resoluble si puede identificarse exactamente el complemento de B dentro de A.
- Si B es UNRESOLVED o su cobertura incompleta, la diferencia es UNRESOLVED.
- El resultado describe un conjunto residual; no significa elegible.

### 5.4 Contención y equivalencia

Contención requiere prueba para todos los elementos, no coincidencia de una muestra. Equivalencia requiere mismo conjunto semántico, contexto, universo y vigencia; compartir scope_id textual no basta si versiones o predicados difieren.

### 5.5 Operaciones prohibidas

- UNRESOLVED convertido en EMPTY.
- NOT_APPLICABLE interpretado como ALL.
- Unión de rutas heterogéneas presentada como una ruta.
- Diferencia por exclusión de registros sin universo cerrado.
- Herencia automática entre native/wrapped, venue/market, cuenta/custodio o actividad.
- Propagación transitiva sin regla y edges aprobados.
- Comparación de unidades, monedas, jurisdicciones o tiempos incompatibles sin normalización.

## 6. Relación de afectación de veto/evidencia

Cada causa se compara con el scope del consumidor:

| Resultado | Significado | Consecuencia conceptual |
|---|---|---|
| AFFECTED | Consumer scope cae dentro del alcance causal demostrado | Aplicar consecuencia de regla/policy |
| NOT_AFFECTED_BY_THIS_RULE | No coincidencia demostrada con esta regla | No propagar esta regla; no significa libre de otros riesgos |
| PARTIALLY_AFFECTED | Solo una región coincide | Particionar y evaluar por región |
| SCOPE_UNRESOLVED | No puede probarse coincidencia ni no coincidencia | No emitir resultado favorable para la parte dependiente |
| INVALID_SCOPE | Scope o referencias inválidas | Rechazar composición |

La álgebra identifica la región afectada; la policy determina BLOCK, REVIEW, ABSTAIN, LIMIT o NO_PROPAGATION.

## 7. Partición atómica exacta

### 7.1 Condiciones de validez

Una partición P={P1,...,Pn} de requested_scope R es válida solo si:

1. n ≥ 1;
2. cada Pi es RESOLVED y no vacío;
3. cada Pi está contenido en R;
4. para todo i ≠ j, Pi ∩ Pj = EMPTY demostrado;
5. unión de todos Pi = R demostrado;
6. todos los átomos comparten decision context compatible;
7. dimensiones materiales resueltas o explícitamente NOT_APPLICABLE;
8. dependencias que crucen fronteras evaluadas;
9. ningún residual queda sin clasificar;
10. algoritmo y normalización versionados y reproducibles.

No basta con que los conteos sumen, que nombres parezcan distintos o que una muestra no encuentre solapamiento.

### 7.2 Procedimiento propuesto

1. Congelar requested_scope_id, versión y snapshot de universo.
2. Canonicalizar selectors e IDs con resolver versionado.
3. Validar firma de dimensiones y tipos.
4. Recopilar fronteras inducidas por reglas, evidencias, rutas, jurisdicciones, tamaños, tiempos y dependencias.
5. Construir regiones por intersecciones y diferencias demostrables, sin expandir fuera del requested_scope.
6. Crear residual explícito para cualquier parte no cubierta por predicados específicos.
7. Evaluar cada región frente a todos los inputs/causas pertinentes.
8. Fusionar regiones solo si predicados, inputs, applicability, dependencias y consecuencias son equivalentes.
9. Verificar disjunción par a par y cobertura exacta por método independiente aprobado.
10. Emitir manifest con proof/evidence refs, algoritmo/versiones y digest.
11. Si una operación queda UNRESOLVED, marcar envelope INCOMPLETE o REVIEW_REQUIRED según causa; no declarar partición válida.
12. Congelar resultado antes de aplicar agregación X-06/X-09.

### 7.3 Residual y ausencia de regla

Residual significa “parte del requested_scope no cubierta por predicados específicos”. No significa libre de veto ni elegible. Se evalúa con política baseline e inputs obligatorios. Si no puede probarse que residual existe o delimitarse, partición queda UNRESOLVED.

### 7.4 Fusión de átomos

Dos átomos pueden fusionarse solo si unión resuelta, mismo decision context/versiones compatibles, inputs materiales con mismos resultados, dependencias y consecuencias equivalentes, y la fusión no borra frontera necesaria para enforcement/auditoría. Outcomes iguales por sí solos no bastan.

## 8. Dependencias y cierre causal

### 8.1 Dependency graph

Cada edge debe declarar:
- dependency_edge_id, version y estado;
- source_object_id/type y target_object_id/type;
- dependency_type: SETTLEMENT, REDEMPTION, CUSTODY_CONTROL, BRIDGE_BACKING, PRICE_ORACLE, GOVERNANCE_CONTROL, DATA_LINEAGE u otro catálogo;
- dirección causal y condición de activación;
- purpose/activity afectadas;
- scope/ventana temporal;
- evidence refs, incertidumbre y owner;
- regla de propagación aprobada.

### 8.2 Propagación

1. Solo edges explícitos, vigentes y aplicables participan.
2. Evaluar edge por edge; no inferir transitividad por conectividad.
3. Clausura transitiva solo para dependency_type y policy que declaren transitividad, con límite/ciclo definido.
4. Dependencia no resuelta produce SCOPE_UNRESOLVED para consumidor potencialmente dependiente.
5. Edge no probado como independiente no se trata como independencia.
6. Dependencia no aplicable requiere evidencia y scope; no se presume por ausencia de edge.
7. Ciclo se rechaza o adjudica según regla versionada.
8. Grafo, reglas y versión forman parte del snapshot/digest.

### 8.3 Fronteras

Si una dependencia cruza dos unidades candidatas, crear región/relación visible, evaluar cada consumer scope, dividir unidades si la consecuencia difiere y mantenerlas separadas si enforcement exige identidades/rutas distintas. Si no puede resolverse el alcance, bloquear conclusión agregada favorable.

## 9. Scopes derivados y subset

Un scope derivado registra derived_scope_id, parent_scope_id, operación, requested scope original/versión, predicado exacto, proof/manifest de pertenencia, región excluida y razón, nueva decisión, policy/algorithm versions, enforcement reference y actor/owner/aprobador.

Invariantes:
1. Derived scope es subconjunto demostrado del parent cuando se declara subset.
2. No hereda decisión favorable del parent ni átomos hermanos.
3. Requiere nueva composición y decision_id.
4. Parent mantiene outcome original.
5. Región excluida queda identificada y no reingresa por default.
6. Si consumidor no puede aplicar selector sin ambigüedad, subset no es operable.
7. Cambio material de ruta, tamaño, tiempo, jurisdicción o environment requiere reevaluación.

## 10. Semántica temporal

- Intervalos expresan timezone y precisión; convención half-open [from,to) propuesta.
- valid_time no es observed_at, recorded_at ni evaluated_at.
- Scopes son temporalmente disjuntos solo si intervalos efectivos son disjuntos y precisión/timezone compatible.
- Evento cuyo instante no puede ordenarse produce relación temporal UNRESOLVED.
- TTL vencido no cambia una decisión de veto; dispara revisión/freshness según X-07.
- Evaluación histórica conserva versiones y universo originales.

## 11. ValidationEnvelope e interacción con X-09 A12

El álgebra devuelve ScopeResolutionResult independiente de DecisionOutcome.

| ScopeResolutionResult | ValidationEnvelope candidato | Regla |
|---|---|---|
| Dimensiones/relaciones RESOLVED; partición exacta demostrada | VALID | Puede continuar agregación |
| Identidad, alias, jerarquía, dependencia o intersección ambigua | REVIEW_REQUIRED | No emitir DecisionOutcome agregado |
| Cobertura/inputs/átomos obligatorios ausentes o residual sin clasificar | INCOMPLETE | No emitir DecisionOutcome agregado |
| Payload malformado, tipos/unidades inválidos o integridad rota | REJECTED_INVALID_INPUT | Rechazar composición |
| Dependencia desconocida pero delimitable provisionalmente | REVIEW_REQUIRED o INCOMPLETE según policy aprobada | No favorable en scope potencialmente afectado |

Esta tabla es una propuesta de mapeo; owners deben aprobar taxonomía y límite entre REVIEW_REQUIRED e INCOMPLETE. La precedencia del documento 18 solo aplica tras VALID.

## 12. Ejemplos

### 12.1 Dos vetos parcialmente solapados

Requested scope: activo A, redes N1/N2, activity TRADE. Veto V1: activo A en N1. Veto V2: activo A en venue X, con rutas en N1/N2.

Regiones candidatas: intersección (N1 + venue X), exclusiva V1 (N1 + otros venues), exclusiva V2 (N2 + venue X), residual (N2 + otros venues), siempre que el universo de rutas/venues esté definido. Intersección conserva ambas causas; regiones exclusivas solo la causa coincidente; residual requiere evaluación baseline. Si universo venues es desconocido, no se puede afirmar cobertura exhaustiva.

### 12.2 Wrapped y native

Veto a bridge B aplica a representación wrapped W. Native N no hereda veto por compartir underlying. Solo dependencia causal explícita (p.ej. redención/settlement común) puede crear región afectada adicional. Dependencia incierta deja native SCOPE_UNRESOLVED para conclusión dependiente; no automáticamente INELIGIBLE ni limpio.

### 12.3 Tamaño y liquidez

Assessment aplica a TRADE, venue X, SELL, notional USD 10k–20k, ventana T. No cubre BUY, otros tamaños, monedas ni venues. Evaluación para USD 15k no demuestra cobertura para USD 100k. Conversión requiere rate/source/timestamp en snapshot.

### 12.4 Withdrawal y settlement

Veto WITHDRAWAL en ruta R no se propaga a TRADE genéricamente. Si TRADE settlement depende causalmente de R, edge debe declararlo y región dependiente se particiona. Sin edge o evidencia de independencia, no se asume independencia.

## 13. Invariantes propuestos

- SCOPE-INV-01 Explicit dimensions: dimensión material es CONSTRAINED, UNCONSTRAINED_BY_SCOPE, NOT_APPLICABLE o UNRESOLVED.
- SCOPE-INV-02 No implicit wildcard: null/omitted/empty no significa ALL.
- SCOPE-INV-03 Canonical identity: sujetos usan IDs canónicos y namespaces requeridos.
- SCOPE-INV-04 Typed operations: no operar entre dominios, unidades, monedas o versiones incompatibles.
- SCOPE-INV-05 Unknown is not empty: UNRESOLVED nunca es EMPTY/DISJOINT.
- SCOPE-INV-06 Exact partition coverage: unión de átomos equivale exactamente a requested_scope.
- SCOPE-INV-07 Pairwise disjointness: toda intersección entre átomos distintos es EMPTY demostrada.
- SCOPE-INV-08 Atomic homogeneity: cada átomo es homogéneo en inputs, reglas, dependencias y consecuencias materiales.
- SCOPE-INV-09 Residual conservation: ninguna región queda fuera de átomos o residual explícito.
- SCOPE-INV-10 No scope expansion: ninguna operación amplía fuera del parent/requested scope sin nueva solicitud.
- SCOPE-INV-11 Dependency provenance: propagación exige edge causal versionado y evidencia.
- SCOPE-INV-12 No implicit transitivity: no transitividad salvo policy/type explícitos.
- SCOPE-INV-13 Derived scope isolation: subset requiere nueva decisión y conserva outcome parent.
- SCOPE-INV-14 Purpose/activity separation: purpose y activity son dimensiones distintas.
- SCOPE-INV-15 Route specificity: ORE se define por ruta concreta; no hereda entre venues/cuentas/redes.
- SCOPE-INV-16 Temporal precision: relaciones temporales requieren timezone/precision y semántica común.
- SCOPE-INV-17 Deterministic normalization: misma entrada/versiones producen mismo digest y partición.
- SCOPE-INV-18 Envelope gate: partición unresolved/incomplete/invalid impide DecisionOutcome agregado.
- SCOPE-INV-19 Cause conservation: operaciones conservan lineage y causas.
- SCOPE-INV-20 No permission by residual: residual no equivale a elegibilidad ni ausencia global de veto.

## 14. Escenarios de aceptación propuestos

No son fixtures ni pruebas ejecutables.

| ID | Input | Resultado esperado |
|---|---|---|
| SA-01 | Dos scopes idénticos, misma versión/contexto | EQUAL |
| SA-02 | Scope red N1 y N2 con IDs distintos y universo compatible | DISJOINT demostrado |
| SA-03 | Alias de activo ambiguo | UNRESOLVED; sin propagación favorable |
| SA-04 | Contract address sin chain_id | INVALID/UNRESOLVED |
| SA-05 | Dimensión omitida | INVALID/UNRESOLVED; no ALL |
| SA-06 | NOT_APPLICABLE justificado para scope analítico | Comparable según firma aprobada |
| SA-07 | Venue X dentro de universo explícito | Contención/intersección determinable |
| SA-08 | Jerarquía venue-parent no registrada | UNRESOLVED |
| SA-09 | Intersección [10,20] y [15,25], misma unidad | [15,20] según inclusividad |
| SA-10 | Rangos USD y token nativo sin conversión fijada | UNRESOLVED/INVALID |
| SA-11 | Intervalos [t0,t1) y [t1,t2) | DISJOINT |
| SA-12 | Intervalos con timezone/precision incompatible | UNRESOLVED |
| SA-13 | Dos vetos solapados y universo cerrado | Partición intersección, exclusivas y residual |
| SA-14 | Universo venues desconocido | INCOMPLETE/REVIEW_REQUIRED; sin exact partition |
| SA-15 | Veto wrapper sin dependencia causal a native | No propagar a native |
| SA-16 | Dependency edge aplicable a settlement | Particionar y propagar solo a región dependiente |
| SA-17 | Dependency graph contiene ciclo no permitido | INVALID/REVIEW_REQUIRED |
| SA-18 | Missing edge no prueba independencia | SCOPE_UNRESOLVED |
| SA-19 | Átomos solapan en boundary | Partición inválida hasta resolver inclusividad |
| SA-20 | Residual sin assessment baseline | INCOMPLETE |
| SA-21 | Subset derivado contenido en parent con prueba | Nuevo scope/decision_id; parent intacto |
| SA-22 | Derived scope excede parent | INVALID |
| SA-23 | Outcomes iguales pero inputs/dependencias distintos | No fusionar átomos |
| SA-24 | Inputs/scopes iguales con orden distinto | Digest/partición deterministas |
| SA-25 | Purpose RESEARCH y activity TRADE mezclados | No agregar |
| SA-26 | ORE de dos rutas diferentes | Una evaluación por route_id |
| SA-27 | Residual sin regla específica pero baseline completa | Evaluar baseline; no favorable automático |
| SA-28 | Cualquier relación material UNRESOLVED | Envelope no VALID; sin DecisionOutcome agregado |

## 15. Evidencia y auditoría

Cada operación debe reconstruirse con operand scope IDs/versiones/digests, operación/parámetros, universo de cada dimensión, resolver/normalizer versions, dependency graph snapshot/policy, regiones resultantes, pruebas de EMPTY/containment/disjointness/coverage, estado y reason codes, algoritmo/version, digest, actor, timestamp, revisión y supersedes links.

La evidencia no tiene que ser proof matemático formal en todos los casos; debe ser un artefacto verificable apropiado al dominio, con criterio aprobado por Architecture y Security. Técnica (constraint solver, interval algebra, finite set enumeration, symbolic predicates u otra) queda abierta.

## 16. Contrato conceptual y futura implementación

Objetos candidatos:
- ScopeDefinition
- ScopeDimension
- ScopeRelationResult
- ScopeOperationRecord
- ScopePartitionManifest
- AtomicScope
- DependencyEdge
- ScopeResolutionResult
- DerivedScopeRecord

No se proponen todavía JSON Schemas ni API. Antes de implementar se requiere cerrar vocabularios e identidad, elegir modelo de predicados, definir universos y firmas por scope_kind, resolver versionado/canonicalización/digest, aprobar algebra y ValidationEnvelope mapping, definir proof strategy/límites computacionales, aprobar threat model, mapear IIM por owners, diseñar property-based/negative tests y ejecutar suite reproducible con revisión independiente.

## 17. Owners y decisiones pendientes

| Área | Revisión | Aprobación |
|---|---|---|
| Architecture/Data | Modelo, dimensiones, álgebra, identidad/versionado, canonicalización, digest | Sí |
| Risk | Scope mínimo de veto, incertidumbre, propagación causal, solapamientos | Sí |
| Security | Spoofing, alias collision, boundary manipulation, graph poisoning, resource exhaustion | Sí |
| Operations | Route selectors, subset enforcement, residual, rollback | Sí |
| Legal/Compliance | Jurisdicción, persona/entidad, conducta y vigencia | Cuando aplique |
| Valuation/Research | Purpose, horizon, valuation object, source/model scopes | Sí para sus dominios |
| Requirement Owners | Trazabilidad IIM e impacto normativo | Sí antes de cambio IIM |
| Governance | Autoridad, revisión, excepción y auditoría | Sí |

Preguntas abiertas:
1. ¿Universo de cada dimensión finito enumerado, jerárquico, intervalar o predicate-based?
2. ¿Dimensiones y firmas obligatorias por scope_kind?
3. ¿Normalización de jurisdicción, instrumentos, venues y entidades?
4. ¿Prueba mínima aceptable para EMPTY, DISJOINT, CONTAINS y cobertura exacta?
5. ¿Qué dependency_edge admite transitividad?
6. ¿Límite entre REVIEW_REQUIRED e INCOMPLETE?
7. ¿Cómo limitar explosión combinatoria sin perder cobertura?
8. ¿Límites de tamaño/tiempo para expresiones y grafos?
9. ¿Política de fusión de átomos y autoridad aprobadora?
10. ¿Versionado de cambios de universo sin reescribir históricos?

## 18. Estado y límites

**ADDRESSED_IN_DRAFT / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

Esta propuesta formaliza vocabulario y método conceptual para comparar, operar y particionar scopes. No demuestra que el repositorio disponga de motor de scope, ontologías, registros canónicos, dependency graph, contratos ni pruebas. No se han creado fixtures/tests ejecutables ni se ha ejecutado validación matemática/empírica. No modifica requisitos IIM, schemas, código, políticas aprobadas o datos históricos.

El siguiente gate es revisión especializada y resolución de decisiones abiertas. Solo después corresponde traducir el modelo aprobado a contratos versionados, pruebas de propiedades y una implementación verificable.
