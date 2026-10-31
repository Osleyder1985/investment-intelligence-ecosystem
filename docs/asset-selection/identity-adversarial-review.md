# Revisión adversarial de la propuesta integrada de identidad y admisibilidad

**ID:** IIE-IDENTITY-ADV-REVIEW-003  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** REVISE_REQUIRED / NOT_APPROVED  
**Objeto revisado:** `docs/asset-selection/identity-graph-purpose-integration-proposal.md`  
**Referencias cruzadas:** modelo canónico de estados (doc. 03), lifecycle/freshness (doc. 12), Scope Algebra (doc. 19), FSA-01 (doc. 22), auditoría IDCA (doc. anterior).

## 1. Dictamen

La propuesta integrada mejora sustancialmente la separación de identidad, claims, evidencia, aplicabilidad y admisibilidad por finalidad. Sin embargo, la revisión adversarial detecta **10 hallazgos nuevos o insuficientemente resueltos**: 4 HIGH, 5 MEDIUM y 1 LOW.

**Resultado: REVISE_REQUIRED. No aprobar, no congelar schemas y no implementar todavía.**

Los bloqueos más importantes son:
- confusión entre purpose, scope_kind y activity;
- inconsistencia transversal sobre STALE;
- duplicación semántica de NOT_APPLICABLE;
- ausencia de una semántica completa y tipada para claims;
- reglas de proyección a TRUE/FALSE/UNKNOWN que todavía pueden producir inferencias incorrectas.

La revisión es documental y estática. No se ejecutaron tests, validación matemática, consultas de red ni pruebas empíricas.

## 2. Hallazgos

### ADV-01 — OPERATIONAL_ROUTE está modelado como purpose en vez de scope_kind / decision domain
**Severidad:** HIGH  
**Evidencia:** La propuesta declara ANALYTICAL_SELECTION y OPERATIONAL_ROUTE como purposes distintos. Scope Algebra define OPERATIONAL_ROUTE como valor de scope_kind y separa purpose de activity; el modelo canónico también separa SelectionDecision de RouteEligibility.  
**Riesgo:** El mismo concepto puede aparecer en diferentes ejes y generar scopes incompatibles, firmas inconsistentes o una ruta interpretada como propósito.  
**Corrección requerida:** Definir tres ejes independientes: (1) scope_kind / decision domain, (2) purpose, (3) activity. ANALYTICAL_SELECTION debe ubicarse en el eje aprobado de decision_type/purpose según catálogo; OPERATIONAL_ROUTE debe ser scope_kind o decision domain, no duplicarse como purpose sin una decisión explícita de Architecture. Mantener RouteEligibility/ORE como evaluación consumidora independiente.  
**Criterio de cierre:** Catálogo y matriz de firmas que demuestren combinaciones válidas y prohíban combinaciones semánticamente inválidas.

### ADV-02 — STALE sigue mezclando estado de identidad y freshness
**Severidad:** HIGH  
**Evidencia:** La propuesta incluye STALE en IdentityStatus. El modelo canónico doc. 03 también incluye STALE en IdentityStatus, pero Lifecycle doc. 12 exige separar freshness (FRESH/APPROACHING_EXPIRY/STALE), admissibility, lifecycle y assessment domain status.  
**Riesgo:** Dos consumidores pueden interpretar STALE como estado de resolución de identidad o como caducidad temporal. Puede producir transiciones inválidas y mappings contradictorios.  
**Corrección requerida:** No resolverlo localmente en esta propuesta. Abrir una decisión transversal del modelo canónico: mantener IdentityStatus solo para RESOLVED/UNRESOLVED/CONFLICTED y representar freshness en dimensión separada, o documentar formalmente por qué el estado canónico actual conserva STALE como proyección derivada. Nunca almacenar dos fuentes de verdad independientes para STALE.  
**Criterio de cierre:** Decisión de Architecture, actualización coordinada de doc. 03 y doc. 12, y mapping/migración versionados. No editar esos documentos sin aprobación.

### ADV-03 — NOT_APPLICABLE está duplicado en PurposeAdmissibilityStatus y ApplicabilityStatus
**Severidad:** HIGH  
**Evidencia:** La propuesta declara ApplicabilityStatus con NOT_APPLICABLE y también incluye NOT_APPLICABLE en PurposeAdmissibilityStatus. El modelo canónico distingue Applicability de EligibilityStatus y de los estados de evaluación.  
**Riesgo:** No queda claro si significa “la regla no aplica” o “la evaluación de finalidad no aplica”. Un agregador podría tratar una obligación no aplicable como resultado favorable o excluirla incorrectamente.  
**Corrección requerida:** Eliminar NOT_APPLICABLE del resultado epistemológico PurposeAdmissibilityStatus. Mantenerlo exclusivamente en ApplicabilityAssessment. El resultado del PurposeAssessment debe registrar si las obligaciones aplicables fueron satisfechas y conservar la lista de obligaciones excluidas y su justificación. Si el purpose completo no es aplicable, usar un objeto/decisión de applicability de nivel superior, no reutilizar el estado de una obligación individual.  
**Criterio de cierre:** Tabla de composición con cero, una y múltiples obligaciones aplicables, incluidas todas no aplicables y aplicabilidad indeterminada.

### ADV-04 — ClaimResolutionStatus carece de contrato formal de proposición atómica
**Severidad:** HIGH  
**Evidencia:** Se proponen CONFIRMED/REFUTED/UNKNOWN/CONFLICTED/NOT_ASSESSED/RETRACTED, pero no se exige una estructura completa de proposición.  
**Riesgo:** “Supply es 100M”, “el contrato tiene admin key”, “la reserva está respaldada” o “el bridge es solvente” pueden mezclarse en claims no comparables, sin unidad, cuantificador, periodo, sujeto, modalidad o criterio de refutación. CONFIRMED/REFUTED pueden quedar subjetivos.  
**Corrección requerida:** Cada claim debe contener al menos subject, predicate, object/value, qualifiers, unit/currency, valid_time, polarity, quantifier/population, jurisdiction/context cuando aplique, method/standard of proof, evidence refs y claim version. Definir atomización, contradicción, refutación y cuándo una negación es realmente exhaustiva. No asumir mundo cerrado por ausencia de evidencia.  
**Criterio de cierre:** Especificación de claim envelope, ejemplos positivos/negativos y reglas de adjudicación por familia de claims.

### ADV-05 — EvidenceAssessment no define granularidad ni agregación de fuentes
**Severidad:** MEDIUM  
**Evidencia:** La propuesta lista dimensiones de evidencia, pero no define si el assessment es por artefacto, claim, fuente, variable, finalidad o conjunto de evidencias. El doc. 03 exige conservar estado por variable y fuente y prohíbe agregar evidencia heterogénea sin preservar estados individuales.  
**Riesgo:** Una fuente rechazada puede contaminar indebidamente un claim respaldado por otra fuente independiente; o una fuente admisible puede ocultar evidencia conflictiva/rechazada. No hay política de corroboración, independencia ni resolución de conflicto.  
**Corrección requerida:** Definir EvidenceRecord, SourceAssessment y ClaimEvidenceAssessment separados; preservar estado por fuente/artefacto/claim/uso; declarar corroboration policy, source independence groups, conflict handling y threshold. La agregación debe ser determinista y explicable; no promediar estados categóricos.  
**Criterio de cierre:** Matriz source × claim × purpose, con casos de fuentes concordantes, dependientes, contradictorias, retiradas y no admisibles.

### ADV-06 — La proyección de REFUTED / inadmisibilidad a FSA-01 no está suficientemente tipada
**Severidad:** MEDIUM  
**Evidencia:** La tabla indica que un claim refutado puede dar FALSE si la regla demuestra no pertenencia y que evidencia inadmisible no aporta TRUE, con UNKNOWN/bloqueo según policy. FSA-01 requiere que TRUE/FALSE se deriven de reglas y evidencia admisible; UNKNOWN no se convierte en FALSE.  
**Riesgo:** Una refutación de un claim auxiliar podría convertirse en no pertenencia al scope aunque ese claim no defina la pertenencia. La inadmisibilidad de una fuente podría convertirse en UNKNOWN global incluso cuando existe otra fuente admisible suficiente.  
**Corrección requerida:** El adaptador debe operar sobre predicados de pertenencia explícitos, no mapear estados de claim de forma genérica. Debe evaluar el conjunto de evidencias elegibles según la finalidad y la regla de decisión. REFUTED solo produce FALSE cuando refuta exactamente una condición necesaria/suficiente de pertenencia y se cumplen las condiciones de completitud. REJECTED de una fuente excluye esa fuente, no invalida automáticamente todas las otras fuentes.  
**Criterio de cierre:** Tabla de decisión formal por predicate type, source set y purpose; pruebas de refutación parcial y corroboración alternativa.

### ADV-07 — Contrato temporal no preserva suficientemente el origen y la incertidumbre de timestamps
**Severidad:** MEDIUM  
**Evidencia:** La propuesta recomienda UTC y precision declarada, pero no exige preservar timezone/offset original, clock/source identity, incertidumbre temporal, precisión real ni evidencia del public_availability_at. Lifecycle exige timezone/offset o UTC con conversión original preservada.  
**Riesgo:** Se pueden ordenar eventos que no son temporalmente comparables, fabricar disponibilidad histórica o introducir look-ahead bias. Publicación, accesibilidad pública y conocimiento autorizado son eventos distintos.  
**Corrección requerida:** Para cada timestamp registrar valor normalizado, valor original, offset/timezone, precision, clock/source, uncertainty interval, derivation method y evidence ref. public_availability_at y authorized_availability_at deben ser valores demostrados o UNKNOWN, nunca inferidos del timestamp de captura. Definir cutoff con precisión y regla de frontera.  
**Criterio de cierre:** Ejemplos de relojes desalineados, publicación corregida, feed privado, archivo republicado y eventos con orden temporal indeterminado.

### ADV-08 — Dependency contract no incorpora todos los atributos de Lifecycle y la propagación provisional
**Severidad:** MEDIUM  
**Evidencia:** La propuesta exige tipo, intended_use, scope_transform y propagation_rule/version. Lifecycle además exige IDs/versiones upstream/downstream, owner, creación/vigencia; eventos incluyen verification_status, confidence, causation/correlation, idempotency y affected consumers.  
**Riesgo:** No se puede reproducir qué versión de la dependencia fue usada ni decidir cómo tratar señales creíbles pero no verificadas. Una señal no verificada podría invalidar demasiado o demasiado poco.  
**Corrección requerida:** Referenciar el contrato LifecycleEvent/DependencyEdge/InvalidationRecord existente y no redefinir una versión reducida. Incorporar edge IDs, upstream/downstream object version, validity, owner, event verification/confidence, causal path, idempotency y affected consumer. Separar REVIEW_REQUIRED/QUARANTINED provisional de INVALIDATED confirmado.  
**Criterio de cierre:** Reglas de propagación para trigger confirmado, provisional, retractado, tardío, duplicado y fuera de scope.

### ADV-09 — IdentityStatus no declara explícitamente el alcance de la resolución
**Severidad:** MEDIUM  
**Evidencia:** El modelo canónico define RESOLVED como identidad y representación resueltas para el alcance. La propuesta presenta vocabulario de identidad global y luego separa claims/relaciones, pero no fija si EntityIdentityAssessment resuelve una entidad abstracta, una representación, una red concreta o una clave compuesta.  
**Riesgo:** Una entidad económica resuelta puede confundirse con contrato/red/representación resueltos. Una identidad canónica global podría habilitar una representación no corroborada.  
**Corrección requerida:** Definir IdentityAssessment siempre con subject type, subject key, representation_id cuando aplique, network/environment, scope y valid_time. Separar canonical entity resolution de representation/network binding. RESOLVED solo para la clave y alcance declarados.  
**Criterio de cierre:** Matriz de identidad económica, representación nativa/wrapped/bridged, contrato y network binding; escenarios de migración, fork y chain ID conflictivo.

### ADV-10 — El resultado de PurposeAssessment no separa claramente outcome, workflow y lifecycle
**Severidad:** LOW  
**Evidencia:** PurposeAdmissibilityStatus enumera resultado, mientras que el proceso también puede requerir revisión, evaluación en curso, invalidación y supersession. La propuesta indica que lifecycle va separado, pero no define el envelope completo ni la regla de combinación.  
**Riesgo:** Los consumidores pueden tratar NOT_ASSESSED, REVIEW_REQUIRED, STALE o INVALIDATED como valores mutuamente excluyentes de una sola columna, perdiendo si el proceso está incompleto o si el resultado histórico quedó invalidado.  
**Corrección requerida:** Definir PurposeAssessment envelope con assessment_status/outcome, EvaluationLifecycle, Freshness, Lifecycle, Applicability y Evidence dependencies como campos/dominios ortogonales. Fijar reglas de consumo para cada combinación.  
**Criterio de cierre:** Máquina de estados y matriz de combinación para in-progress, complete, review-required, stale, invalidated y superseded.

## 3. Matriz de severidad

| Severidad | Hallazgos |
|---|---|
| HIGH | ADV-01, ADV-02, ADV-03, ADV-04 |
| MEDIUM | ADV-05, ADV-06, ADV-07, ADV-08, ADV-09 |
| LOW | ADV-10 |

## 4. Orden de corrección

1. ADV-01 y ADV-02: alinear catálogo de propósito/scope_kind/activity y resolver conflicto canónico de STALE.
2. ADV-03 y ADV-04: normalizar aplicabilidad y formalizar claim envelope.
3. ADV-05 y ADV-06: diseñar la composición claim-evidence-predicate sin inferencias indebidas.
4. ADV-07 y ADV-08: alinear timestamps, eventos y dependencias con lifecycle.
5. ADV-09 y ADV-10: delimitar identidad por representación/scope y el envelope del assessment.
6. Ejecutar segunda revisión adversarial independiente.
7. Solo con revisión aprobada, diseñar schemas y fixtures.

## 5. Restricciones y conclusión

- No se modificaron requisitos IIM, schemas, código, fixtures, tests ni documentos normativos.
- No se ejecutaron tests ni validación matemática/empírica.
- No se cerraron IDCA-01..08 ni IDR-01/02.
- La propuesta integrada permanece NOT_APPROVED.

**Conclusión:** La arquitectura de separación de objetos es aprovechable, pero el contrato todavía presenta ambigüedades que podrían trasladarse directamente a errores de clasificación, scope, decisión o invalidación. Corregir primero el modelo semántico es menos costoso y más seguro que parchear esos errores después en código.
