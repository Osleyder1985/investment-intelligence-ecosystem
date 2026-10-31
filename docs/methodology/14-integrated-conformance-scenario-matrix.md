# Matriz integrada de escenarios de conformidad

**ID:** IIE-METH-CONFORMANCE-010  
**Versión:** 0.4.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo:** X-09; revisión 0.3.0 incorpora resoluciones propuestas A01-A10 de auditoría 15  
**Dependencias:** M-02 a M-13; resoluciones X-01 a X-08  
**Propósito:** especificar escenarios, combinaciones, resultados esperados e invariantes antes de construir fixtures y pruebas ejecutables.

## 1. Objetivo y alcance

Definir una matriz integrada que permita evaluar la consistencia semántica entre identidad, evidencia, aplicabilidad, riesgo, veto, elegibilidad analítica (AE), elegibilidad operativa de ruta (ORE), decisión, lifecycle, autorizaciones y gobernanza.

La matriz debe comprobar que:
- los mismos inputs versionados y el mismo contexto producen el mismo resultado;
- estados desconocidos, incompletos, inválidos, obsoletos o conflictivos nunca se convierten en resultados favorables por defecto;
- los vetos solo afectan los scopes y actividades que les corresponden;
- AE, ORE, decisión de selección y autorización transaccional permanecen separadas;
- causas concurrentes se conservan sin compensación mediante score;
- invalidación, expiración y excepciones siguen reglas explícitas;
- las decisiones privilegiadas respetan autoridad y segregación de funciones.

Este documento no implementa un motor ni define por sí solo la política de inversión. Los resultados esperados son una baseline de revisión; las reglas deben ser aprobadas por los owners competentes antes de convertirse en tests normativos.

## 2. Documentos y decisiones de referencia

La matriz integra las propuestas de:
- M-02: modelo canónico de estados y transiciones.
- M-03: elegibilidad analítica frente a elegibilidad operativa.
- M-04: RiskAssessment, RiskDomainStatus y VetoDecision.
- M-06: auditoría cruzada y hallazgo X-09.
- M-07: resolución de vocabulario de riesgo/veto (X-01/X-03).
- M-08: catálogo de dominios y aplicabilidad (X-02).
- M-09: estado canónico ORE (X-04).
- M-10: propagación de vetos por finalidad, actividad y scope (X-05).
- M-11: algoritmo determinista de composición (X-06).
- M-12: lifecycle, freshness e invalidación (X-07).
- M-13: gobernanza RACI y SoD (X-08).

**Advertencia de versión:** todos estos documentos son propuestas de diseño sujetas a revisión. La matriz no convierte sus estados o reglas en vocabulario normativo aprobado.

## 3. Modelo de conformidad y unidades de evaluación

### 3.1 Unidad de evaluación

Un escenario se evalúa sobre un snapshot reproducible que identifica, al menos:
- asset_id y representation_id cuando aplique;
- finalidad analítica y/o actividad operativa;
- scope de activo, red, contrato, venue, contraparte, cuenta, jurisdicción y entorno, según corresponda;
- EvaluationRecord y snapshot_id;
- versiones de taxonomía, catálogo de riesgo, política de veto, reglas de propagación, algoritmo de composición y políticas de lifecycle;
- referencias y versiones de IdentityAssessment, EvidenceAssessment, ApplicabilityAssessment, AE, RiskAssessment, RiskDomainAssessment, VetoDecision, ORE y decisiones consumidoras;
- timestamps relevantes: valid_time, observed_at, recorded_at, evaluated_at y effective_at;
- actores, roles, delegaciones, recusaciones y aprobaciones requeridas;
- estado de excepción y autorización, si existe.

Los campos no aplicables se omiten o se marcan NOT_APPLICABLE con justificación; no se rellenan con valores ficticios.

### 3.2 Dimensiones independientes

| Dimensión | Valores/estados relevantes | Pregunta de conformidad |
|---|---|---|
| Identidad | Resuelta, no resuelta, conflictiva, inválida, obsoleta | ¿La conclusión depende de una identidad suficientemente estable? |
| Evidencia | Admisible, insuficiente, desconocida, obsoleta, conflictiva, inválida | ¿La evidencia es apta para este uso y snapshot? |
| Aplicabilidad | APPLICABLE, NOT_APPLICABLE, APPLICABILITY_UNRESOLVED, APPLICABILITY_CONFLICTED | ¿El dominio/regla aplica y está justificado? |
| Riesgo | NOT_ASSESSED, ASSESSMENT_IN_PROGRESS, ASSESSED_WITH_FINDINGS, ASSESSED_NO_MATERIAL_FINDING, UNKNOWN, STALE, CONFLICTED, INVALID | ¿Qué estado de evaluación tiene cada dominio? |
| Veto | VETO_ACTIVE, VETO_REVIEW_REQUIRED, VETO_NOT_ASSESSED, NO_VETO_IDENTIFIED | ¿Qué resultado produjo la política de veto para el scope? |
| AE | Estados canónicos definidos por M-02/M-03 y sus razones | ¿Puede continuarse la finalidad analítica? |
| ORE | Diez estados propuestos por M-09 | ¿Puede usarse esta ruta concreta para esta actividad? |
| Decisión | Selección, rechazo, abstención, revisión o estado equivalente aprobado | ¿Qué conclusión produce la composición? |
| Lifecycle | Vigente, stale, invalidated, superseded, reevaluation pending, etc. | ¿Los inputs continúan siendo consumibles? |
| Gobernanza | Propuesto, revisado, aprobado, recusado, delegado, expirado | ¿La acción fue realizada por autoridad válida e independiente? |
| Autorización | No emitida, pendiente, aprobada, denegada, expirada, revocada | ¿Existe permiso independiente para la acción concreta? |

No se debe construir un único enum global que mezcle estas dimensiones.

### 3.3 Alcance de los resultados

Los resultados de esta matriz se expresan por finalidad y actividad. Un escenario puede producir simultáneamente:
- AE favorable para RESEARCH;
- ORE inelegible para WITHDRAWAL;
- ORE elegible para MARKET_DATA;
- una decisión de selección en revisión por otra causa;
- ninguna TradeAuthorization.

No se debe colapsar ese vector en un único estado de activo.

## 4. Resultados esperados y reglas de interpretación

### 4.1 Resultado analítico

La composición analítica debe distinguir, según el catálogo finalmente aprobado:
- elegible para finalidad;
- inelegible por regla aplicable;
- revisión requerida;
- información insuficiente;
- abstención;
- no evaluado.

Una evaluación material UNKNOWN, NOT_ASSESSED, STALE, CONFLICTED o INVALID bloquea la conclusión favorable que dependa de ella. No necesariamente bloquea toda finalidad no dependiente de ese dominio.

### 4.2 Resultado de veto

Precedencia propuesta:
1. VETO_ACTIVE;
2. VETO_REVIEW_REQUIRED;
3. VETO_NOT_ASSESSED;
4. NO_VETO_IDENTIFIED.

La precedencia se aplica al mismo scope y regla/política. No sustituye el estado del dominio de riesgo ni autoriza a mezclar scopes distintos.

### 4.3 Resultado ORE

Catálogo propuesto:
- ROUTE_NOT_ASSESSED
- ROUTE_ASSESSMENT_IN_PROGRESS
- ROUTE_ELIGIBLE
- ROUTE_ELIGIBLE_WITH_LIMITATIONS
- ROUTE_INELIGIBLE
- ROUTE_REVIEW_REQUIRED
- ROUTE_INSUFFICIENT_INFORMATION
- ROUTE_STALE
- ROUTE_CONFLICTED
- ROUTE_INVALID

Una ruta solo puede resultar elegible si todos los controles obligatorios aplicables a esa actividad, ruta, cuenta, entorno y jurisdicción satisfacen las condiciones aprobadas. Las limitaciones deben ser expresas, verificables y aplicables por enforcement.

### 4.4 No equivalencias

- NOT_APPLICABLE no equivale a UNKNOWN ni a falta de datos.
- NO_VETO_IDENTIFIED no equivale a riesgo cero ni a ausencia universal de amenazas.
- VETO_NOT_ASSESSED no equivale a NO_VETO_IDENTIFIED.
- AE favorable no equivale a ORE favorable.
- ORE favorable no equivale a TradeAuthorization.
- VETO no evaluado no se resuelve por TTL.
- VETO fuera de scope no equivale a NO_VETO_IDENTIFIED para el scope actual.
- Un estado técnico no crea autoridad organizativa.
- Una aprobación organizativa no omite enforcement ni controles pre/post-trade.

## 5. Matriz base de decisión

Esta tabla ofrece expectativas mínimas para un scope dependiente. Las causas adicionales pueden endurecer el resultado, nunca hacerlo más favorable.

| ID | Aplicabilidad | RiskDomainStatus | VetoDecision | Resultado esperado mínimo |
|---|---|---|---|---|
| B-01 | APPLICABLE | NOT_ASSESSED | VETO_NOT_ASSESSED | Conclusión dependiente no favorable; abstención/incompleto |
| B-02 | APPLICABLE | UNKNOWN | VETO_NOT_ASSESSED | Conclusión dependiente no favorable; información insuficiente |
| B-03 | APPLICABLE | STALE | NO_VETO_IDENTIFIED antiguo | No usar como favorable la conclusión dependiente hasta verificar vigencia. Evaluar por separado freshness del RiskDomainAssessment, VetoDecision, evidencia subyacente y policy; no invalidar ni levantar automáticamente un veto cuya evidencia/decisión siga vigente |
| B-04 | APPLICABLE | CONFLICTED | VETO_REVIEW_REQUIRED | REVIEW_REQUIRED en scope |
| B-05 | APPLICABLE | ASSESSED_WITH_FINDINGS | VETO_ACTIVE | Para cada unidad afectada, aplicar el DecisionOutcome tipado (p. ej. DECISION_INELIGIBLE o ROUTE_INELIGIBLE según decision_type); VETO_ACTIVE permanece en VetoDecision y no sustituye el outcome |
| B-06 | APPLICABLE | ASSESSED_NO_MATERIAL_FINDING | NO_VETO_IDENTIFIED | Puede continuar gates dependientes; no implica riesgo cero |
| B-07 | NOT_APPLICABLE justificado | Sin evaluación del dominio excluido | No aplica regla asociada | Excluir solo ese dominio/regla del denominador; conservar justificación |
| B-08 | APPLICABILITY_UNRESOLVED | Cualquier estado no concluyente | Cualquier resultado favorable aparente | No asumir NOT_APPLICABLE; revisión/insuficiencia |
| B-09 | APPLICABILITY_CONFLICTED | Cualquier estado | Cualquier resultado favorable aparente | REVIEW_REQUIRED; resolver conflicto |
| B-10 | APPLICABLE | Cualquier estado | VETO_ACTIVE | Bloquear scope sin compensación por score |
| B-11 | APPLICABLE | Cualquier estado | VETO_REVIEW_REQUIRED | Suspender conclusión favorable en scope |
| B-12 | APPLICABLE | Cualquier estado | VETO_NOT_ASSESSED | No concluir ausencia de veto |
| B-13 | APPLICABLE | Estado favorable | NO_VETO_IDENTIFIED pero cobertura incompleta | Resultado inválido; no aceptar NO_VETO_IDENTIFIED |
| B-14 | APPLICABLE | INVALID | NO_VETO_IDENTIFIED | Invalidar composición dependiente; no consumir como favorable |
| B-15 | APPLICABLE | ASSESSMENT_IN_PROGRESS | VETO_NOT_ASSESSED | No usar como resultado final |

## 6. Matriz de propagación por finalidad y actividad

La evaluación debe considerar por separado la finalidad analítica y la actividad operativa. La tabla es ilustrativa; la matriz M-10 y su catálogo de reglas aprobado serán la fuente normativa final.

| Caso | Veto/scope | RESEARCH | VALUATION | MARKET_DATA | TRADE | DEPOSIT | WITHDRAWAL |
|---|---|---|---|---|---|---|---|
| P-01 | Suspensión exclusiva de WITHDRAWAL | No bloquear por inferencia | No bloquear por inferencia | No bloquear por inferencia | No bloquear si settlement/financiación/ruta son independientes y la policy lo permite; si dependen del servicio suspendido, bloquear/revisar | No bloquear por inferencia | Bloquear/revisar según veto |
| P-02 | Compromiso de bridge para representación wrapped | Investigación del activo nativo sigue posible, si es independiente | Para wrapper: NOT_ESTIMABLE/INSUFFICIENT_EVIDENCE si la paridad/redención es material y no verificable; ESTIMABLE_WITH_LIMITATIONS solo con modelo alternativo independiente y limitaciones explícitas | Bloquear/revisar feeds/ruta afectados | Bloquear representación/ruta afectada | Según scope de representación | Según scope de representación |
| P-03 | Compromiso de custodia de cuenta concreta | No bloquear investigación general | No bloquear valoración general | Puede seguir disponible fuera de cuenta | Bloquear operaciones que dependan de custodia afectada | Bloquear depósitos a cuenta afectada | Bloquear retiros desde custodia afectada |
| P-04 | Riesgo legal/jurisdiccional de una actividad específica | Puede restringirse finalidad según regla | Puede restringirse finalidad según regla | Según jurisdicción y actividad | Bloquear actividad prohibida | Bloquear actividad prohibida | Bloquear actividad prohibida |
| P-05 | Venue comprometido | Investigación de activo posible | Análisis general posible | Datos del venue cuestionado no admisibles sin corroboración | Bloquear ruta en venue afectado | No inferir bloqueo universal | No inferir bloqueo universal |
| P-06 | Riesgo material del protocolo que afecta al activo | Bloquear/restringir finalidad según regla | Bloquear/restringir finalidad según regla | No necesariamente bloqueado, pero etiquetar | Bloquear actividad si regla y scope lo exigen | Según scope | Según scope |

Regla: cada resultado requiere una regla de propagación identificable, versión, evidencia, scope y consecuencia. No inferir una propagación que no esté especificada.

## 7. Catálogo de escenarios integrados

### 7.1 Identidad y evidencia

| ID | Condición | Resultado esperado | Clase |
|---|---|---|---|
| S-001 | Identidad de contrato no resuelta; evidencia de mercado corresponde a dos representaciones posibles | Bloquear conclusiones que requieran identidad exacta; no asignar evidencia a ambas por conveniencia | Negativo |
| S-002 | Identidad resuelta, pero evidencia crítica sin procedencia verificable | EvidenceAssessment no admisible; conclusión dependiente en insuficiencia/revisión | Negativo |
| S-003 | Fuente válida, observación fuera de TTL para uso actual | Marcar stale según política de dominio; reevaluación | Negativo |
| S-004 | Dos fuentes creíbles presentan valores incompatibles y no hay resolución | CONFLICTED; conservar ambas y abstenerse de conclusión dependiente | Negativo |
| S-005 | Evidencia NOT_APPLICABLE con justificación taxonómica aprobada | Excluir solo del módulo/regla pertinente, con traza | Positivo condicionado |
| S-006 | Evidencia corregida por productor | Crear nueva versión/evento; preservar historial y exigir nueva revisión cuando sea material | Lifecycle |
| S-007 | EvidenceAssessment de un snapshot distinto al de la decisión | Rechazar mezcla de snapshots o exigir recomposición | Negativo |
| S-008 | Analista intenta modificar evidencia fuente para cerrar contradicción | Denegar mutación; devolver al productor/owner; registrar intento | SoD |

### 7.2 Riesgo, aplicabilidad y veto

| ID | Condición | Resultado esperado | Clase |
|---|---|---|---|
| S-009 | Dominio material NOT_ASSESSED y veto NOT_ASSESSED | No emitir conclusión favorable dependiente | Negativo |
| S-010 | Dominio UNKNOWN pero otra regla de veto está ACTIVE | Aplicar veto en su scope; conservar UNKNOWN como causa secundaria independiente | Múltiples causas |
| S-011 | Dominio NOT_APPLICABLE con justificación y versión de taxonomía | Excluir únicamente ese dominio; no reducir cobertura de otros dominios | Positivo condicionado |
| S-012 | Aplicabilidad APPLICABILITY_UNRESOLVED | No tratar como NOT_APPLICABLE; resolver aplicabilidad | Negativo |
| S-013 | Aplicabilidad APPLICABILITY_CONFLICTED | REVIEW_REQUIRED; no producir NO_VETO_IDENTIFIED por defecto | Negativo |
| S-014 | Riesgo ASSESSED_WITH_FINDINGS sin veto activo | Interpretar severidad/hallazgos según política; no inventar veto ni asumir elegibilidad | Límite |
| S-015 | ASSESSED_NO_MATERIAL_FINDING con cobertura completa y NO_VETO_IDENTIFIED | Continuar gates dependientes; nunca declarar riesgo cero | Positivo condicionado |
| S-016 | NO_VETO_IDENTIFIED sin cobertura de una regla material | Invalidar la conclusión de veto favorable | Negativo |
| S-017 | VETO_REVIEW_REQUIRED por señal creíble, sin confirmación final | Suspender conclusión favorable en scope, sin afirmar condición probada | Negativo |
| S-018 | VETO_ACTIVE para scope concreto | Bloqueo no compensable dentro de ese scope | Negativo |
| S-019 | VETO_ACTIVE solo para retiros | No propagar automáticamente a research o deposit; para TRADE verificar independencia de settlement, financiación, custodia y ruta; dependencia desconocida implica revisión/bloqueo de la unidad dependiente | Scope |
| S-020 | VETO_ACTIVE sobre bridge/wrapped representation | No extender automáticamente al activo nativo no afectado | Scope |
| S-021 | VETO_NOT_ASSESSED y sin señales adversas | No convertir ausencia de señales en NO_VETO_IDENTIFIED | Negativo |
| S-022 | TTL/freshness policy de VetoDecision o evidencia del veto vence | TTL no levanta VETO_ACTIVE. Emitir evento FRESHNESS_EXPIRED/revisión; mantener bloqueo conservador hasta nueva VetoDecision autorizada. Evaluar separadamente freshness de decisión, evidencia y RiskDomainAssessment | Lifecycle |
| S-023 | Veto liberado con evidencia de remediación, revisión independiente y autoridad válida | Nueva VetoDecision versionada; conservar el veto histórico | Positivo condicionado |
| S-024 | Veto levantado por el mismo proponente o por job automático sin autoridad | Rechazar liberación y registrar violación SoD | Gobernanza |

### 7.3 AE, ORE y decisión

| ID | Condición | Resultado esperado | Clase |
|---|---|---|---|
| S-025 | AE favorable para RESEARCH; ORE INELIGIBLE para WITHDRAWAL | Investigación puede continuar; retiro bloqueado | Independencia AE/ORE |
| S-026 | AE favorable; ORE NOT_ASSESSED para TRADE | No habilitar trade ni inferir ruta apta | Negativo |
| S-027 | AE INELIGIBLE por riesgo fundamental; ORE técnicamente elegible | Ruta técnicamente elegible no rescata la selección analítica | Independencia AE/ORE |
| S-028 | ORE ELIGIBLE_WITH_LIMITATIONS con limitación fuera del scope de la orden | Orden denegada o reconfigurada; no ignorar limitación | Enforcement |
| S-029 | ORE favorable pero TradeAuthorization ausente | No ejecutar orden | Autorización |
| S-030 | ORE favorable pero pre-trade control falla | Bloquear orden | Enforcement |
| S-031 | AE y ORE favorables, pero inputs con policy versions incompatibles | Envelope de validación REVIEW_REQUIRED o REJECTED_INVALID_INPUT según causa; no producir outcome favorable ni inventar enum INPUTS_INCONSISTENT. Registrar VERSION_INCOMPATIBLE y conservar decision_type/scope | Integridad |
| S-032 | Un dominio no evaluado afecta solo una finalidad concreta | Bloquear la finalidad dependiente; preservar otras finalidades no dependientes si la política lo permite | Scope |
| S-033 | DECISION recibe causa primaria y tres causas secundarias | Conservar todas; presentación primaria no elimina causas | Determinismo |
| S-034 | Dos órdenes de inputs idénticos, versiones y contexto idénticos | Resultado idéntico y replay reproducible | Determinismo |
| S-035 | Falta input obligatorio | Resultado explícito incompleto/abstención; nunca default favorable | Negativo |
| S-036 | Resultado calculado con un input superseded | Rechazar snapshot o recomponer con versión vigente según policy | Lifecycle |

### 7.4 Lifecycle, freshness e invalidación

| ID | Condición | Resultado esperado | Clase |
|---|---|---|---|
| S-037 | Nueva alerta afecta un dominio y sus dependientes | Invalidar/revisar solo dependencias declaradas; preservar historial | Propagación |
| S-038 | Evento tardío con valid_time anterior al snapshot | Aplicar política point-in-time; no reescribir decisiones históricas silenciosamente | Temporal |
| S-039 | Assessment stale, veto activo vigente | Staleness no elimina veto; mantener bloqueo y reevaluar | Precedencia |
| S-040 | Veto no evaluado y TTL vencido | Sigue no evaluado; TTL no crea ausencia de veto | Precedencia |
| S-041 | Revisión nueva supersede assessment anterior | Nueva versión enlazada; decisiones históricas reproducibles | Versionado |
| S-042 | Dependencia no declarada recibe evento | Registrar dependency-map gap; consumidores potencialmente dependientes no resueltos quedan no favorables (REVIEW_REQUIRED/INCOMPLETE/STALE tipado según causa) hasta reconciliar el grafo; no declarar invalidación completa ni mantener PASS antiguo | Observabilidad |
| S-043 | Repetición del mismo evento de invalidación | Procesamiento idempotente; no duplicar efectos de estado | Idempotencia |
| S-044 | Error parcial de propagación | Estado PARTIALLY_AFFECTED explícito; bloquear uso favorable de todos los consumidores potencialmente afectados cuyo estado no esté reconciliado; retry/reconciliation idempotente; no reportar propagación completa | Fail-closed |
| S-045 | Lifecycle Owner intenta levantar veto directamente | Denegar; derivar a autoridad Veto Release | SoD |
| S-046 | Un evento de policy change altera reglas futuras | Versionar policy; no reescribir decisiones pasadas sin evento de revisión | Gobernanza temporal |

### 7.5 Gobernanza, autorización y SoD

| ID | Condición | Resultado esperado | Clase |
|---|---|---|---|
| S-047 | Productor intenta aprobar su evidencia crítica | Denegar por SoD | Negativo |
| S-048 | Veto Proposer intenta liberar su propio veto | Denegar; exigir autoridad independiente | Negativo |
| S-049 | Solicitante intenta aprobar su excepción | Denegar | Negativo |
| S-050 | Delegación expirada o fuera de scope | Denegar aprobación/acción | Negativo |
| S-051 | Aprobador recusado intenta votar | Denegar y registrar recusación | Negativo |
| S-052 | Aprobación válida, pero objeto cambió de versión antes de ejecutar | Revalidar aprobación contra versión exacta; exigir nueva aprobación si cambió materialmente | TOCTOU |
| S-053 | Service identity intenta aprobar decisión reservada a persona | Denegar. Solo se permite automatización para tipos de decisión expresamente delegables mediante policy aprobada/versionada, owner accountable, scope y límites, control independiente, identidad de servicio y pruebas de bypass; decisiones no delegables siempre denegadas | Negativo |
| S-054 | Break-glass utilizado para contención | Permitir únicamente acciones enumeradas en un catálogo aprobado de control de emergencia, con principal autorizado, scope mínimo, dual control/justificación, expiración, alertas y revisión independiente; nunca habilitar acción comercial ni levantar veto | Excepción |
| S-055 | Break-glass intenta habilitar trade bloqueado | Denegar; break-glass no sustituye TradeAuthorization | Negativo |
| S-056 | Mismo operador ejecuta y reconcilia operación | Denegar certificación unilateral; exigir reconciliación independiente | SoD |
| S-057 | IAM administrator se otorga privilegio de negocio | Denegar; requerir flujo independiente y log externo | SoD |
| S-058 | Excepción expirada permanece cacheada | Enforcement debe denegar tras expiración; cache invalidation comprobada | Temporal |
| S-059 | Aprobación dual realizada por dos cuentas de una misma persona vinculada | Tratar como conflicto de identidad/SoD; no contar como dos aprobadores independientes | SoD |
| S-060 | Auditor intenta modificar decisión auditada | Denegar mutación; acceso de auditoría de solo lectura | Independencia |

## 8. Escenarios combinados de alta criticidad

Los escenarios siguientes prueban interacciones de tres o más dimensiones y tienen prioridad sobre pruebas aisladas.

| ID | Combinación | Resultado esperado |
|---|---|---|
| C-01 | AE favorable + ORE favorable + VETO_ACTIVE para TRADE + TradeAuthorization aprobada | Trade bloqueado por veto; autorización no lo compensa |
| C-02 | AE favorable para RESEARCH + VETO_ACTIVE para WITHDRAWAL + ORE favorable para MARKET_DATA | Research/market data pueden continuar si sus inputs son válidos; withdrawal permanece bloqueado |
| C-03 | RiskDomainStatus STALE + VETO_ACTIVE vigente + excepción expirada | Mantener veto; no aceptar excepción; reevaluar riesgo |
| C-04 | RiskDomainStatus UNKNOWN + NO_VETO_IDENTIFIED + cobertura incompleta | NO_VETO_IDENTIFIED inválido; abstención/revisión para scope dependiente |
| C-05 | AE favorable + ORE ELIGIBLE_WITH_LIMITATIONS + orden excede limitación + dual approval | Denegar orden; doble aprobación no anula límites |
| C-06 | VETO_REVIEW_REQUIRED + decisión analítica previa favorable + evidencia nueva conflictiva | Suspender conclusión favorable en scope y generar nueva evaluación |
| C-07 | Bridge compromised + representación nativa y wrapped mezcladas en un mismo portfolio proposal | Separar representaciones y scopes; bloquear solo exposición/ruta afectada según regla |
| C-08 | Custody incident + invalidación de cuenta + orden pendiente + break-glass | Particionar órdenes/rutas afectadas; ejecutar solo acciones de emergencia explícitamente autorizadas (p. ej. pausa/cancelación si están en catálogo); no inferir reducción/cierre/transferencia; break-glass no habilita trade ni levanta veto; reconciliación independiente |
| C-09 | Lifecycle event tardío + decisión histórica + nueva policy version | Mantener reproducibilidad histórica; nueva evaluación bajo policy nueva |
| C-10 | Veto release request + evidencia de remediación + proponente original como único aprobador disponible | No liberar; mantener bloqueo hasta revisión independiente |
| C-11 | AE y ORE favorables + identidad de activo conflictuada + evidencia de precio de representación incorrecta | Invalidar inputs dependientes; no producir decisión favorable |
| C-12 | Decision Approver autorizado + conflicto de interés no declarado detectado posteriormente | Poner decisión en revisión, preservar auditoría y aplicar política de remediación |
| C-13 | Portfolio proposal cumple límites nominales + exposición agregada stale + pre-trade control PASS antiguo | Rechazar autorización con snapshot stale; recalcular controles |
| C-14 | Dos vetos concurrentes con scopes parcialmente solapados y causas distintas | Conservar ambos; bloquear unión de scopes aplicables sin extender a zonas no afectadas |
| C-15 | Un dominio NOT_APPLICABLE por taxonomía desactualizada + evento que cambia clasificación | Invalidar aplicabilidad dependiente y reevaluar dominios afectados |
| C-16 | Aprobación dual válida + cambio de tamaño/venue/cuenta tras aprobar | Invalidar aprobación para objeto/contexto modificado; volver a autorizar |

## 9. Reglas para construir la futura suite

### 9.1 No usar solo el producto cartesiano

El producto cartesiano completo de todos los estados produciría una cantidad enorme de combinaciones, muchas imposibles o redundantes. La suite debe combinar:
- cobertura de cada estado individual;
- cobertura de cada transición permitida y prohibida;
- pairwise/combinatorial testing entre dimensiones;
- cobertura obligatoria de cada interacción de alto riesgo;
- escenarios multi-causa y scope parcial;
- pruebas metamórficas e invariantes;
- regresión de cada hallazgo y cada bug corregido.

La reducción combinatoria no puede eliminar escenarios con vetos activos, estados desconocidos, conflictos de scope, invalidación, excepciones, autorización o SoD.

### 9.2 Clases de prueba

1. **Positive:** combinación válida que permite continuar una finalidad o actividad concreta.
2. **Negative:** combinación que debe bloquear o abstenerse.
3. **Boundary:** límites de TTL, timestamps, scope, importes, expiración y transición.
4. **Conflict:** evidencias, políticas, estados o scopes incompatibles.
5. **Multi-cause:** causas concurrentes con precedencia y conservación de causas.
6. **Temporal:** eventos tardíos, point-in-time, stale, superseded e invalidación.
7. **Authorization/SoD:** principal, rol, delegación, recusación, dual control y service identity.
8. **Metamorphic:** pequeñas transformaciones de input que no deben alterar un resultado no relacionado.
9. **Replay:** reconstrucción exacta del resultado a partir de snapshot y versiones.
10. **Failure injection:** fallo parcial, timeout, duplicación, orden de eventos y pérdida de dependencia.

### 9.3 Requisitos de cada fixture futuro

Cada fixture deberá contener:
- scenario_id único y estable;
- propósito y riesgo cubierto;
- input snapshot completo o referencia reproducible;
- versiones de schemas, catálogos y policies;
- reloj controlado y timestamps;
- roles/principals y relaciones de SoD;
- expected outcome por finalidad/actividad;
- causas primarias y secundarias esperadas;
- scope permitido y bloqueado;
- explicación de por qué no se permite un resultado más favorable;
- propiedad/invariante que verifica;
- categoría positive/negative/boundary/etc.;
- owner de revisión y estado de aprobación.

Los fixtures usarán datos sintéticos. No deben incluir secretos, claves privadas, credenciales ni datos personales reales.

## 10. Invariantes transversales verificables

- **INV-01 Fail-closed:** falta de input obligatorio nunca produce PASS.
- **INV-02 No risk laundering:** UNKNOWN/STALE/CONFLICTED/INVALID no se convierten en riesgo bajo.
- **INV-03 No veto laundering:** VETO_NOT_ASSESSED no se convierte en NO_VETO_IDENTIFIED.
- **INV-04 Veto non-compensation:** score, ranking, aprobación o presupuesto no compensan VETO_ACTIVE.
- **INV-05 Scope confinement:** veto solo se propaga por regla explícita y scope coincidente.
- **INV-06 AE/ORE independence:** cambiar ORE no cambia AE si los inputs analíticos permanecen iguales, y viceversa.
- **INV-07 No trade implication:** ORE favorable no genera TradeAuthorization.
- **INV-08 No stale authorization:** autorización y control expiran/invalida según contexto y policy.
- **INV-09 No TTL release:** TTL no levanta veto activo ni crea NO_VETO_IDENTIFIED.
- **INV-10 Source immutability:** consumidor DECISION no muta assessment fuente.
- **INV-11 Cause preservation:** agregar una causa aplicable no elimina causas anteriores ni mejora el resultado.
- **INV-12 Determinism:** mismo snapshot, versiones, policy y contexto producen mismo resultado.
- **INV-13 Historical reproducibility:** decisiones históricas se reconstruyen con versiones históricas.
- **INV-14 Idempotency:** procesar el mismo evento repetido no duplica efectos.
- **INV-15 SoD enforcement:** identidad vinculada no obtiene independencia por usar cuentas distintas.
- **INV-16 No self-approval:** principal no aprueba su propia acción cuando se exige segregación.
- **INV-17 Delegation scope:** delegación expirada/fuera de scope no autoriza.
- **INV-18 Exception expiry:** excepción expirada no habilita la acción.
- **INV-19 No break-glass bypass:** emergencia no equivale a autorización de inversión/trading.
- **INV-20 Audit independence:** auditoría no altera objetos ni resultados auditados.
- **INV-21 Applicability integrity:** NOT_APPLICABLE requiere justificación y versión taxonómica.
- **INV-22 Partial propagation honesty:** fallo parcial no se informa como invalidación completa y los consumidores potencialmente afectados sin reconciliar no conservan un resultado favorable utilizable.
- **INV-23 Version coherence:** snapshots incompatibles no se mezclan para elegir resultado favorable.
- **INV-24 No hidden override:** toda intervención manual produce decisión separada, actor, razón, scope y expiración.
- **INV-25 Typed outcomes:** DecisionOutcome, RouteEligibilityStatus, VetoDecision y validation envelope no se intercambian ni usan abreviaturas ambiguas.
- **INV-26 Veto freshness separation:** la expiración de riesgo, evidencia o política no levanta por sí sola un VETO_ACTIVE; su vigencia se evalúa por objeto y dependencia.
- **INV-27 Dependency uncertainty:** consumidores potencialmente afectados por dependencias no resueltas no reciben resultado favorable hasta reconciliación.
- **INV-28 Emergency action confinement:** break-glass solo ejecuta acciones de control enumeradas y autorizadas; nunca crea autorización comercial.
- **INV-29 Service identity least authority:** una identidad de servicio no ejerce decisiones no delegables ni aprueba fuera de policy aprobada.

## 11. Criterios de cobertura y métricas

Antes de aceptar una suite futura, exigir:
- 100% de cobertura de estados canónicos por dimensión, incluidos estados no favorables, demostrada mediante inventarios versionados y crosswalks;
- 100% de cobertura de transiciones permitidas y prohibidas identificadas;
- 100% de invariantes con al menos un test verificable;
- 100% de escenarios combinados críticos C-01 a C-16;
- cobertura explícita de cada tipo de veto y cada familia de actividad relevante;
- cobertura de scopes exactos, parciales, solapados y no coincidentes;
- cobertura de expiración, stale, evento tardío, duplicado y fallo parcial;
- cobertura de SoD, excepciones, delegaciones, recusación y break-glass;
- replay determinista de fixtures aprobados;
- trazabilidad bidireccional scenario → regla → requisito → componente → test.

La cobertura porcentual de líneas de código no sustituye la cobertura semántica. Un 100% de line coverage no demuestra conformidad de política.

## 12. Reglas de aprobación y control de cambios

1. Cada resultado esperado requiere aprobación de los owners de la dimensión afectada.
2. Riesgo/veto requiere aprobación de Risk; scope legal, de Compliance/Legal; operación, de Operations; seguridad, de Security; interfaces, de Architecture.
3. Escenarios SoD requieren revisión de Governance y Audit, preservando independencia de Audit.
4. Discrepancias se registran como findings; no se resuelven seleccionando el resultado más favorable.
5. Cambios de catálogo/policy/expected outcome incrementan versión y generan revisión de regresión.
6. Los fixtures aprobados se tratan como baseline versionada; no se reescriben para hacer pasar una implementación.
7. Excepciones de conformidad deben tener scope, razón, owner, aprobador, controles compensatorios y expiración.
8. No se autoriza implementación por el mero hecho de publicar esta matriz.

## 13. Dependencias pendientes y limitaciones

Antes de convertir esta matriz en un contrato ejecutable deben resolverse:
- aprobación semántica final de catálogos de estados y transiciones;
- reconciliación exhaustiva entre vocabularios legacy y canónicos;
- confirmación de reglas de aplicabilidad por tipología y dominios;
- aprobación de matriz de propagación de vetos y scopes;
- definición final de estados/outcomes de AE y decisión;
- confirmación de algoritmo de composición y precedencia;
- parámetros TTL/freshness por dominio y uso;
- asignación institucional real de roles, quorum, delegaciones y excepciones;
- definición de los contratos de input/output y versionado;
- aprobación de las autoridades de cada escenario.

Esta propuesta no prueba que los documentos X-01 a X-08 estén aprobados ni que sus reglas sean ejecutables. Algunos resultados son deliberadamente condicionales a una policy todavía pendiente.

## 14. Criterios de aceptación del documento

- Cada dimensión está separada y tiene estados/objetos identificables.
- La matriz base incluye aplicabilidad, riesgo, veto y resultados dependientes.
- La propagación por finalidad/actividad respeta scope explícito.
- Escenarios aislados y combinados cubren estados favorables, desfavorables, incompletos, conflictos y límites.
- Lifecycle, point-in-time, invalidación, expiración e idempotencia están cubiertos.
- Gobernanza y SoD forman parte de la conformidad, no son controles externos opcionales.
- Los invariantes son verificables y trazables.
- Se evita el producto cartesiano indiscriminado sin omitir combinaciones críticas.
- Se define un proceso de aprobación de expected outcomes y cambios.
- No se afirma implementación ni validación empírica.

## 15. Estado

**DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

Se trata de una especificación documental de escenarios y resultados esperados. No se crearon fixtures, schemas ni pruebas ejecutables; no se ejecutaron tests, backtests ni validaciones empíricas. No constituye aprobación de requisitos IIM ni autorización para producción o trading.

No modificar `requirements/requirements-register.md`, requisitos normativos, schemas o código hasta recibir aprobación explícita de la matriz y de sus dependencias.


## 16. Resoluciones de auditoría A01-A08 y ampliación de cobertura (v0.2.0-draft)

Esta sección forma parte de la revisión 0.2.0. Sus reglas son propuestas sujetas a aprobación; no convierten X-01..X-08 en normas aprobadas ni sustituyen sus catálogos fuente.

### 16.1 A01 — Namespaces y outcomes tipados

Los siguientes tipos son independientes y no intercambiables:

| Tipo | Catálogo de referencia | Uso |
|---|---|---|
| DecisionOutcome | DECISION_NOT_ASSESSED, DECISION_IN_PROGRESS, DECISION_REVIEW_REQUIRED, DECISION_INELIGIBLE, DECISION_ABSTAIN, DECISION_ELIGIBLE_WITH_LIMITATIONS, DECISION_ELIGIBLE | Resultado analítico por unidad/finalidad |
| RouteEligibilityStatus | Los 10 estados ROUTE_* de X-04 | Resultado ORE por ruta y actividad |
| VetoDecision | VETO_ACTIVE, VETO_REVIEW_REQUIRED, VETO_NOT_ASSESSED, NO_VETO_IDENTIFIED | Decisión de veto por regla/scope |
| RiskDomainStatus | Los 8 estados de X-01/X-03 | Estado del assessment de dominio |
| ValidationEnvelope | VALID, INCOMPLETE, REVIEW_REQUIRED, REJECTED_INVALID_INPUT | Admisibilidad/validación de inputs de composición |
| ValuationStatus | ESTIMABLE, ESTIMABLE_WITH_LIMITATIONS, NOT_ESTIMABLE, INSUFFICIENT_EVIDENCE, NOT_RUN | Estado independiente de valoración |

No utilizar VETOED, INELIGIBLE, REVIEW_REQUIRED o INPUTS_INCONSISTENT sin indicar el tipo. En escenarios, un resultado debe identificar decision_type, purpose/activity, scope_id, atomic_unit_id y validation_envelope. El envelope puede impedir que se emita un DecisionOutcome/RouteEligibilityStatus nuevo; no lo sustituye.

### 16.2 A02/A06 — Freshness por objeto y ciclo de revisión del veto

La evaluación temporal debe mantener al menos cuatro referencias separadas: evidencia subyacente, RiskDomainAssessment, VetoDecision y policy/version que gobierna su vigencia. Un evento FRESHNESS_EXPIRED afecta solo los objetos y consumidores definidos por dependency graph y freshness policy.

Reglas:
1. TTL vencido nunca convierte VETO_ACTIVE en NO_VETO_IDENTIFIED ni libera el scope.
2. Si la VetoDecision y su evidencia siguen vigentes, la obsolescencia de un RiskDomainAssessment no invalida automáticamente esa decisión.
3. Si la evidencia material que sustenta el veto se retira, compromete o invalida, abrir revisión del veto; mantener bloqueo conservador en el scope plausible hasta adjudicación.
4. Si vence la vigencia/review date del propio veto, crear revisión obligatoria y mantener bloqueo; no borrar ni sobrescribir la decisión histórica.
5. Solo una nueva VetoDecision emitida por Veto Release Authority con remediación verificada, revisión independiente y policy válida puede cambiar el bloqueo.
6. Registrar freshness_status/lifecycle_status fuera del enum VetoDecision. No añadir un estado “expired veto” que se confunda con liberación.

### 16.3 A03 — Crosswalk de dominios de riesgo y tipologías

Cobertura de escenarios existente y ampliada. La asignación indica pruebas de interacción, no demuestra que todos los subcontroles estén probados.

| Catálogo X-02 | Escenarios de cobertura mínima |
|---|---|
| RSK-01 Protocol & Smart Contract | S-020, S-061, S-067 |
| RSK-02 Cybersecurity & Key Management | S-062, S-068, C-08 |
| RSK-03 Consensus & Network | S-063, S-069 |
| RSK-04 Economic Design & Tokenomics | S-064, S-070 |
| RSK-05 Market, Liquidity & Market Integrity | S-065, S-071, S-072 |
| RSK-06 Financial & Counterparty | S-066, S-073 |
| RSK-07 Custody & Operational | S-045, S-074, C-08 |
| RSK-08 Governance & Control | S-024, S-075, S-076 |
| RSK-09 Legal & Regulatory | S-077, S-078 |
| RSK-10 Data Integrity & Market Abuse | S-002, S-004, S-079 |
| RSK-11 Bridge & Interoperability | S-020, S-061, S-067 |
| RSK-12 Environmental & External Dependencies | S-080 |

| Tipología X-02 | Escenarios de cobertura mínima |
|---|---|
| CAT-01 NATIVE_NETWORK_ASSET | S-063, S-064 |
| CAT-02 SMART_CONTRACT_TOKEN | S-061, S-067 |
| CAT-03 STABLECOIN_OR_REFERENCED_VALUE | S-066, S-073 |
| CAT-04 WRAPPED_OR_BRIDGED_ASSET | S-020, S-061 |
| CAT-05 LIQUID_STAKING_OR_RESTAKING | S-068, S-070 |
| CAT-06 DEFI_PROTOCOL_OR_POSITION | S-067, S-071 |
| CAT-07 EXCHANGE_OR_PLATFORM_TOKEN | S-065, S-075 |
| CAT-08 GOVERNANCE_OR_UTILITY_TOKEN | S-075, S-076 |
| CAT-09 PRIVACY_OR_SPECIALIZED_NETWORK_ASSET | S-077, S-080 |

Una tipología puede coexistir con otras; las pruebas combinadas deben preservar todas las etiquetas aplicables. Esta tabla es cobertura mínima de tipologías, no cobertura exhaustiva de todas las combinaciones CAT × RSK.

### 16.4 A03 — Crosswalk de clases de veto

| Clase X-05 | Escenarios de cobertura mínima |
|---|---|
| V-01 Asset / rights / fraud | S-081 |
| V-02 Protocol / contract | S-061, S-067 |
| V-03 Network / consensus | S-063, S-069 |
| V-04 Bridge / wrapped representation | S-020, S-061 |
| V-05 Venue / market integrity | S-065, S-071 |
| V-06 Counterparty / issuer / reserve | S-066, S-073 |
| V-07 Custody / account / key control | S-074, C-08 |
| V-08 Legal / sanctions / jurisdiction | S-077, S-078 |
| V-09 Liquidity / settlement / market access | S-071, S-072, S-082 |
| V-10 Data / oracle / evidence integrity | S-002, S-079 |

### 16.5 A03 — Crosswalk de estados ORE

| Estado ORE X-04 | Escenario de cobertura |
|---|---|
| ROUTE_NOT_ASSESSED | S-026 |
| ROUTE_ASSESSMENT_IN_PROGRESS | S-083 |
| ROUTE_ELIGIBLE | S-029, S-034 |
| ROUTE_ELIGIBLE_WITH_LIMITATIONS | S-028 |
| ROUTE_INELIGIBLE | S-025, S-027 |
| ROUTE_REVIEW_REQUIRED | S-084 |
| ROUTE_INSUFFICIENT_INFORMATION | S-026, S-035 |
| ROUTE_STALE | S-003, S-039 |
| ROUTE_CONFLICTED | S-004, S-031 |
| ROUTE_INVALID | S-007, S-036 |

Los escenarios citados son objetivos de cobertura: antes de convertirlos en fixtures se deben fijar inputs, scope y expected outcomes exactos. No se afirma que la cobertura esté probada por la sola existencia de la referencia.

### 16.6 A04 — Dependencias de settlement y actividad

Un veto exclusivo de WITHDRAWAL no bloquea TRADE por simple equivalencia de actividades. Sin embargo, el resultado TRADE solo puede continuar cuando el grafo de dependencias demuestra que financiación, settlement, finality, custodia, rutas de salida y controles posteriores son independientes del servicio suspendido. Si la dependencia es desconocida o no está modelada, la unidad operativa dependiente recibe REVIEW_REQUIRED/INSUFFICIENT_INFORMATION y no puede recibir autorización favorable. Añadir S-082 como prueba de frontera.

### 16.7 A05 — Fail-closed ante dependencia desconocida o propagación parcial

Para S-042/S-044:
- consumidores confirmados como no afectados pueden conservar su evaluación si la independencia está demostrada;
- consumidores afectados reciben invalidación/revisión en el scope correspondiente;
- consumidores potencialmente afectados pero no resueltos quedan no favorables hasta reconciliación;
- el sistema informa PARTIALLY_AFFECTED y no declara propagación completa;
- retries son idempotentes y registran dependency graph version, consumer set, error y resultado de reconciliación.

No se permite mantener un PASS/ELIGIBLE anterior para un consumidor cuya dependencia material está sin resolver.

### 16.8 A07 — Catálogo de acciones break-glass

Este documento no concede permisos. La siguiente lista es un esquema para aprobación separada de Governance, Security, Risk y Operations:

| Acción de emergencia | Default propuesto | Condición |
|---|---|---|
| PAUSE_NEW_ORDERS | Permitida solo si se delega explícitamente | Incidente/scope/autoridad/expiración |
| CANCEL_OPEN_ORDERS | No implícita; requiere permiso explícito | Política de cancelación, protección de cliente y registro |
| REDUCE_ONLY | Denegada por defecto | Solo autoridad de riesgo y política específica |
| CLOSE_POSITION | Denegada por defecto | Autoridad específica, ruta segura y autorización |
| TRANSFER_ASSETS | Denegada por defecto | Nunca habilitada por break-glass genérico |
| RELEASE_VETO | Prohibida vía break-glass | Workflow Veto Release separado |
| ENABLE_TRADING | Prohibida vía break-glass | TradeAuthorization independiente |

Toda acción permitida requiere principal autenticado, incidente, motivo, scope mínimo, policy_id/version, ventana temporal, doble control o justificación de imposibilidad, logging externo, alerta y revisión posterior. La ausencia de policy aprobada significa DENY.

### 16.9 A08 — Aprobación por service identity

S-053 debe evaluar policy_id/version, acción exacta, tipo de objeto, scope, límite, entorno, owner accountable, aprobador institucional, independencia del workflow, identidad/credenciales propias, evidencia de ejecución y expiración. Automatización solo puede aprobar tareas expresamente delegables. Se prohíbe automatizar: liberación de vetos críticos, dispensas legales, cambios de apetito/límites no delegables, aprobación de su propia evidencia o aprobación de su propio cambio de policy. Si la clasificación delegable no existe o está sin aprobar, DENY.

### 16.10 Escenarios adicionales para cobertura explícita

| ID | Condición | Resultado esperado | Dimensión |
|---|---|---|---|
| S-061 | Contrato/token bridged con upgrade/admin exploit | Evaluar RSK-01/RSK-11; bloquear representación y funciones dependientes; no extender al nativo sin dependencia | Protocol/bridge |
| S-062 | Clave de validador, signer o administrador comprometida | RSK-02; scope a claves, cuentas, contratos y activos expuestos; revisión de consumidores dependientes | Cyber/key |
| S-063 | Halt/fork/finality failure de una red | RSK-03; bloquear transferencias/settlement dependientes en esa red; no extender a redes independientes | Network |
| S-064 | Emisión/unlock/supply restatement material | RSK-04; invalidar tokenomics/valoración dependientes y recomponer con point-in-time | Tokenomics |
| S-065 | Wash trading/manipulación en venue específico | RSK-05/RSK-10; excluir datos afectados, mantener fuentes independientes verificadas | Market integrity |
| S-066 | Emisor/reservas/contraparte insolvente o no verificable | RSK-06; distinguir evidencia insuficiente de incumplimiento probado; aplicar veto solo según regla | Counterparty |
| S-067 | Protocolo DeFi o contrato de posición explotado | RSK-01/RSK-11 según dependencia; aislar contrato/posición y rutas relacionadas | Protocol |
| S-068 | Staking/restaking depende de operadores o claves comprometidas | RSK-02/RSK-07; bloquear exposición y rutas que dependan de control comprometido | Custody/security |
| S-069 | Red depende de proveedor/consenso externo interrumpido | RSK-03/RSK-12; revisar disponibilidad y finality en scope | External dependency |
| S-070 | Token liquid-staking cambia derechos, redemption queue o exchange rate | RSK-04/RSK-06; valuation NOT_ESTIMABLE o limitada según evidencia independiente | Tokenomics/rights |
| S-071 | Liquidez insuficiente para lado/tamaño/ventana concretos | RSK-05; limitar solo scope medido; no extrapolar a todos los tamaños | Liquidity |
| S-072 | Settlement/withdrawal suspendido y trade usa la misma ruta de salida | TRADE no favorable por dependencia demostrada; si no se conoce, REVIEW_REQUIRED | Settlement |
| S-073 | Stablecoin/reserve/redemption no verificable o suspendida | RSK-06; separar paridad de mercado, solvencia del emisor y derecho de redención | Stablecoin |
| S-074 | Custody/account/key control comprometido | RSK-07; bloquear acciones dependientes de cuenta/custodio; preservar investigación independiente | Custody |
| S-075 | Exchange/platform token depende de entidad/venue con conflicto de gobernanza | RSK-08/RSK-06; evaluación por entidad, poder y obligaciones | Governance |
| S-076 | Cambio de quorum, multisig, timelock o control administrativo | RSK-08; invalidar assessment de gobernanza dependiente | Governance |
| S-077 | Restricción legal/sanción aplica a sujeto, actividad y jurisdicción precisos | RSK-09; bloquear solo conducta dentro del alcance legal aplicable; revisión competente | Legal |
| S-078 | Privacidad/activo especializado cambia clasificación regulatoria según jurisdicción | CAT-09/RSK-09; APPLICABILITY_UNRESOLVED hasta análisis competente | Legal/taxonomy |
| S-079 | Oráculo/feed comprometido alimenta precio, NAV o collateral | RSK-10; invalidar solo cálculos/decisiones dependientes y exigir fuente alternativa independiente | Oracle/data |
| S-080 | Dependencia ambiental/proveedor/geográfica crítica concentrada | RSK-12; evaluar materialidad y continuidad, no emitir veto automático por etiqueta | External |
| S-081 | Fraude de identidad/derechos o activo falso con ticker similar | V-01; bloquear representación falsa; no contaminar activos homónimos | Asset identity |
| S-082 | Veto WITHDRAWAL; TRADE depende de la misma ruta de settlement/salida | TRADE recibe REVIEW_REQUIRED/ROUTE_INSUFFICIENT_INFORMATION o ROUTE_INELIGIBLE según evidencia y regla; no autorizar | Activity dependency |
| S-083 | ORE assessment se inició y permanece en curso | ROUTE_ASSESSMENT_IN_PROGRESS; no operar | ORE lifecycle |
| S-084 | Alerta creíble sobre ruta/venue requiere adjudicación | ROUTE_REVIEW_REQUIRED; suspender conclusión favorable en scope | ORE review |

### 16.11 A09/A10 — Inventario de transiciones y trazabilidad

Antes de aprobación de la futura suite, cada máquina de estados deberá publicar un inventario versionado con: state_machine_id/version, estado origen, evento/guard, actor/autoridad, estado destino, side effects, scope, TTL/review trigger, transición prohibida y regla de recuperación. Los escenarios de esta matriz no prueban por sí solos todas las transiciones.

Crosswalk mínimo de trazabilidad:
- INV-01..INV-29 → escenarios S/C específicos → regla fuente X-01..X-08 → requisito IIM aplicable → futuro test_id.
- B-01..B-15 y P-01..P-06 → regla fuente y decisión expected.
- Cada S/C → dimensión, catálogo, finalidad/actividad, scope, causa y test futuro.
- Todo requisito sin test o escenario se marca GAP; no se infiere cobertura.

No se declaran porcentajes de cobertura hasta que los inventarios de estados/transiciones y el crosswalk estén completos, revisados y aprobados.

## 17. Registro de resolución de auditoría A01-A08

| Hallazgo | Cambio propuesto en 0.2.0 | Estado |
|---|---|---|
| A01 | Namespaces explícitos, tabla de tipos, corrección B-05/S-031 | ADDRESSED_IN_DRAFT |
| A02 | Freshness separado para evidencia/riesgo/veto/policy; B-03/S-022 ampliados | ADDRESSED_IN_DRAFT |
| A03 | Crosswalks RSK, CAT, Veto classes y ORE states; S-061..S-084 | ADDRESSED_IN_DRAFT |
| A04 | P-01/S-019/S-082 incluyen dependencia de settlement | ADDRESSED_IN_DRAFT |
| A05 | S-042/S-044 exigen estado no favorable para consumidores no reconciliados | ADDRESSED_IN_DRAFT |
| A06 | Expiración de veto genera revisión y mantiene bloqueo hasta nueva decisión autorizada | ADDRESSED_IN_DRAFT |
| A07 | Catálogo break-glass propuesto con DENY por defecto para acciones no aprobadas | ADDRESSED_IN_DRAFT |
| A08 | Automatización de service identity limitada a acciones delegables aprobadas | ADDRESSED_IN_DRAFT |

**Importante:** ADDRESSED_IN_DRAFT significa que el texto de la propuesta incorpora una respuesta al hallazgo. No significa que el hallazgo haya sido aceptado por sus owners, que la matriz esté aprobada, que exista implementación o que se hayan ejecutado pruebas.



## 18. Trazabilidad y catálogo de transiciones (v0.3.0)

La resolución documental de A09 y A10 se desarrolla en el documento complementario:

[`16-x09-traceability-and-state-transition-inventory.md`](16-x09-traceability-and-state-transition-inventory.md)

Este documento complementario contiene:
- Crosswalk inicial de S-001..S-084 y C-01..C-16 hacia reglas fuente, dimensiones, requisitos IIM candidatos y futuros test IDs.
- Crosswalk INV-01..INV-29 hacia escenarios y reglas.
- Inventario SM-01..SM-12 con estados de cobertura y brechas.
- Transiciones fuente explícitas de VetoDecision y eventos/reglas lifecycle.
- Transiciones prohibidas, métricas y criterios de aceptación.

**Estado:** A09/A10 ADDRESSED_IN_DRAFT. El crosswalk requiere expansión por escenario y validación por Requirement Owners. El inventario revela que varias máquinas (incluidas RiskDomainStatus, ORE, AE, SelectionDecision, Governance y TradeAuthorization) carecen todavía de grafos exhaustivos. No se declara cobertura completa ni aprobación. No se han creado ni ejecutado tests.


## 19. A11 — Escenarios de valoración wrapped (v0.4.0-draft)

La especificación detallada está en [17-x09-wrapped-valuation-and-atomic-aggregation.md](17-x09-wrapped-valuation-and-atomic-aggregation.md).

| ID | Input/condición | Expected outcome tipado | Regla |
|---|---|---|---|
| S-085 | No se identifica contrato/representación o derechos del holder | ValuationStatus.INSUFFICIENT_EVIDENCE; no emitir estimación | A11 W-01 |
| S-086 | Derechos identificados; bridge comprometido; redención/paridad material no verificable | ValuationStatus.NOT_ESTIMABLE; VetoDecision separado; bloquear conclusión favorable dependiente | A11 W-02 |
| S-087 | Solo precio secundario del wrapper, sin modelo defendible del claim | ValuationStatus.NOT_ESTIMABLE; precio queda como market data, no fair value | A11 W-03 |
| S-088 | Modelo independiente defendible para claim recuperable delimitado, con haircut/escenarios; bridge comprometido | ValuationStatus.ESTIMABLE_WITH_LIMITATIONS; no libera veto ni selección automáticamente | A11 W-04 |
| S-089 | Derechos, respaldo/redención, fuentes independientes y método aplicable verificados | ValuationStatus.ESTIMABLE; decisión consume veto/riesgo y demás gates | A11 W-05 |
| S-090 | Valoración no iniciada | ValuationStatus.NOT_RUN; no inferir insuficiencia ni inelegibilidad | A11 W-06 |
| S-091 | Wrapper comprometido y token nativo independiente con evidencia suficiente | ValuationStatus separado por representation_id; wrapper según evidencia; nativo evaluado independientemente | A11 W-07 |

## 20. A12 — Partición atómica y agregación (v0.4.0-draft)

| ID | Input/condición | Expected outcome por unidad | Agregación/garantía |
|---|---|---|---|
| S-092 | Wrapper chain-A con veto; token nativo chain-A independiente | Wrapper DECISION_INELIGIBLE; native DECISION_ELIGIBLE si sus gates están satisfechos | No producir DECISION_ELIGIBLE global; conservar vector por unidad |
| S-093 | Wrapper chain-A en review; wrapper chain-B independiente y elegible | Chain-A DECISION_REVIEW_REQUIRED; chain-B DECISION_ELIGIBLE | Scope completo no favorable; resultado local de chain-B permanece identificable |
| S-094 | Dos vetos solapados afectan misma unidad | Outcome por precedencia de X-06; ambas causas y scopes preservados | Resolver un veto no elimina el otro |
| S-095 | Subconjunto permitido y excluido, con límites identificables y enforcement efectivo | Outcomes por unidad y control de subset | DECISION_ELIGIBLE_WITH_LIMITATIONS solo si policy lo permite y enforcement está garantizado |
| S-096 | Restricción parcial no puede aislarse ni hacerse cumplir | REVIEW/ABSTAIN/INELIGIBLE según causa demostrada; no favorable global | No usar LIMITATION de palabra |
| S-097 | Varias rutas heterogéneas; una elegible y otra ineligible | ORE separado por route_id | Matriz por ruta; no ORE global ROUTE_ELIGIBLE |
| S-098 | Dependencia de settlement cruza unidad aparentemente no afectada | SCOPE_UNRESOLVED / review o bloqueo de unidad dependiente | No asumir independencia; resolver grafo de dependencias |
| S-099 | Unidades disjuntas, exhaustivas, homogéneas y todas elegibles | DECISION_ELIGIBLE por cada unidad | Agregación favorable solo para requested_scope exactamente cubierto |

### 20.1 Expected outcome ampliado de C-07

Para el portfolio proposal que mezcla representación nativa y wrapped:
1. Particionar por canonical_asset_id + representation_id + network/bridge cuando sean materiales.
2. La unidad wrapped afectada por V-04 recibe el resultado de veto y decisión aplicable a ese scope.
3. La unidad nativa solo se bloquea si existe dependencia causal documentada; de lo contrario se evalúa independientemente.
4. No se agrega un único score ni un outcome favorable global que oculte la unidad bloqueada.
5. Mantener todos los VetoDecision y sus scopes.

### 20.2 Expected outcome ampliado de C-14

Para vetos concurrentes con scopes parcialmente solapados:
1. Crear unidades disjuntas para intersección, región exclusiva del veto A, región exclusiva del veto B y región no afectada, cuando existan.
2. En la intersección, conservar ambas causas y aplicar la consecuencia más restrictiva según X-06.
3. En cada región exclusiva, aplicar únicamente el veto cuyo scope coincida.
4. En región no afectada, no propagar los vetos; siguen siendo necesarias las demás evaluaciones.
5. Agregar solo unidades compatibles; el summary nunca elimina el desglose ni concede permiso global implícito.

### 20.3 Invariantes adicionales

- **INV-30 Wrapped valuation separation:** ValuationStatus no sustituye VetoDecision ni DecisionOutcome.
- **INV-31 Representation non-transitivity:** resultado wrapper no se extrapola a native ni viceversa sin dependencia causal.
- **INV-32 Valuation evidence semantics:** INSUFFICIENT_EVIDENCE, NOT_ESTIMABLE y NOT_RUN conservan semánticas distintas.
- **INV-33 Atomic partition integrity:** las unidades cubren exactamente requested_scope y son disjuntas.
- **INV-34 Cause conservation:** ninguna causa se pierde al particionar o agregar; resolver una causa no elimina otras.
- **INV-35 No heterogeneous global permission:** rutas/unidades heterogéneas conservan resultados separados; el summary no concede elegibilidad global.

**Estado A11/A12:** ADDRESSED_IN_DRAFT; pendiente revisión de Valuation, Risk, Architecture, Operations y Legal/Compliance. Los escenarios S-085..S-099 son especificaciones de prueba futuras, no tests ejecutables ni resultados validados.
