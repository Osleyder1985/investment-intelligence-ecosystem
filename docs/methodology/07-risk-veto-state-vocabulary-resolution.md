# Resolución de vocabulario: RiskDomainStatus y VetoDecision

**ID:** IIE-METH-STATE-RES-001  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgos:** X-01 y X-03  
**Fecha:** 2026-10-31

## 1. Decisión semántica propuesta

Se adopta una separación estricta entre el estado del conocimiento de riesgo y el resultado de la política de veto. Son objetos diferentes, con vocabularios y ciclos de vida distintos.

- `RiskDomainStatus` responde: ¿qué sabemos y cuál es el estado de evaluación de este dominio de riesgo?
- `VetoDecision` responde: ¿se evaluó la política de veto aplicable a este scope y qué decisión produjo?

Por tanto, `RISK_UNKNOWN` no pertenece a `VetoDecision`. El estado correspondiente a una política de veto no evaluada es `VETO_NOT_ASSESSED`.

## 2. Vocabulario canónico propuesto

### 2.1 RiskDomainStatus

| Estado | Significado | Uso permitido |
|---|---|---|
| NOT_ASSESSED | Evaluación no iniciada o no realizada | Nunca favorable |
| ASSESSMENT_IN_PROGRESS | Evaluación en curso | No usar como resultado final |
| ASSESSED_WITH_FINDINGS | Evaluación completada con hallazgos | Interpretar findings por separado |
| ASSESSED_NO_MATERIAL_FINDING | Evaluación suficiente sin hallazgo material identificado en scope | No significa riesgo cero |
| UNKNOWN | Evidencia insuficiente para concluir | Bloquea conclusiones dependientes |
| STALE | Evaluación fuera de vigencia | Reevaluación requerida |
| CONFLICTED | Evidencia material incompatible no resuelta | Revisión requerida |
| INVALID | Evaluación inválida por integridad, método o datos | No consumir como resultado vigente |

La aplicabilidad es un atributo separado: `APPLICABLE`, `NOT_APPLICABLE`, `APPLICABILITY_UNRESOLVED` o `APPLICABILITY_CONFLICTED`. NOT_APPLICABLE exige regla taxonómica, justificación y versión. Nunca representa falta de datos.

### 2.2 VetoDecision

| Estado | Significado | Consecuencia |
|---|---|---|
| VETO_ACTIVE | Condición crítica verificada y vigente | Bloquea el scope/actividad definidos |
| VETO_REVIEW_REQUIRED | Señal creíble, contradicción o remediación pendiente | Suspende conclusión favorable en el scope |
| VETO_NOT_ASSESSED | Política de veto no evaluada para el scope | No permite inferir ausencia de veto |
| NO_VETO_IDENTIFIED | Reglas y dominios materiales revisados con evidencia suficiente; no se identificó veto | Permite continuar gates, no declara seguridad |

`VETO_NOT_ASSESSED` no significa que exista un evento adverso. Significa que no se ha completado el juicio de política. `VETO_REVIEW_REQUIRED` requiere una señal o circunstancia concreta que justifique revisión; no es el default para toda falta de datos.

## 3. Reglas de composición

1. RiskDomainStatus y VetoDecision se publican en campos separados y con IDs/versiones propios.
2. Un dominio `UNKNOWN` o `NOT_ASSESSED` no se renombra ni se copia como estado de veto.
3. Si un dominio material es desconocido y una regla de veto depende de él, la decisión de esa regla no puede ser `NO_VETO_IDENTIFIED`; será `VETO_NOT_ASSESSED`, salvo que exista una señal concreta que exija `VETO_REVIEW_REQUIRED`.
4. `NO_VETO_IDENTIFIED` requiere cobertura suficiente de todas las reglas obligatorias y dominios materiales aplicables al scope. Un dominio `NOT_APPLICABLE` solo se excluye con justificación taxonómica.
5. `VETO_ACTIVE` prevalece sobre todos los estados favorables, scores y rankings.
6. `VETO_REVIEW_REQUIRED` prevalece sobre `VETO_NOT_ASSESSED` y `NO_VETO_IDENTIFIED` para el scope afectado.
7. `VETO_NOT_ASSESSED` prevalece sobre `NO_VETO_IDENTIFIED` cuando una regla obligatoria no ha sido evaluada.
8. La precedencia de veto es: `VETO_ACTIVE` > `VETO_REVIEW_REQUIRED` > `VETO_NOT_ASSESSED` > `NO_VETO_IDENTIFIED`.
9. La precedencia de veto no sustituye el estado de riesgo por dominio. DECISION evalúa ambas dimensiones.
10. Un cambio de RiskDomainStatus no actualiza automáticamente VetoDecision. La política debe reevaluarse explícitamente y generar evento/versionado.
11. Si los objetos tienen scopes, snapshots, policy versions o timestamps incompatibles, DECISION devuelve `REVIEW_REQUIRED`/`INPUTS_INCONSISTENT`; no selecciona el estado más favorable.
12. Los consumidores no pueden degradar o editar el estado fuente.

## 4. Tabla de decisión mínima

| RiskDomainStatus | VetoDecision | Resultado mínimo para una conclusión dependiente |
|---|---|---|
| NOT_ASSESSED | VETO_NOT_ASSESSED | RISK_INCOMPLETE; abstenerse |
| UNKNOWN | VETO_NOT_ASSESSED | RISK_INCOMPLETE; abstenerse |
| STALE | VETO_NOT_ASSESSED | RISK_INCOMPLETE; reevaluar |
| CONFLICTED | VETO_REVIEW_REQUIRED | REVIEW_REQUIRED |
| ASSESSED_WITH_FINDINGS | VETO_ACTIVE | VETOED en el scope afectado |
| ASSESSED_NO_MATERIAL_FINDING | NO_VETO_IDENTIFIED | Continuar gates; no declarar riesgo cero |
| NOT_APPLICABLE justificado | No se aplica regla relacionada | Excluir del denominador de cobertura de esa regla; conservar justificación |
| Cualquier estado | VETO_ACTIVE | VETOED; no compensable |
| Cualquier estado | VETO_REVIEW_REQUIRED | REVIEW_REQUIRED en scope afectado |
| Cualquier estado | VETO_NOT_ASSESSED | No emitir conclusión favorable dependiente |

La tabla es una regla mínima; no reemplaza la matriz de aplicabilidad por tipología ni la matriz de propagación por finalidad/actividad que se resolverán en X-05.

## 5. Transiciones de VetoDecision

- `VETO_NOT_ASSESSED` → `VETO_REVIEW_REQUIRED`: surge señal material/alerta que exige investigación.
- `VETO_NOT_ASSESSED` → `NO_VETO_IDENTIFIED`: reglas obligatorias y dominios materiales se evalúan con evidencia suficiente.
- `NO_VETO_IDENTIFIED` → `VETO_REVIEW_REQUIRED`: nueva alerta creíble o evidencia contradictoria.
- `VETO_REVIEW_REQUIRED` → `VETO_ACTIVE`: condición crítica verificada y vigente.
- `VETO_REVIEW_REQUIRED` → `NO_VETO_IDENTIFIED`: investigación documentada descarta la condición en el scope.
- `VETO_ACTIVE` → `VETO_REVIEW_REQUIRED`: se alega remediación o surge evidencia que requiere validación; el bloqueo permanece.
- `VETO_ACTIVE` → `NO_VETO_IDENTIFIED`: remediación y cierre verificados, con revisión independiente cuando corresponda.

No hay transición automática de VetoDecision a `RISK_UNKNOWN`. Si evidencia de riesgo se deteriora, se actualiza RiskDomainStatus; si esto invalida la conclusión de veto, se actualiza VetoDecision a `VETO_NOT_ASSESSED` o `VETO_REVIEW_REQUIRED` mediante una nueva evaluación y justificación. Un veto activo no se levanta por TTL.

## 6. Migración desde vocabularios previos

| Legacy | Dominio de destino | Regla de migración |
|---|---|---|
| RISK_UNKNOWN usado como riesgo por dominio | RiskDomainStatus.UNKNOWN | Mantener el estado de riesgo; retirar su uso como VetoDecision |
| RISK_UNKNOWN usado como falta de evaluación | RiskDomainStatus.NOT_ASSESSED o UNKNOWN | Elegir según si no se inició evaluación o existe evidencia insuficiente; no migrar ambiguamente |
| RISK_UNKNOWN usado como veto no evaluado | VetoDecision.VETO_NOT_ASSESSED | Migrar solo si el registro representa explícitamente la evaluación de la política de veto |
| VETO_ACTIVE | VetoDecision.VETO_ACTIVE | Mantener scope, regla, evidencia y vigencia |
| VETO_REVIEW_REQUIRED | VetoDecision.VETO_REVIEW_REQUIRED | Mantener alerta, motivo y scope |
| NO_VETO_IDENTIFIED | VetoDecision.NO_VETO_IDENTIFIED | Revalidar cobertura antes de aceptar el estado |

La migración debe conservar valor original, documento/origen, versión, motivo de transformación, timestamp y referencias. No usar reemplazo global de cadenas ni alias silencioso. Registros legacy ambiguos quedan en cuarentena de migración y requieren revisión humana.

## 7. Cambios de sincronización requeridos

- M-02: retirar `RISK_UNKNOWN` de VetoDecision, incorporando `VETO_NOT_ASSESSED`; conservar `RISK_UNKNOWN` exclusivamente en RiskDomainStatus. Añadir `ASSESSMENT_IN_PROGRESS` e `INVALID` al catálogo de RiskDomainStatus o documentar explícitamente su exclusión.
- M-04: mantener la separación definida en sus secciones 6–9; alinear las transiciones y casos de aceptación con esta resolución.
- `docs/risk/veto-policy.md`: sustituir RISK_UNKNOWN en el enum/precedencia/transiciones de veto por VETO_NOT_ASSESSED; describir RiskDomainStatus aparte.
- `docs/decision/decision-gates-and-outcomes.md`: separar la entrada de riesgo por dominio de la entrada VetoDecision; distinguir `RISK_ASSESSMENT_INCOMPLETE` de `VETO_NOT_ASSESSED`.
- M-03: consumir estados de riesgo/veto por scope sin fusionarlos con ORE ni TradeAuthorization.

## 8. Criterios de aceptación X-01/X-03

- [ ] Ningún enum de VetoDecision contiene RISK_UNKNOWN.
- [ ] VETO_NOT_ASSESSED está definido en todos los consumidores de VetoDecision.
- [ ] RiskDomainStatus.UNKNOWN/NOT_ASSESSED conservan significado propio.
- [ ] NO_VETO_IDENTIFIED requiere cobertura suficiente y aplicabilidad resuelta.
- [ ] La tabla de decisión distingue falta de evaluación, evidencia insuficiente y alerta concreta.
- [ ] Transiciones no mezclan RiskDomainStatus con VetoDecision.
- [ ] La migración legacy conserva procedencia y no aplica sustituciones ciegas.
- [ ] Los documentos M-02, M-04, veto-policy y decision-gates están sincronizados.
- [ ] Revisión humana/aprobación registrada antes de declarar X-01/X-03 cerrados.

## 9. Estado y limitaciones

Esta resolución es una propuesta de normalización para X-01 y X-03. No modifica el registro normativo IIM, no aprueba políticas, no implementa máquina de estados ni valida comportamiento empírico. La publicación del documento no implica que todos los consumidores legacy ya estén sincronizados. X-01/X-03 permanecen abiertos hasta completar sincronización, revisión y aceptación.
