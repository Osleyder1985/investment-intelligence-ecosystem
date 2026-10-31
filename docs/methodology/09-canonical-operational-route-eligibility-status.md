# Catálogo canónico de estados de elegibilidad operativa (ORE)

**ID:** IIE-METH-ORE-STATUS-005  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo:** X-04  
**Fecha:** 2026-10-31

## 1. Propósito y alcance

Resolver la divergencia de vocabulario entre M-02 (`ROUTE_UNKNOWN`) y M-03 (`ROUTE_INSUFFICIENT_INFORMATION`, `ROUTE_NOT_ASSESSED`, `ROUTE_ELIGIBLE_WITH_LIMITATIONS`, `ROUTE_ELIGIBLE`). Se propone un único `RouteEligibilityStatus` para la decisión compuesta ORE, separado de los estados de los assessments que la alimentan y de los controles transaccionales.

Este documento es una propuesta semántica. No actualiza silenciosamente M-02/M-03, no crea schemas ni código, no aprueba una política operativa y no autoriza trading real. No modifica el registro normativo IIM.

## 2. Principios invariantes

1. ORE se evalúa para un scope completo: activo y representación, actividad, instrumento, venue/contraparte, cuenta, red, custodio, jurisdicción, lado, tamaño, moneda, entorno, política y ventana temporal.
2. El estado no es una propiedad universal del activo ni del venue.
3. Cada actividad constituye una evaluación distinta: MARKET_DATA no implica TRADE; TRADE no implica DEPOSIT, WITHDRAWAL, TRANSFER, CUSTODY o REDEMPTION; DEPOSIT no implica WITHDRAWAL.
4. ORE compone assessments fuente; no reemplaza ni reescribe Venue/Counterparty, Instrument, TechnicalSecurity, Custody, Liquidity/Cost y OperationalHealth assessments.
5. Estado ORE favorable no es mandato, permiso, autorización, pre-trade approval, orden aceptada ni confirmación de ejecución.
6. Fallos críticos y vetos no dispensables no son compensables por score, liquidez, precio o limitaciones favorables en otro dominio.
7. UNKNOWN, NOT_ASSESSED, STALE, CONFLICTED e INVALID no se convierten en elegibilidad favorable.
8. La decisión debe conservar causas primarias y secundarias, scope, fuentes, versiones, timestamps y linaje.
9. Un cambio en actividad, tamaño, lado, venue, cuenta, red, custodio, jurisdicción o entorno requiere nueva evaluación o invalidación explícita.
10. No inferir permisos entre rutas ni reutilizar aprobaciones fuera de su scope.

## 3. Catálogo canónico propuesto

`RouteEligibilityStatus` tendrá exactamente los siguientes estados en v0.1.0-draft:

| Estado | Definición | Consecuencia mínima |
|---|---|---|
| ROUTE_NOT_ASSESSED | No se inició o no se completó la evaluación requerida | No operar; no inferir resultado |
| ROUTE_ASSESSMENT_IN_PROGRESS | Evaluación en curso, aún sin conclusión | No operar hasta decisión final |
| ROUTE_ELIGIBLE | Todos los controles obligatorios aplicables se evaluaron satisfactoriamente para el scope | Puede pasar a autorización/controles independientes; no autoriza acción |
| ROUTE_ELIGIBLE_WITH_LIMITATIONS | Controles obligatorios satisfechos con restricciones explícitas, medibles y aplicables | Solo puede avanzar dentro de limitaciones y tras autorización independiente |
| ROUTE_INELIGIBLE | Un criterio obligatorio falla o existe prohibición/veto verificado para el scope | Bloquear la actividad/ruta afectada |
| ROUTE_REVIEW_REQUIRED | Alerta creíble, conflicto material o decisión crítica pendiente de revisión | Suspender conclusión favorable y bloquear la actividad afectada hasta resolución |
| ROUTE_INSUFFICIENT_INFORMATION | La evaluación se intentó, pero falta evidencia material para decidir | No operar; solicitar evidencia o abstenerse |
| ROUTE_STALE | La evidencia/assessment excedió vigencia o quedó invalidado temporalmente | No operar hasta reevaluar |
| ROUTE_CONFLICTED | Inputs materiales incompatibles impiden una composición coherente | No operar; resolver conflicto |
| ROUTE_INVALID | La decisión/assessment incumple integridad, contrato, procedencia o método requerido | Rechazar como input; no operar |

No se define `ROUTE_UNKNOWN` como estado canónico. Es ambiguo porque mezcla falta de evaluación, insuficiencia de evidencia y estados de incertidumbre distintos. Se conserva únicamente como valor legacy sujeto a migración explícita.

## 4. Distinción de los estados de los assessments fuente

Los assessments de componentes conservan sus propios estados y códigos; no deben reutilizar el enum ORE como si fueran equivalentes. Como mínimo, cada assessment fuente necesita:

- `assessment_status` y su vocabulario de dominio;
- `applicability_status` cuando exista decisión de aplicabilidad;
- `finding_severity`/`control_result` separados del estado del assessment;
- `scope`, `assessment_id`, `policy_version`, `evidence_manifest_id`;
- `evaluated_at`, `valid_until`, `source_version` y `supersedes_id`;
- owner, reviewer y aprobación cuando la política lo exija.

Subdominios mínimos a enlazar:

1. Venue & Counterparty Eligibility.
2. Instrument / Asset Representation Eligibility.
3. Technical Security (API, wallet, signer, chain, bridge, smart contract).
4. Custody & Key Control.
5. Liquidity, Market Access & Cost (lado, tamaño, ventana, depth, spread, fees, slippage, impact, gas).
6. Operational Health & Connectivity.
7. Jurisdiction, Legal Restrictions & Permissions.
8. Settlement, Finality, Reconciliation & Failure Handling.
9. Mandate / Portfolio Constraints como input de compatibilidad, sin confundirlos con autorización.

Un estado de componente no se copia literalmente al estado ORE. La política de composición transforma causas tipadas a un estado ORE y conserva la referencia fuente.

## 5. Reglas de composición y precedencia

La composición es fail-closed, determinista y por scope. Precedencia propuesta para la decisión final:

1. Prohibición legal/sanción o veto no dispensable, verificado y aplicable → `ROUTE_INELIGIBLE`.
2. Fallo crítico verificado de seguridad, custodia, contraparte, instrumento o control obligatorio → `ROUTE_INELIGIBLE`.
3. Alerta creíble, conflicto de evidencia o revisión crítica pendiente → `ROUTE_REVIEW_REQUIRED` o `ROUTE_CONFLICTED` según la naturaleza: revisión cuando existe una cuestión adjudicable; conflicted cuando inputs incompatibles impiden la composición.
4. Input inválido, corrupto, no autenticado o incompatible con el contrato/policy version → `ROUTE_INVALID`.
5. Evidencia obligatoria obsoleta → `ROUTE_STALE`.
6. Evaluación iniciada con evidencia material insuficiente → `ROUTE_INSUFFICIENT_INFORMATION`.
7. Evaluación no iniciada/incompleta sin resultado → `ROUTE_NOT_ASSESSED` o `ROUTE_ASSESSMENT_IN_PROGRESS` según lifecycle.
8. Todos los controles obligatorios satisfechos, con limitaciones explícitas y verificables → `ROUTE_ELIGIBLE_WITH_LIMITATIONS`.
9. Todos los controles obligatorios aplicables satisfechos, sin limitaciones pendientes → `ROUTE_ELIGIBLE`.

La lista ordena el estado principal, pero no elimina causas simultáneas. Para que el resultado sea reproducible, la implementación futura debe definir una función de composición con inputs ordenados, reglas de tie-break, clasificación de causas, policy version, snapshot y tabla de conformidad. Este documento define el vocabulario, no implementa aún ese algoritmo completo (relacionado con X-06).

### 5.1 Diferencias que no se deben colapsar

- `ROUTE_INELIGIBLE`: existe una razón afirmativa y probada para no usar esa ruta.
- `ROUTE_INSUFFICIENT_INFORMATION`: se intentó evaluar, pero la evidencia no permite concluir.
- `ROUTE_NOT_ASSESSED`: no existe evaluación completada ni intento suficiente.
- `ROUTE_ASSESSMENT_IN_PROGRESS`: el proceso está activo y no ha concluido.
- `ROUTE_REVIEW_REQUIRED`: hay una alerta o cuestión concreta que requiere decisión humana/autoridad designada.
- `ROUTE_CONFLICTED`: fuentes/assessments incompatibles impiden un resultado coherente.
- `ROUTE_STALE`: una evaluación antes utilizable ya no está vigente.
- `ROUTE_INVALID`: el input o resultado no es admisible por fallo de integridad/contrato.

## 6. Migración explícita desde M-02/M-03

| Estado legacy | Destino propuesto | Regla |
|---|---|---|
| ROUTE_ELIGIBLE | ROUTE_ELIGIBLE | Mantener solo si scope y evidencia cumplen los requisitos canónicos |
| ROUTE_ELIGIBLE_WITH_LIMITATIONS | ROUTE_ELIGIBLE_WITH_LIMITATIONS | Conservar todas las limitaciones, límites y expiraciones |
| ROUTE_INELIGIBLE | ROUTE_INELIGIBLE | Conservar criterio, evidencia y scope de bloqueo |
| ROUTE_REVIEW_REQUIRED | ROUTE_REVIEW_REQUIRED | Conservar alerta, owner, plazo y criterio de resolución |
| ROUTE_INSUFFICIENT_INFORMATION | ROUTE_INSUFFICIENT_INFORMATION | Mantener si evaluación intentada y falta evidencia material |
| ROUTE_NOT_ASSESSED | ROUTE_NOT_ASSESSED | Mantener si no existe evaluación concluida/iniciada sustancialmente |
| ROUTE_STALE | ROUTE_STALE | Conservar timestamps y causa de expiración/invalidación |
| ROUTE_UNKNOWN | ROUTE_NOT_ASSESSED | Solo si no se inició evaluación; si se intentó, clasificar por evidencia disponible |
| ROUTE_UNKNOWN | ROUTE_INSUFFICIENT_INFORMATION | Si la evaluación se intentó y falta evidencia material |
| ROUTE_UNKNOWN | ROUTE_REVIEW_REQUIRED | Solo si existe alerta concreta que exige revisión; no por default |
| ROUTE_UNKNOWN | ROUTE_CONFLICTED / ROUTE_INVALID | Solo si la causa documentada es conflicto o invalidez, respectivamente |

No hacer sustitución global de `ROUTE_UNKNOWN`. La migración debe conservar estado original, fuente, scope, fecha, motivo, regla de transformación y reviewer. Registros ambiguos permanecen en cuarentena de migración y no pueden producir resultado favorable.

## 7. Scope y claves de decisión

Cada `OperationalRouteEligibilityDecision` debe fijar, como mínimo:

- asset, representation y chain/network;
- activity y acción concreta;
- instrument/product y side cuando aplique;
- venue/broker/exchange y counterparty;
- account/subaccount y authority;
- custodian y modelo de control de claves;
- jurisdiction y restricciones;
- size/notional/currency y límites de liquidez;
- route endpoints/bridge/dirección cuando aplique;
- environment (RESEARCH, SIMULATION, PAPER, PRODUCTION);
- policy version, evidence snapshot y período de validez.

La clave de decisión debe impedir reutilización accidental entre scopes distintos. Un cambio en cualquier dimensión material genera nueva decisión o invalidación explícita de la anterior.

## 8. Interfaz con AE, DECISION y Portfolio & Execution

- AE y ORE son independientes. `ROUTE_INELIGIBLE` no vuelve al activo analíticamente inelegible.
- `ROUTE_ELIGIBLE` no crea tesis, valoración, oportunidad ni ranking favorable.
- `ROUTE_ELIGIBLE` y `ROUTE_ELIGIBLE_WITH_LIMITATIONS` no equivalen a `TradeAuthorization`.
- Portfolio & Execution debe volver a verificar mandato, límites, exposición, presupuesto de riesgo, autorización, pre-trade controls, permisos y estado de cuenta antes de cualquier orden.
- Una decisión ORE no puede aprobar depósitos/retiros/transferencias por inferencia desde TRADE.
- Un veto de riesgo/legal/security se propaga solo según su regla y scope; no se generaliza a todas las actividades ni se reduce a un simple estado global.
- `STOP_NEW_ORDERS`, `CANCEL_OPEN_ORDERS`, `REDUCE_ONLY` y `CLOSE_ONLY` son controles/enforcement, no estados ORE. Su definición corresponde a Portfolio & Execution y X-05/X-06.

## 9. Freshness, invalidación y lifecycle

ORE debe tener `evaluated_at`, `valid_until`, `policy_version`, evidencia y eventos invalidantes. La vigencia se calcula por dominio/actividad/venue y riesgo de cambio; no se impone un TTL universal.

Eventos invalidantes incluyen suspensión de venue/contraparte, incidentes de seguridad, pérdida/cambio de claves, cambio de contrato/red/bridge, cambio de custodio/cuenta/jurisdicción, deterioro de liquidez o capacidad de retiro, cambios de tamaño/lado/actividad/entorno, interrupciones de API/chain, finality/reconciliation failures y expiración de política/autorización.

Cuando una decisión pasa a STALE, CONFLICTED o INVALID, no puede seguir siendo consumida como elegible. La transición debe generar nueva versión/evento; nunca borrar historial ni revivir automáticamente un estado anterior.

## 10. Casos de conformidad propuestos

| Caso | Inputs | Estado esperado |
|---|---|---|
| Evaluación no iniciada | Sin assessments obligatorios | ROUTE_NOT_ASSESSED |
| Evaluación activa | Evaluaciones en curso, sin conclusión | ROUTE_ASSESSMENT_IN_PROGRESS |
| Falta evidencia de jurisdicción | Assessment intentado, fuente legal insuficiente | ROUTE_INSUFFICIENT_INFORMATION o REVIEW_REQUIRED si hay alerta concreta |
| Exchange suspendió retiros | Actividad WITHDRAWAL, suspensión confirmada | ROUTE_INELIGIBLE para WITHDRAWAL; no inferir TRADE/DEPOSIT |
| Venue y custodia conformes con límite de tamaño | Todos los controles obligatorios PASS, máximo notional explícito | ROUTE_ELIGIBLE_WITH_LIMITATIONS |
| Todos los controles conformes | Evidencia vigente y scope completo | ROUTE_ELIGIBLE; aún exige autorización independiente |
| Bridge comprometido | Ruta de transferencia depende del bridge afectado | ROUTE_INELIGIBLE o REVIEW_REQUIRED según confirmación/scope |
| Fuentes incompatibles | Chain/network o custodio contradictorios | ROUTE_CONFLICTED |
| Assessment expirado | Liquidez/custodia fuera de TTL | ROUTE_STALE |
| Schema o firma inválida | Evidencia/assessment no autenticado o contrato inválido | ROUTE_INVALID |
| ROUTE_UNKNOWN legacy sin detalle | No se conoce si evaluación fue iniciada | Cuarentena de migración; nunca mapear a ELIGIBLE |
| Depósito aprobado, retiro no evaluado | ORE DEPOSIT favorable; no existe ORE WITHDRAWAL | DEPOSIT no implica WITHDRAWAL; segundo ROUTE_NOT_ASSESSED |

## 11. Criterios de aceptación X-04

- [ ] Un solo enum RouteEligibilityStatus con definiciones mutuamente distinguibles.
- [ ] `ROUTE_UNKNOWN` retirado del vocabulario canónico y migración legacy explícita.
- [ ] Se distinguen NOT_ASSESSED, IN_PROGRESS, INSUFFICIENT_INFORMATION, REVIEW_REQUIRED, CONFLICTED, STALE e INVALID.
- [ ] Estado ORE separado de estados de assessments fuente.
- [ ] Scope de actividad/ruta completo y clave de decisión establecida.
- [ ] Precedencia documentada sin ocultar causas simultáneas.
- [ ] AE, ORE, TradeAuthorization y enforcement permanecen separados.
- [ ] No hay propagación implícita entre actividades ni scopes.
- [ ] Casos positivos, negativos, ambiguos y de frontera revisados.
- [ ] M-02, M-03, consumidores y contratos futuros sincronizados tras aprobación.
- [ ] Revisión de arquitectura, operaciones, seguridad, riesgo y legal/compliance según alcance.

## 12. Estado y limitaciones

Propuesta documental para resolver X-04. No está aprobada, implementada ni validada empíricamente. No crea permisos ni habilita rutas reales. X-04 permanece abierto hasta revisión, decisión de propietarios, sincronización de M-02/M-03 y suite de conformidad. No crear schemas ni motor ORE hasta cerrar la semántica y sus dependencias con X-05/X-06.
