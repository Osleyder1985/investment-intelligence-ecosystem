# Matriz integrada de escenarios de conformidad

**ID:** IIE-METH-CONFORMANCE-010  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo:** X-09 (MEDIUM), auditoría cruzada M-02/M-03/M-04  
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
| B-03 | APPLICABLE | STALE | NO_VETO_IDENTIFIED antiguo | Invalidar conclusión dependiente; reevaluar veto y riesgo |
| B-04 | APPLICABLE | CONFLICTED | VETO_REVIEW_REQUIRED | REVIEW_REQUIRED en scope |
| B-05 | APPLICABLE | ASSESSED_WITH_FINDINGS | VETO_ACTIVE | VETOED en scope aplicable |
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
| P-01 | Suspensión exclusiva de WITHDRAWAL | No bloquear por inferencia | No bloquear por inferencia | No bloquear por inferencia | No bloquear por inferencia | No bloquear por inferencia | Bloquear/revisar según veto |
| P-02 | Compromiso de bridge para representación wrapped | Investigación del activo nativo sigue posible | Valorar representación afectada con cautela | Bloquear/revisar feeds/ruta afectados | Bloquear representación/ruta afectada | Según scope de representación | Según scope de representación |
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
| S-019 | VETO_ACTIVE solo para retiros | No propagar automáticamente a research o deposit; aplicar tabla M-10 | Scope |
| S-020 | VETO_ACTIVE sobre bridge/wrapped representation | No extender automáticamente al activo nativo no afectado | Scope |
| S-021 | VETO_NOT_ASSESSED y sin señales adversas | No convertir ausencia de señales en NO_VETO_IDENTIFIED | Negativo |
| S-022 | VETO_ACTIVE recibe TTL vencido | El veto no se levanta; revisar vigencia y estado, mantener bloqueo hasta decisión autorizada | Lifecycle |
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
| S-031 | AE y ORE favorables, pero inputs con policy versions incompatibles | REVIEW_REQUIRED/INPUTS_INCONSISTENT; no elegir versión favorable | Integridad |
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
| S-042 | Dependencia no declarada recibe evento | Registrar gap de dependency map; no declarar invalidación completa | Observabilidad |
| S-043 | Repetición del mismo evento de invalidación | Procesamiento idempotente; no duplicar efectos de estado | Idempotencia |
| S-044 | Error parcial de propagación | Estado parcial explícito, retry/reconciliation; no reportar propagación completa | Fail-closed |
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
| S-053 | Service identity intenta aprobar decisión reservada a persona | Denegar salvo política explícita y aprobación automatizada permitida | Negativo |
| S-054 | Break-glass utilizado para contención | Permitir solo acción de emergencia delimitada; alerta, expiración y revisión posterior | Excepción |
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
| C-08 | Custody incident + invalidación de cuenta + orden pendiente + break-glass | Aplicar suspensión de acciones afectadas; break-glass solo contención; reconciliación independiente |
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
- **INV-22 Partial propagation honesty:** fallo parcial no se informa como invalidación completa.
- **INV-23 Version coherence:** snapshots incompatibles no se mezclan para elegir resultado favorable.
- **INV-24 No hidden override:** toda intervención manual produce decisión separada, actor, razón, scope y expiración.

## 11. Criterios de cobertura y métricas

Antes de aceptar una suite futura, exigir:
- 100% de cobertura de estados canónicos por dimensión, incluidos estados no favorables;
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
