# Lifecycle, freshness e invalidación transversal

**ID:** IIE-METH-LIFECYCLE-008  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo:** X-07 (MEDIUM), auditoría cruzada M-02/M-03/M-04  
**Dependencias:** documentos metodológicos 03–11, especialmente estados, AE/ORE, riesgo/veto, propagación y composición determinista.

## 1. Propósito y límites

Definir un modelo transversal para creación, vigencia, expiración, invalidación, supersession, reevaluación y propagación de cambios en evidencias, assessments y decisiones. El objetivo es evitar que una conclusión que fue válida en un momento anterior continúe consumiéndose como vigente después de un cambio material.

Este documento es una propuesta metodológica. No asigna TTL numéricos universales, no aprueba políticas de dominio, no modifica requisitos IIM, no implementa un event bus ni un motor de dependencias, y no demuestra conformidad, seguridad o validez empírica.

La política concreta de frescura depende de la variable, fuente, volatilidad, finalidad, actividad, criticidad, jurisdicción y consecuencias del uso. No se debe inventar un único TTL para todos los datos o decisiones.

## 2. Principios de lifecycle

1. **Historial append-only:** no borrar ni reescribir eventos, evidencias o decisiones históricas.
2. **Vigencia explícita:** cada assessment declara su intervalo o condición de validez y sus triggers de reevaluación.
3. **Frescura contextual:** freshness se calcula por dominio, uso, scope y timestamp de evaluación.
4. **Invalidación selectiva:** un evento afecta únicamente objetos que dependen de él, según relaciones explícitas; impactos inciertos requieren revisión.
5. **No autorización por defecto:** vencimiento o ausencia de eventos no equivale a una nueva evaluación favorable.
6. **No levantamiento automático de vetos:** TTL, silencio, paso del tiempo o supersession no retiran VETO_ACTIVE.
7. **Reevaluación versionada:** toda reevaluación genera un nuevo objeto/evento, enlazado al anterior.
8. **Propagación trazable:** cada cambio derivado identifica el evento causante, camino de dependencia y política aplicada.
9. **Idempotencia y orden:** eventos duplicados o fuera de orden no deben producir transiciones divergentes.
10. **Separación de tiempo:** distinguir cuándo un hecho era válido en el mundo y cuándo el sistema lo conoció o registró.
11. **Fail closed y scope limitado:** los objetos dependientes no pueden continuar como vigentes si falta evidencia material; no se invalida todo el universo sin justificación.
12. **Reversibilidad controlada:** una corrección posterior genera un nuevo evento y nueva evaluación; no elimina el hecho histórico de que hubo invalidación.

## 3. Objetos y estados de lifecycle

El lifecycle se aplica a objetos distintos y no debe colapsarlos en un status universal:

- EvidenceRecord / DataObservation;
- IdentityAssessment;
- ApplicabilityAssessment;
- RiskDomainAssessment;
- VetoDecision;
- AnalyticalEligibilityAssessment (AE);
- OperationalRouteAssessment (ORE);
- ValuationAssessment;
- SelectionDecision;
- RankingSnapshot;
- Portfolio/Execution assessment cuando corresponda.

Cada tipo de objeto declara su máquina de estados y reglas de transición. El sobre transversal registra eventos y relaciones, no reemplaza estados de dominio.

### 3.1 Estados del expediente de evaluación

Catálogo candidato basado en M-02:

| Estado | Significado |
|---|---|
| NOT_STARTED | No se inició el expediente |
| IN_PROGRESS | Evaluación en curso |
| COMPLETE | Módulos requeridos completados; no implica resultado favorable |
| REVIEW_REQUIRED | Requiere adjudicación o revisión antes de una conclusión definida |
| SUPERSEDED | Reemplazado por un nuevo expediente/versión |
| INVALIDATED | Perdió validez por un evento, error o evidencia invalidante |

COMPLETE no equivale a ELIGIBLE. SUPERSEDED e INVALIDATED son terminales para esa versión de registro; una nueva evaluación crea un objeto nuevo.

### 3.2 Estados de vigencia y admisibilidad

No usar un único estado para representar simultáneamente lifecycle, calidad de evidencia y decisión. Como mínimo, distinguir:

- **Freshness:** FRESH, APPROACHING_EXPIRY, STALE, NOT_APPLICABLE.
- **Evidence admissibility:** ADMISSIBLE, ADMISSIBLE_WITH_LIMITATIONS, QUARANTINED, REJECTED, UNKNOWN, NOT_ASSESSED.
- **Lifecycle:** ACTIVE, SUPERSEDED, INVALIDATED, WITHDRAWN (cuando el tipo y la política permitan retiro explícito).
- **Assessment domain status:** estados propios de identidad, riesgo, aplicabilidad, veto, AE y ORE definidos en sus documentos.

FRESH significa dentro de la política de vigencia para ese uso, no necesariamente de alta calidad. STALE significa que ya no satisface la condición temporal/contextual de uso. INVALIDATED significa que un evento explícito rompió la validez del assessment/resultado. SUPERSEDED significa que una versión nueva lo reemplaza para usos actuales; el anterior se conserva para replay.

APPROACHING_EXPIRY es una señal operativa de planificación, no un outcome favorable/negativo ni una sustitución de FRESH.

## 4. Modelo temporal

### 4.1 Dos ejes temporales

Cada registro/evento debe distinguir:

- valid_time: intervalo o instante durante el cual el hecho/evidencia se considera válido en el dominio de origen;
- recorded_at: instante en que el sistema recibió o registró el hecho;
- observed_at: instante de observación por la fuente/sistema, si difiere;
- evaluated_at: instante explícito utilizado para evaluar freshness y componer una decisión;
- effective_at: instante en que una transición o invalidación produce efecto, si es distinto de recorded_at.

Esta separación permite manejar eventos tardíos: una alerta recibida hoy puede referirse a un incidente ocurrido antes. No reescribir evaluaciones históricas; crear un evento retroactivo y determinar qué resultados históricos quedan afectados para usos futuros o auditoría.

Todos los timestamps deben incluir zona/offset o normalizarse a UTC con la conversión original preservada. No inferir zona horaria.

### 4.2 Intervalos de validez

Un assessment puede definir valid_from, valid_until (opcional si existe trigger/evento en vez de TTL fijo), freshness_policy_id y versión, reassessment_triggers[], review_due_at y last_verified_at.

Un assessment sin TTL fijo debe declarar qué eventos o condiciones invalidan su uso y cómo se comprueba que sigue vigente. La ausencia de valid_until no puede interpretarse como validez perpetua.

### 4.3 Freshness por uso

La misma observación puede ser fresca para investigación histórica y obsoleta para una decisión de trading en tiempo real. Freshness se evalúa para la combinación:

object + field/domain + source + scope + intended_use + evaluated_at + freshness_policy_version.

No copiar un is_fresh global a todos los consumidores.

## 5. Política de freshness y TTL

### 5.1 Requisitos de política

Cada política de freshness debe definir:

- identificador, versión, owner y aprobación;
- tipo de objeto/campo y fuente cubierta;
- finalidad/actividad permitida;
- scope y tipologías de activo;
- TTL máximo y/o regla de renovación;
- warning window (si aplica);
- reloj de referencia y tratamiento de retrasos;
- calidad mínima de fuente y condiciones de continuidad;
- triggers de invalidación inmediata;
- comportamiento ante ausencia de actualización;
- gracia permitida, si existe, con justificación;
- efecto en consumidores dependientes;
- SLA/priority de reevaluación;
- evidencia y pruebas que respaldan el TTL;
- fecha de entrada en vigor y compatibilidad/migración.

### 5.2 No universalidad de TTL

No fijar en esta especificación valores como “24 horas para todo” o “7 días para todo”. Una política puede necesitar TTL muy corto para precio/order book, diferente para supply/circulating supply, incidentes de seguridad, gobernanza, reservas, licencias o datos históricos. Los valores deben calibrarse por riesgo, volatilidad, cadencia de fuente, latencia y coste de error.

### 5.3 Categorías de criticidad propuestas

- CRITICAL_REAL_TIME: un retraso pequeño puede cambiar materialmente una decisión operativa.
- HIGH_VOLATILITY: cambia con frecuencia o puede sufrir shocks.
- EVENT_DRIVEN: la vigencia depende principalmente de eventos materiales.
- PERIODIC: se renueva por calendario o publicación periódica.
- SLOW_MOVING: cambia lentamente, pero exige verificación periódica.
- HISTORICAL_IMMUTABLE: dato histórico que no se actualiza, aunque puede corregirse la atribución/calidad.

Estas categorías ayudan a diseñar políticas; no determinan TTL numérico automáticamente.

### 5.4 Freshness no equivale a confianza

Un dato reciente puede ser falso, manipulado o de baja calidad. Un dato antiguo puede seguir siendo válido para análisis histórico. Quality, provenance, integrity, admissibility y freshness son dimensiones separadas.

## 6. Eventos de lifecycle

Catálogo candidato, extensible y versionado:

| Evento | Significado |
|---|---|
| OBJECT_CREATED | Se crea un objeto |
| EVIDENCE_RECEIVED | Llega una observación/evidencia |
| EVIDENCE_CORRECTED | La fuente corrige una observación previa |
| SOURCE_WITHDRAWN | La fuente retira una publicación/afirmación |
| SOURCE_COMPROMISED | La fuente o credencial se considera comprometida |
| FRESHNESS_WARNING | Se aproxima vencimiento según policy |
| FRESHNESS_EXPIRED | Se supera la condición TTL/renovación |
| MATERIAL_CHANGE_DETECTED | Cambio material que puede afectar assessments |
| CONFLICT_DETECTED | Se detectan evidencias incompatibles |
| INTEGRITY_FAILURE | Falla hash, firma, schema, secuencia o integridad |
| ASSESSMENT_STARTED | Comienza reevaluación |
| ASSESSMENT_COMPLETED | Termina assessment nuevo |
| REVIEW_OPENED / REVIEW_CLOSED | Se abre/cierra revisión formal |
| VETO_ISSUED | Se emite veto tras verificación |
| VETO_REMEDIATION_PROPOSED | Se declara una posible remediación |
| VETO_REMEDIATION_VERIFIED | La remediación se verifica con evidencia y autoridad |
| POLICY_CHANGED | Cambia política, catálogo, TTL o método |
| DEPENDENCY_CHANGED | Cambia una relación de dependencia |
| OBJECT_SUPERSEDED | Se crea reemplazo versionado |
| OBJECT_INVALIDATED | Se invalida el objeto dentro de scope definido |
| REASSESSMENT_REQUESTED | Se solicita reevaluación |
| REASSESSMENT_DEFERRED | Reevaluación aplazada con motivo y control |
| MANUAL_OVERRIDE_REQUESTED / APPROVED / REJECTED | Flujo controlado de excepción, si la política lo permite |

Un evento no produce automáticamente un estado de dominio. VETO_REMEDIATION_PROPOSED no levanta un veto; SOURCE_WITHDRAWN no significa que el dato alternativo sea correcto; POLICY_CHANGED no modifica retroactivamente decisiones sin un proceso explícito de migración/replay.

## 7. Triggers de invalidación

### 7.1 Clases de trigger

1. Data correction: corrección de una observación fuente que alimentó un assessment.
2. Source withdrawal/compromise: retirada, hack, manipulación o pérdida de fiabilidad de la fuente.
3. Identity change: cambio de contrato, migración, redenominación, rebranding, wrapper, chain split o resolución de identidad.
4. Security incident: exploit, vulnerabilidad, key compromise, bridge incident, upgrade crítico.
5. Governance/protocol change: upgrade, cambio de parámetros, control administrativo o token rights.
6. Economic/tokenomics change: emisión, unlock, supply restatement, fee/revenue model, backing/reserve change.
7. Market/venue change: suspensión, delisting, deterioro de liquidez, manipulación, contraparte comprometida.
8. Legal/regulatory change: sanción, prohibición, licencia, restricción jurisdiccional o cambio de estatus.
9. Custody/operational change: incidente de custodia, cuenta, red, settlement, withdrawal/deposit.
10. Method/policy change: cambio de metodología, threshold, taxonomía, modelo, TTL o catálogo.
11. Integrity/schema change: corrupción, firma inválida, incompatibilidad de schema o secuencia.
12. Temporal expiry: vencimiento de TTL o falta de renovación requerida.

### 7.2 Efectos por clase

El trigger debe declarar event_id y source; tipo, severidad, confianza y estado de verificación; affected entity/scope; valid_time y recorded_at; evidencia; dependencias conocidas; si la invalidación es inmediata, provisional o requiere adjudicación; consumers afectados y política aplicada; actor/owner y plazo de revisión.

Una señal creíble aún no verificada puede abrir REVIEW_REQUIRED o QUARANTINED dentro del scope plausible, sin declarar automáticamente un hecho probado ni propagarlo a entidades no relacionadas.

## 8. Grafo de dependencias y propagación

### 8.1 Relación explícita

Cada objeto derivado debe registrar dependencias explícitas con:

- upstream_object_id/version;
- downstream_object_id/version;
- dependency_type (REQUIRED, MATERIAL, OPTIONAL, CONTEXTUAL);
- scope transform / mapping;
- intended use;
- propagation rule/version;
- dependency owner;
- creation timestamp y validity.

No basta con que dos documentos mencionen el mismo activo para inferir una dependencia.

### 8.2 Propagación dirigida

Ante un evento:
1. resolver el objeto y scope de origen;
2. validar evento, integridad y confianza;
3. identificar relaciones downstream vigentes;
4. aplicar reglas de scope y materialidad;
5. clasificar cada dependencia: AFFECTED, PARTIALLY_AFFECTED, NOT_AFFECTED, UNRESOLVED;
6. generar invalidation/reassessment events para dependencias afectadas;
7. recalcular únicamente las decisiones cuya conclusión material pueda cambiar;
8. preservar los objetos históricos y el camino de propagación.

Si la dependencia o scope no puede resolverse, no declarar no afectado. Marcar DEPENDENCY_UNRESOLVED y abrir revisión/abstención para la conclusión dependiente.

### 8.3 Reglas de aislamiento

- AE y ORE son grafos relacionados, pero separados: un incidente exclusivo de venue/custodia no invalida AE por defecto.
- Un incidente de identidad/contrato puede afectar múltiples módulos analíticos y rutas que dependan de esa representación.
- Un cambio de precio/order book puede invalidar market analysis, liquidity/costs y decisiones operativas sensibles al mercado, pero no necesariamente la identidad o análisis histórico.
- Un evento de bridge se propaga a representación wrapped y rutas dependientes; no se extiende al activo nativo sin dependencia demostrada.
- Un cambio de política puede exigir replay o reevaluación, pero no cambia el hecho histórico ni sustituye la versión de policy usada originalmente.
- Un veto permanece dentro del scope declarado y la matriz X-05; lifecycle no amplía su alcance.

## 9. Reglas de transición y resolución

### 9.1 STALE

Un objeto pasa a STALE cuando incumple la política de freshness aplicable. No significa que la información sea falsa ni que se haya demostrado un incumplimiento. El consumidor debe abstenerse de conclusiones que requieran frescura, o usar explícitamente una fuente alternativa validada.

### 9.2 INVALIDATED

Un objeto pasa a INVALIDATED cuando un evento identificado demuestra que el resultado ya no debe utilizarse para su scope/uso, por ejemplo error material, fuente comprometida, identidad errónea o dependencia rota. Registrar causa, scope, effective_at y downstream impact.

### 9.3 SUPERSEDED

Un objeto pasa a SUPERSEDED cuando una versión posterior reemplaza su función actual. La versión antigua continúa disponible para auditoría/replay y no se borra.

### 9.4 REVIEW_REQUIRED / QUARANTINED

Se usa cuando existe señal o conflicto material no resuelto. No se declara un hecho negativo como probado si la evidencia solo justifica investigación. Las restricciones provisionales deben estar limitadas en scope, tener owner, rationale, revisión y expiración/review_at.

### 9.5 Resolución y remediación

La resolución exige nueva evidencia admisible y verificable; evaluación del mismo scope y dependencias afectadas; revisión independiente cuando la criticidad lo exija; cierre documentado del incidente/review; nueva versión de assessment y decisión; relación supersedes y evento causante.

No se permite editar el assessment invalidado para cambiar su estado histórico. Un veto activo solo puede levantarse por el proceso de transición de VetoDecision y autoridad definida en la política correspondiente, nunca por el lifecycle engine de forma automática.

## 10. Reevaluación y priorización

Cada trigger genera una solicitud de reevaluación con prioridad determinada por criticidad del dominio, materialidad para decisión/actividad, alcance de exposición y número de consumidores, severidad/confianza del evento, cercanía a expiración, impacto potencial si se consume dato obsoleto, disponibilidad de alternativa segura, SLA y capacidad operativa.

Clases candidatas:
- P0: posible riesgo inmediato de seguridad, sanciones, identidad o control operativo; bloquear uso dependiente hasta triage.
- P1: cambio material en fundamento, tokenomics, mercado o contraparte; revisión urgente.
- P2: actualización periódica/TTL próximo sin señal adversa; reevaluación planificada.
- P3: mejora no urgente, revisión de calidad o deuda de evidencia.

Las prioridades no sustituyen políticas de riesgo ni crean vetos por sí mismas. Cada solicitud incluye owner, due_at, estado, causa y decisión temporal de uso mientras se evalúa.

## 11. Idempotencia, orden y concurrencia

1. Cada evento tiene event_id globalmente único e idempotency_key.
2. Se conserva source_event_id, source sequence/version y payload digest.
3. Duplicados exactos no vuelven a aplicar efectos; discrepancias bajo mismo id se tratan como conflicto de integridad.
4. Eventos se ordenan por secuencia confiable de la fuente cuando exista, y por reglas de causalidad; recorded_at no basta para inferir orden causal.
5. Eventos tardíos se admiten y generan evaluación de impacto retroactivo.
6. Transiciones incompatibles concurrentes producen CONFLICTED/REVIEW_REQUIRED; no gana arbitrariamente el último evento recibido.
7. Los retries deben ser seguros y no duplicar invalidaciones ni evaluaciones.
8. La proyección de estado debe poder reconstruirse desde el log de eventos y versiones de políticas.
9. Un evento compensatorio es un nuevo evento; no se borra el evento original.
10. Las reglas de deduplicación, secuencia y conflicto deben estar versionadas y probadas.

## 12. Registro de evento y registro de invalidación

### 12.1 Campos mínimos de LifecycleEvent

event_id; event_type y event_schema_version; aggregate_type, aggregate_id y aggregate_version; source_id, source_event_id y source_sequence; occurred_at / valid_time; observed_at y recorded_at; effective_at cuando aplique; scope; severity, confidence y verification_status; evidence_refs y payload_digest; causation_id / correlation_id; idempotency_key; actor/process identity; policy_version y algorithm_version; security classification y access control metadata.

### 12.2 Campos mínimos de InvalidationRecord

invalidation_id; trigger_event_id; target_object_id/version; target_scope y intended_use; invalidation_type (STALE, INVALIDATED, SUPERSEDED, REVIEW_REQUIRED); reason_code y rationale; dependency_path[]; propagation_policy_version; affected_consumers[]; effective_at, recorded_at, review_due_at; owner/reviewer; reassessment_status y superseding_object_id; audit references.

No exponer datos sensibles innecesarios en eventos generales; usar referencias controladas y permisos de mínimo privilegio.

## 13. Contrato con X-06 y decisiones consumidoras

X-07 alimenta el algoritmo X-06 mediante estados/versiones y eventos, pero no sustituye su precedencia:

- STALE en input material → no emitir outcome favorable dependiente; ANALYTICAL_SELECTION normalmente ABSTAIN y ORE normalmente STALE/INSUFFICIENT_INFORMATION según catálogo aprobado.
- INVALIDATED → no consumir el assessment invalidado; reabrir evaluación downstream.
- SUPERSEDED → usar versión sucesora solo si es compatible, vigente y de scope equivalente; si no, REVIEW/ABSTAIN.
- REVIEW_REQUIRED → suspender conclusión favorable en el scope afectado.
- FRESH no significa que el contenido sea favorable ni elimina veto.
- Evento no propagado a un consumidor requiere registro de NOT_AFFECTED con regla y evidencia; no es una afirmación global de ausencia de riesgo.

Un consumidor debe rechazar referencias a versiones INVALIDATED/SUPERSEDED para decisiones actuales, salvo replay histórico explícito.

## 14. Observabilidad y controles operativos

Métricas mínimas:
- proporción de assessments FRESH/STALE por dominio;
- expiraciones y reevaluaciones atrasadas;
- eventos por tipo, fuente y severidad;
- tiempo desde evento hasta triage, invalidación y reevaluación;
- cantidad y antigüedad de dependencias no resueltas;
- decisiones downstream afectadas y consumidores notificados;
- eventos duplicados, fuera de orden y conflictivos;
- overrides solicitados/aprobados/rechazados;
- falsos positivos/falsos negativos detectados en invalidación;
- fallos de propagación, retries y dead-letter events.

Alertas deben tener umbral, owner, severidad, runbook y evidencia. El sistema debe detectar que un consumer sigue usando una versión invalidada o vencida. La telemetría no debe filtrar secretos, claves, credenciales o datos personales.

## 15. Escenarios de aceptación

| # | Escenario | Resultado esperado |
|---|---|---|
| 1 | TTL de order book vence | Datos stale para uso de mercado/ejecución; no invalida identidad por defecto |
| 2 | Dato de supply corregido por fuente | Evento de corrección; identificar y reevaluar valoraciones/decisiones dependientes |
| 3 | Fuente comprometida | Cuarentena/invalidation del scope afectado y trazado downstream |
| 4 | Incidente de custodia en venue A | ORE de rutas/cuentas dependientes afectada; AE independiente permanece |
| 5 | Bridge comprometido | Wrapped representation y rutas dependientes afectadas; nativo solo si dependencia probada |
| 6 | Veto activo llega a TTL | No levantar veto por expiración; revisión explícita requerida |
| 7 | Veto remediation proposal | Mantener veto activo hasta verificación y transición autorizada |
| 8 | Assessment nuevo sustituye antiguo | Antiguo SUPERSEDED, nuevo enlazado; historial reproducible |
| 9 | Evento retroactivo recibido tarde | Recalcular impacto histórico y vigencia presente sin reescribir registros |
| 10 | Dos fuentes contradicen identidad | CONFLICTED/REVIEW_REQUIRED; no escoger la última recibida |
| 11 | Policy TTL cambia | Versionar política y determinar replay/migración; no reescribir resultados previos |
| 12 | Evento duplicado exacto | Idempotencia; un único efecto lógico |
| 13 | Mismo event_id con payload distinto | Conflicto de integridad; no aplicar arbitrariamente |
| 14 | Dependencia no resoluble | Marcar DEPENDENCY_UNRESOLVED; abstenerse/revisar downstream |
| 15 | Evento de precio cambia market view | Invalidar solo módulos y decisiones que dependan materialmente de ese dato |
| 16 | Fuente deja de publicar sin aviso | Aplicar TTL/renewal policy; no presumir vigencia indefinida |
| 17 | Consumer usa assessment INVALIDATED | Rechazar para uso actual y alertar |
| 18 | Consumer usa assessment histórico en replay | Permitir solo con modo replay, timestamp y policy originales explícitos |
| 19 | Evento de gobernanza afecta parámetros | Reevaluar dominios de protocolo/tokenomics y rutas dependientes |
| 20 | Evento afecta una jurisdicción | Propagación limitada a jurisdicción/actividad cuando el scope legal así lo establezca |
| 21 | Reevaluación no puede completarse dentro del SLA | Mantener estado restrictivo/abstención y escalar owner; no auto-PASS |
| 22 | Evento compensatorio corrige señal errónea | Registrar nuevo evento; solo nueva evaluación puede restaurar estado favorable |

## 16. Invariantes de conformidad

- L-01: ningún objeto histórico se borra o reescribe al invalidarse.
- L-02: FRESH no implica calidad ni resultado favorable.
- L-03: ausencia de actualización no implica validez perpetua.
- L-04: vencimiento no levanta VETO_ACTIVE.
- L-05: invalidación se propaga solo por dependencias y scopes demostrables.
- L-06: incertidumbre de dependencia produce revisión/abstención, no NOT_AFFECTED.
- L-07: AE y ORE conservan lifecycle y scopes separados.
- L-08: todo resultado derivado referencia sus upstream versions.
- L-09: un objeto SUPERSEDED no se usa en decisiones actuales salvo replay explícito.
- L-10: eventos duplicados exactos son idempotentes.
- L-11: eventos contradictorios no se resuelven por orden de llegada.
- L-12: eventos tardíos permiten análisis de impacto retroactivo.
- L-13: reevaluación crea nueva versión; no edita el registro anterior.
- L-14: políticas de freshness son versionadas y específicas por uso.
- L-15: cualquier override tiene autoridad, scope, justificación, vigencia y auditoría.
- L-16: los consumidores detectan y rechazan versiones inválidas/obsoletas para usos actuales.
- L-17: eventos y registros respetan seguridad, clasificación y mínimo privilegio.
- L-18: replay usa eventos, catálogos y políticas originales.

## 17. Cuestiones abiertas para revisión

1. ¿Qué dominios necesitan event-time streaming y cuáles pueden usar reevaluación batch?
2. ¿Qué TTL/renewal policy se justifica por campo, fuente, finalidad, actividad y tipología? Requiere evidencia empírica y owners de dominio.
3. ¿Cómo se versiona y mantiene el grafo de dependencias y sus transformaciones de scope?
4. ¿Qué eventos activan invalidación inmediata versus provisional review/quarantine?
5. ¿Qué SLAs y escalaciones se asignan a P0–P3 por dominio y criticidad?
6. ¿Qué fuentes/autoridades pueden emitir eventos vinculantes y cómo se autentican?
7. ¿Cómo se implementan replay, idempotency, ordering, retries y dead-letter handling?
8. ¿Qué controles de retención, privacidad, confidencialidad y acceso aplican a evidencia y eventos?
9. ¿Qué cambios de policy requieren replay histórico, evaluación prospectiva o ambas?
10. ¿Qué roles pueden cerrar reviews, invalidar assessments y levantar vetos? Debe coordinarse con X-08 (RACI/SoD).
11. ¿Cómo se evita que un consumer siga usando datos cacheados después de invalidación?
12. ¿Qué métricas y pruebas medirán falsos positivos/negativos de invalidación?

## 18. Criterios de aceptación X-07

- [ ] Estados lifecycle, freshness, admissibility y domain assessment separados.
- [ ] Tiempo válido, observado, registrado, evaluado y efectivo diferenciados.
- [ ] Política de freshness definida por dominio/uso y versionada; sin TTL universal arbitrario.
- [ ] Triggers de invalidación tipificados y con scope.
- [ ] Grafo de dependencias y reglas de propagación explícitos.
- [ ] Separación AE/ORE preservada.
- [ ] Transiciones de stale, invalidated, superseded, review y recovery inequívocas.
- [ ] Veto activo no se levanta por TTL ni por automatismo de lifecycle.
- [ ] Idempotencia, eventos fuera de orden y conflictos especificados.
- [ ] Reevaluación y replay históricos versionados.
- [ ] Registro de evento e invalidación con provenance y audit trail.
- [ ] Observabilidad, SLA, escalamiento y consumer cache invalidation revisados.
- [ ] Revisión de Architecture, Data Engineering, Risk, Operations, Security y Legal/Compliance según aplique.
- [ ] Aprobación explícita antes de schemas, código o cambios normativos.

## 19. Estado y limitaciones

X-07 queda descrito como propuesta documental para revisión. No se han definido TTL numéricos por dominio, creado schemas, implementado event processing/dependency graph, ni ejecutado pruebas de lifecycle, carga, seguridad o replay. No se modifica requirements/requirements-register.md ni requisitos IIM. La publicación no cierra X-07 ni acredita implementación, pruebas o validación empírica.
