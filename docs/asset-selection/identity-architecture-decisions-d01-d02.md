# Resolución arquitectónica propuesta — D-01 y D-02

**ID:** IIE-IDENTITY-ARCH-DECISIONS-005  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED  
**Base:** identity-integration-remediation-plan.md; identity-graph-purpose-integration-proposal.md; metodología 03, 12 y 19  
**Issue:** #61

## 1. Propósito y alcance

Este documento desarrolla una resolución semántica para D-01 (separación de ejes de alcance y evaluación) y D-02 (separación de identidad y freshness). Es una propuesta de decisión arquitectónica. No modifica los documentos metodológicos de referencia, requisitos IIM, schemas ni código; tampoco declara aprobados los catálogos.

## 2. D-01 — Separación de ejes

### 2.1 Decisión propuesta

Adoptar cuatro conceptos ortogonales y tipados:

| Concepto | Función | Ejemplos | No debe representar |
|---|---|---|---|
| `scope_kind` | Clasifica la naturaleza del scope y determina su contrato dimensional | ANALYTICAL, OPERATIONAL_ROUTE, RISK, VETO, EVIDENCE, VALUATION | Una finalidad de evaluación o una acción |
| `decision_type` | Identifica el tipo de decisión consumidora que puede referenciar el scope | ANALYTICAL_SELECTION, ROUTE_ELIGIBILITY | Una dimensión de scope ni una finalidad |
| `purpose` | Declara para qué se evalúa/usa la entidad, claim o evidencia | DISCOVERY, RESEARCH, VALUATION, COMPARISON, BACKTEST | La ruta o acción operativa |
| `activity` | Declara qué acción concreta se pretende realizar | MARKET_DATA, TRADE, DEPOSIT, WITHDRAWAL, TRANSFER, CUSTODY, REDEMPTION | El propósito analítico o la clase del scope |

Los valores son ejemplos existentes en borradores, no un catálogo aprobado. La lista definitiva, cardinalidades, compatibilidad y versionado requieren Architecture y los owners de dominio.

### 2.2 Reglas de composición

1. `scope_kind` es metadato clasificatorio del ScopeDefinition; no sustituye una dimensión ni se combina como si fuera un propósito.
2. `purpose` y `activity` son dimensiones diferentes. Una evaluación puede tener propósito sin actividad operativa; una actividad no implica propósito.
3. `decision_type` identifica al consumidor/resultado de decisión, no una autorización. `ROUTE_ELIGIBILITY` no habilita una orden.
4. `OPERATIONAL_ROUTE` pertenece a `scope_kind`; la ruta concreta y sus componentes pertenecen a la dimensión Route. La actividad se declara por separado.
5. Un scope analítico puede declarar Route como `NOT_APPLICABLE` solo si el contrato de ese `scope_kind` lo permite explícitamente. No se debe omitir silenciosamente la dimensión.
6. Cada `scope_kind` debe publicar una matriz de dimensiones: REQUIRED, OPTIONAL, NOT_APPLICABLE_ALLOWED o FORBIDDEN. Una dimensión requerida ausente produce UNRESOLVED o INVALID según sea ausencia de información o violación estructural.
7. La compatibilidad entre `scope_kind`, `decision_type`, `purpose` y `activity` se valida mediante una matriz versionada. No se aceptan combinaciones no declaradas ni coerción de un valor a otro.
8. Dos scopes solo se comparan/operan si sus dimensiones comunes, unidades, universos, normalización y contextos son compatibles; el mismo texto de enum no demuestra equivalencia semántica.

### 2.3 Contrato de ScopeDefinition

Una definición futura deberá identificar, como mínimo:

- scope_id y versión del contrato/normalización;
- scope_kind;
- decision_type consumidor, cuando exista;
- purpose y activity como campos independientes, con ausencia explícita y semántica;
- subject selector tipado y referencias canónicas;
- dimensiones y sus estados;
- valid_time y contexto temporal de evaluación;
- universo/snapshot y contexto de datos;
- dependencias y reglas de transformación;
- provenance, policy/rule version y digest semántico.

No se prescribe aquí un schema ni una representación JSON.

### 2.4 Casos de aceptación

| Caso | Entrada | Resultado requerido |
|---|---|---|
| A | scope_kind=OPERATIONAL_ROUTE, activity=TRADE, decision_type=ROUTE_ELIGIBILITY | Válido solo si la matriz de compatibilidad de versión lo permite y la Route requerida está resuelta |
| B | purpose=RESEARCH, activity ausente, scope_kind=ANALYTICAL | Puede ser válido si la matriz declara activity opcional/no aplicable; nunca inferir TRADE |
| C | purpose=TRADE | Rechazar como valor de purpose si TRADE pertenece al catálogo de activity |
| D | scope_kind=ANALYTICAL con Route omitida | No interpretar como wildcard; exigir estado explícito según contrato |
| E | decision_type=ROUTE_ELIGIBILITY sin route_id/tupla de ruta requerida | UNRESOLVED si falta información; INVALID si el contrato exige campo estructural y está malformado |
| F | mismo purpose, distinta activity o jurisdicción | Scopes distintos; no heredar admisibilidad/decisión |

### 2.5 Resolución de ADV-01

**Propuesta:** ADDRESSED_IN_DRAFT. Se elimina la ambigüedad conceptual, pero el hallazgo no se cierra hasta aprobar catálogos, matriz de compatibilidad y contrato de dimensiones por Architecture.

## 3. D-02 — IdentityStatus frente a Freshness

### 3.1 Decisión propuesta

Retirar `STALE` del vocabulario propio de `IdentityStatus` en la próxima versión aprobada del modelo canónico y representar la actualidad mediante un eje independiente `FreshnessStatus`.

La identidad responde a: “¿está resuelta la identidad del sujeto/representación para este scope y versión de evidencia?”. Freshness responde a: “¿sigue siendo utilizable esta evaluación para este uso y momento según la política de vigencia?”.

### 3.2 Vocabularios propuestos

**IdentityStatus**
- RESOLVED
- UNRESOLVED
- CONFLICTED

**FreshnessStatus**
- FRESH
- APPROACHING_EXPIRY
- STALE
- NOT_APPLICABLE (solo cuando la política de freshness declare formalmente que no aplica)

**AssessmentLifecycle**
- NOT_STARTED
- IN_PROGRESS
- COMPLETE
- REVIEW_REQUIRED
- SUPERSEDED
- INVALIDATED

Estos vocabularios no se fusionan. `STALE` no significa identidad falsa, contradictoria ni no resuelta. `INVALIDATED` no es sinónimo de `STALE`: requiere un evento que rompa la validez. `SUPERSEDED` significa que otro registro reemplaza el uso actual, preservando el anterior.

### 3.3 Regla de evaluación contextual

Freshness se determina para la tupla:

`object + identity_claim/assessment + source + scope + intended_use + evaluated_at + freshness_policy_id/version`.

Por tanto, una misma identidad puede permanecer RESOLVED mientras una evaluación concreta está STALE para un uso operativo en tiempo real y sigue siendo utilizable para reconstrucción histórica bajo el cutoff/política correspondientes.

No existe un único `is_fresh` global del activo. Cada consumidor declara si requiere FRESH, si acepta APPROACHING_EXPIRY y cómo trata STALE. Si freshness es material y está STALE, el consumidor se abstiene de conclusiones que requieran actualidad; no cambia por ello la verdad histórica del claim.

### 3.4 Regla de bloqueo y composición

- IdentityStatus=UNRESOLVED o CONFLICTED bloquea conclusiones dependientes de la identidad afectada.
- IdentityStatus=RESOLVED + FreshnessStatus=STALE bloquea solo usos que requieren freshness y dentro del scope afectado.
- FreshnessStatus=UNKNOWN/NOT_ASSESSED (si el modelo consumidor lo admite como condición de evaluación) no puede tratarse como FRESH.
- AssessmentLifecycle=INVALIDATED bloquea consumo actual del assessment invalidado; se crea una nueva evaluación.
- Una señal provisional no verificada puede causar REVIEW_REQUIRED/QUARANTINED conforme a la política, pero no debe mutar automáticamente IdentityStatus a CONFLICTED ni Lifecycle a INVALIDATED.
- La restricción se propaga únicamente por dependencias y scopes demostrados; dependencia no resuelta produce revisión, no NOT_AFFECTED.

### 3.5 Compatibilidad y migración

Hasta que Architecture apruebe la migración:

1. No editar el modelo canónico 03 ni Lifecycle 12.
2. Todo consumidor de la versión legacy que reciba IdentityStatus=STALE debe conservar el valor original y tratarlo como estado legacy ambiguo: no mapearlo automáticamente a UNRESOLVED, CONFLICTED ni INVALIDATED.
3. Para migración futura, conservar `legacy_status=STALE`, documento/versión, timestamp y regla de mapping; derivar FreshnessStatus=STALE solo cuando la evidencia de origen demuestre que el significado era expiración temporal/contextual.
4. Si la causa original no puede determinarse, el registro queda en revisión/no migrable automáticamente.
5. No emitir simultáneamente IdentityStatus=STALE y FreshnessStatus=STALE como dos estados independientes sin regla de compatibilidad versionada.
6. La versión mayor del contrato canónico debe declarar la fecha efectiva, productores/consumidores afectados y política de lectura de registros históricos.

### 3.6 Casos de aceptación

| Caso | Entrada | Resultado requerido |
|---|---|---|
| G | Identidad resuelta; evaluación fuera de TTL para trading | Identity=RESOLVED; Freshness=STALE; bloquear solo consumo que exige frescura |
| H | Identidad resuelta históricamente; cutoff histórico válido | Identity=RESOLVED para el assessment histórico; freshness evaluada respecto del cutoff y policy histórica |
| I | Dirección/chain ID contradictorios | Identity=CONFLICTED; no etiquetar como STALE para ocultar conflicto |
| J | No hay evidencia suficiente para resolver contrato | Identity=UNRESOLVED; no inferir STALE |
| K | Incidente confirmado invalida una evaluación anterior | Lifecycle=INVALIDATED con evento/scope/causa; identidad factual se reevalúa separadamente |
| L | Nueva evaluación reemplaza la anterior | Nueva versión; anterior=SUPERSEDED para uso actual y conservada para replay |
| M | Alerta no corroborada sobre una representación | REVIEW_REQUIRED/QUARANTINED según política y scope provisional; no mutación automática de identidad |

### 3.7 Resolución de ADV-02

**Propuesta:** ADDRESSED_IN_DRAFT. El conflicto semántico se resuelve conceptualmente, pero no se cierra hasta aprobar el cambio del modelo 03, la compatibilidad con Lifecycle 12 y la migración de consumidores.

## 4. Matriz integrada de independencia

| Eje | Pregunta | Ejemplo de estado/valor | No implica |
|---|---|---|---|
| Identity | ¿Qué entidad/representación es? | RESOLVED | Freshness, backing, solvencia o elegibilidad |
| Claim resolution | ¿Es verdadera la proposición? | CONFIRMED / REFUTED / UNKNOWN | Admisibilidad de evidencia |
| Evidence admissibility | ¿Puede usarse esta evidencia para el propósito/scope? | ADMISSIBLE / REJECTED | Que el claim sea verdadero/falso |
| Applicability | ¿Aplica la regla? | APPLICABLE / NOT_APPLICABLE / UNDETERMINED | Admisibilidad |
| Freshness | ¿Es suficientemente actual para este uso? | FRESH / STALE | Veracidad o invalidez histórica |
| Lifecycle | ¿Está vigente este registro? | ACTIVE / SUPERSEDED / INVALIDATED | Veracidad del hecho subyacente |
| Scope resolution | ¿Está determinado el alcance? | RESOLVED / UNRESOLVED / INVALID | Resultado favorable |
| Decision | ¿Qué concluye el consumidor? | Estados del modelo de decisión | Autorización de ejecución |

## 5. Riesgos residuales y límites

1. La matriz de compatibilidad de catálogos todavía no está aprobada.
2. El significado histórico de cada aparición de IdentityStatus=STALE debe inventariarse antes de migrar.
3. Los estados UNKNOWN/NOT_ASSESSED de freshness requieren armonización explícita con el vocabulario de Lifecycle; este documento no los agrega unilateralmente al catálogo canónico.
4. No se define aún el algoritmo de agregación de múltiples claims/fuentes para resolver identidad.
5. No se demuestra formalmente la corrección de las reglas ni se crean fixtures/tests.

## 6. Criterios de aprobación

- [ ] Architecture aprueba la separación scope_kind / decision_type / purpose / activity.
- [ ] Se aprueba catálogo versionado y matriz de compatibilidad.
- [ ] Se aprueba la matriz REQUIRED/OPTIONAL/NOT_APPLICABLE_ALLOWED/FORBIDDEN por scope_kind.
- [ ] Architecture aprueba retirar IdentityStatus=STALE en nueva versión del modelo 03.
- [ ] Lifecycle owner confirma que FreshnessStatus es la dimensión temporal única y coherente.
- [ ] Se completa inventario/mapping de usos legacy de STALE y estrategia de migración.
- [ ] Se revisan los casos A–M y se derivan fixtures/tests tras aprobación.
- [ ] Una auditoría cruzada posterior no encuentra contradicciones críticas abiertas.

## 7. Estado final

**PROPOSED / PENDING_ARCHITECTURE_REVIEW / NOT_APPROVED.**

D-01 y D-02 quedan desarrolladas como decisiones propuestas, no aprobadas. No se modificaron los documentos 03, 12 o 19, requisitos IIM, schemas, código, fixtures ni tests. No se ejecutaron pruebas ni validación formal/empírica.
