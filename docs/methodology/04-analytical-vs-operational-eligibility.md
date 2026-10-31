# Separación de elegibilidad analítica y elegibilidad operativa

**ID:** IIE-METH-ELIG-002  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Fecha:** 2026-10-31  
**Hallazgo:** M-03 (HIGH), `docs/methodology/02-methodology-consistency-audit.md`

## 1. Propósito

Definir dos decisiones independientes:

- **Analytical Eligibility (AE):** determina si un activo/representación puede participar en una finalidad de investigación o análisis.
- **Operational Route Eligibility (ORE):** determina si una ruta operacional concreta puede utilizarse para una actividad, instrumento, tamaño y entorno determinados.

La elegibilidad analítica no habilita operaciones. La elegibilidad de una ruta no certifica la calidad de la tesis de inversión. Ninguna de las dos equivale a autorización de trading.

Esta especificación es documental y no modifica el registro IIM ni los contratos aprobados.

## 2. Principios

1. **Separación de dominios:** AE y ORE tienen objetos, criterios, evidencia, propietarios, estados y ciclos de revisión distintos.
2. **Scope explícito:** no existe una elegibilidad universal de activo o venue.
3. **No propagación indebida:** un bloqueo de ruta no contamina automáticamente investigación; una tesis analítica favorable no elimina restricciones de ruta.
4. **Fail closed en operaciones:** UNKNOWN, STALE, INVALID, CONFLICTED, NOT_ASSESSED o evidencia faltante en controles operativos obligatorios bloquean la ruta/acción afectada.
5. **No autorización implícita:** ORE=ELIGIBLE no es TradeAuthorization, ni confirma fondos, límites o controles pre-trade.
6. **Veto no compensable:** vetos legales, sanciones, fraude, seguridad/custodia u otros vetos definidos por política prevalecen en su scope.
7. **Historial inmutable:** cambios generan nuevas evaluaciones versionadas.
8. **Resultados independientes:** la salida conserva AE y ORE en campos separados; nunca se sintetizan en un booleano único.

## 3. Objetos y alcance

### 3.1 Scope de elegibilidad analítica

Cada decisión AE debe identificar:

- activo canónico y representación/red/contrato;
- finalidad: DISCOVERY, RESEARCH, CLASSIFICATION, FUNDAMENTALS, TOKENOMICS, TECHNOLOGY, MARKET_ANALYSIS, RISK_ASSESSMENT, VALUATION, COMPARISON o BACKTEST;
- universo y snapshot;
- jurisdicción cuando sea material para la finalidad;
- horizonte/fecha de corte;
- criterios y versión de política;
- evidencia y sus timestamps/provenance.

La decisión se realiza por finalidad. Por ejemplo, un activo puede ser elegible para DISCOVERY y RESEARCH, pero no para VALUATION o COMPARISON por falta de datos.

### 3.2 Scope de elegibilidad operativa

Cada decisión ORE debe identificar una ruta completa y contextual:

- activo y representación exacta;
- actividad: MARKET_DATA, TRADE, DEPOSIT, WITHDRAWAL, TRANSFER, CUSTODY, REDEMPTION u otra acción explícita;
- instrumento/producto y lado de operación cuando aplique;
- venue, broker, exchange o contraparte;
- cuenta/subcuenta y titularidad/autoridad;
- red, chain, bridge y dirección/ruta de transferencia cuando aplique;
- custodio, modelo de custodia y responsabilidades de firma;
- jurisdicción y restricciones aplicables;
- tamaño/notional, límites y moneda;
- entorno: RESEARCH, SIMULATION, PAPER o PRODUCTION;
- fecha/hora, vigencia, política y evidencia.

Una evaluación para MARKET_DATA no autoriza TRADE; una evaluación para TRADE no autoriza DEPOSIT/WITHDRAWAL; una ruta de depósito no implica ruta de retiro. Cada actividad requiere evaluación propia.

## 4. Dominios de elegibilidad

### 4.1 AnalyticalEligibilityDecision

Estados propuestos:
- `ELIGIBLE`
- `ELIGIBLE_WITH_LIMITATIONS`
- `INELIGIBLE`
- `INSUFFICIENT_INFORMATION`
- `QUARANTINED`
- `NOT_ASSESSED`
- `STALE`

Criterios típicos:
- identidad y representación resueltas;
- datos admisibles y suficientes para la finalidad;
- taxonomía y aplicabilidad de módulos;
- cobertura mínima de evidencia definida por finalidad;
- ausencia de conflicto material no resuelto;
- requisitos legales/de uso de datos que afecten la investigación concreta.

No exigir liquidez de ejecución para permitir análisis fundamental, salvo que la finalidad analítica concreta sea medir invertibilidad/liquidez. La falta de venue compatible no vuelve inelegible al activo para investigación.

### 4.2 OperationalRouteEligibilityDecision

Estados propuestos:
- `ROUTE_ELIGIBLE`
- `ROUTE_ELIGIBLE_WITH_LIMITATIONS`
- `ROUTE_INELIGIBLE`
- `ROUTE_REVIEW_REQUIRED`
- `ROUTE_INSUFFICIENT_INFORMATION`
- `ROUTE_NOT_ASSESSED`
- `ROUTE_STALE`

Criterios por actividad y scope:
- identidad exacta del activo, instrumento y contrato;
- elegibilidad de venue/contraparte para la actividad concreta;
- jurisdicción, restricciones y permisos aplicables;
- seguridad técnica de API, red, bridge, wallet y firma;
- custodia, segregación, control de claves y recuperación;
- liquidez ejecutable para lado/tamaño/ventana;
- fees, spread, slippage, impacto, gas y límites;
- conectividad, integridad de datos y estado operacional;
- reconciliación, finality y tratamiento de estados inciertos;
- compatibilidad con mandato y política de riesgo cuando se evalúe la cartera.

**Separación de subdecisiones:** venue/contraparte eligibility, route technical security, custody eligibility, instrument eligibility y execution readiness deben producir assessments identificables y enlazados. ORE es una composición de ellos; no reemplaza sus evaluaciones.

## 5. Matriz de independencia y consecuencias

| Analytical Eligibility | Operational Route Eligibility | Consecuencia permitida |
|---|---|---|
| ELIGIBLE | ROUTE_ELIGIBLE | Investigación y análisis permitidos; ruta solo puede avanzar a controles/autorización independientes |
| ELIGIBLE | ROUTE_INELIGIBLE | Mantener investigación; bloquear actividad/ruta específica |
| ELIGIBLE | ROUTE_REVIEW_REQUIRED | Mantener investigación; ruta suspendida hasta revisión |
| ELIGIBLE | ROUTE_INSUFFICIENT_INFORMATION | Mantener investigación; no operar por esa ruta |
| INSUFFICIENT_INFORMATION | ROUTE_ELIGIBLE | No emitir tesis analítica favorable; ruta no se habilita por sí sola para inversión |
| INELIGIBLE para VALUATION | ROUTE_ELIGIBLE | No inferir valor/oportunidad; posible uso operativo solo si otra finalidad y controles lo autorizan, nunca por esta evaluación |
| QUARANTINED por identidad | Cualquier estado favorable | Bloquear actividades dependientes de identidad; reevaluar tras resolver conflicto |
| Cualquier AE | ROUTE_STALE | Mantener AE vigente si su evidencia sigue fresca; bloquear uso de la ruta hasta reevaluar |
| ELIGIBLE_WITH_LIMITATIONS | ROUTE_ELIGIBLE | Propagar limitaciones analíticas a la tesis; no convertirlas en permiso operativo |
| ELIGIBLE | ROUTE_ELIGIBLE_WITH_LIMITATIONS | Operar, si luego se autoriza, solo dentro de restricciones explícitas |

La matriz no concede permiso de ejecución. La operación requiere además mandato vigente, autorización específica, presupuesto/límites, controles pre-trade, estado de cuenta/saldo, controles post-trade y reconciliación.

## 6. Flujo de decisión

### 6.1 Flujo analítico

1. Resolver identidad y representación.
2. Declarar finalidad, universo, horizonte y snapshot.
3. Evaluar evidencia y aplicabilidad.
4. Aplicar criterios de elegibilidad de esa finalidad.
5. Registrar restricciones, abstenciones y causas.
6. Continuar únicamente los módulos permitidos.
7. Emitir una decisión analítica versionada.

### 6.2 Flujo operativo

1. Recibir una intención/propuesta contextual, no una orden ejecutable.
2. Fijar activo, representación, actividad, venue, cuenta, red, custodio, jurisdicción, tamaño y entorno.
3. Evaluar por separado venue/contraparte, instrumento, seguridad técnica, custodia, liquidez/costes y controles de infraestructura.
4. Componer ORE con regla fail-closed.
5. Si ORE no es favorable, bloquear la ruta/actividad; no cambiar AE.
6. Si ORE es favorable, remitir a mandato, autorización y controles de Portfolio & Execution.
7. Antes de acción real, revalidar frescura, límites, autorización y controles; la decisión ORE previa no sustituye la comprobación transaccional.

## 7. Composición y precedencia de ORE

La composición debe conservar todos los assessments fuente y aplicar esta precedencia:

1. Prohibición legal/sanción o veto no dispensable aplicable → ROUTE_INELIGIBLE.
2. Veto de seguridad, custodia o contraparte verificado → ROUTE_INELIGIBLE.
3. Conflicto material, alerta creíble o revisión crítica pendiente → ROUTE_REVIEW_REQUIRED.
4. Evidencia crítica desconocida, inválida, obsoleta o no evaluada → ROUTE_INSUFFICIENT_INFORMATION o ROUTE_STALE.
5. Incumplimiento de criterios obligatorios de actividad/tamaño/venue → ROUTE_INELIGIBLE.
6. Restricciones no críticas, explícitas y controlables → ROUTE_ELIGIBLE_WITH_LIMITATIONS.
7. Todos los controles requeridos satisfechos → ROUTE_ELIGIBLE.

No aplicar promedios ni puntuaciones para compensar fallos de controles obligatorios. La salida debe incluir causas primarias y secundarias. La ausencia de evidencia no puede interpretarse como ausencia de riesgo.

## 8. Contrato de salida conceptual

### AnalyticalEligibilityDecision

Campos mínimos:
- `decision_id`, `asset_id`, `representation_id`;
- `purpose`, `scope`, `status`;
- `criteria_version`, `methodology_version`;
- `universe_snapshot_id`, `evidence_manifest_id`;
- `reason_codes`, `limitations`, `abstentions`;
- `evaluated_at`, `valid_until`, `supersedes_decision_id`;
- referencias a assessments de identidad, datos, aplicabilidad y riesgo.

### OperationalRouteEligibilityDecision

Campos mínimos:
- `decision_id`, `asset_id`, `representation_id`;
- `activity`, `instrument_id`, `venue_id`, `counterparty_id`;
- `account_scope`, `network_id`, `custodian_id`, `jurisdiction_scope`;
- `side`, `size`, `notional_currency`, `environment`;
- `status`, `reason_codes`, `limitations`, `blocking_assessments`;
- referencias/versiones de venue, security, custody, liquidity/cost, instrument y operational health assessments;
- `policy_version`, `evidence_manifest_id`, `evaluated_at`, `valid_until`;
- `supersedes_decision_id`, `reviewer` y aprobación cuando aplique.

Estos son contratos conceptuales. No son JSON Schemas ni contratos implementados.

## 9. Interfaz con DECISION y Portfolio & Execution

- DECISION consume AE para determinar si una finalidad analítica puede continuar.
- DECISION consume riesgos/vetos aplicables al scope; no recalcula ni rebaja los resultados fuente.
- La tesis/oportunidad no debe contener un campo ambiguo `tradable=true`.
- Portfolio & Execution consume la decisión analítica como input informativo y ORE como evaluación de ruta, pero vuelve a validar mandato, límites y autorización.
- ORE no puede crear `TradeAuthorization`; esta requiere autoridad independiente y alcance acotado.
- Un activo aprobado para una ruta no se generaliza a otro venue, custodio, cuenta, red, actividad, tamaño o jurisdicción.

## 10. Freshness, eventos invalidantes y reevaluación

La vigencia se determina por riesgo y velocidad de cambio de cada dominio, no por un TTL universal.

Eventos que pueden invalidar ORE:
- suspensión/restricción del venue o contraparte;
- incidente de seguridad, exploit, bridge compromise o pérdida de claves;
- cambio de contrato, token, red, mecanismo de redención o migración;
- cambio de custodio, cuenta, jurisdicción o términos;
- deterioro de liquidez, profundidad, spread o capacidad de retiro;
- interrupción de infraestructura, API, chain congestion/finality;
- cambio de tamaño, lado, actividad o entorno;
- vencimiento de autorización o política.

Eventos que pueden invalidar AE:
- conflicto de identidad o nueva representación;
- cambios materiales en tokenomics, gobernanza, derechos, supply o tecnología;
- datos fuente corregidos, retirados o fuera de TTL;
- cambios de taxonomía, finalidad, universo u horizonte;
- incidentes que alteren la tesis o evaluación de riesgo.

Invalidar una decisión no borra su historial. Se crea una nueva evaluación; la anterior se conserva como histórica/superseded/invalidated.

## 11. Seguridad, segregación y controles humanos

- Productores de AE no aprueban por sí solos ORE.
- Evaluadores de ruta no pueden alterar tesis, score o valoración.
- Aprobación de riesgo, autorización de trading y control operativo deben estar segregados según materialidad y capacidad del sistema.
- Las excepciones deben indicar autoridad, motivo, alcance, vigencia, controles compensatorios y evidencia; no pueden dispensar vetos no dispensables.
- En producción, ninguna ruta pasa a activa por una recomendación del motor; requiere aprobación y gates externos.

## 12. Casos de aceptación

| Caso | AE | ORE | Resultado |
|---|---|---|---|
| Activo investigable sin exchange admitido | ELIGIBLE para RESEARCH | ROUTE_INELIGIBLE | Investigación continúa; operación bloqueada |
| Venue listado pero custodia insegura | ELIGIBLE | ROUTE_INELIGIBLE/REVIEW_REQUIRED | No operar; listing no prueba seguridad |
| Datos analíticos insuficientes y ruta técnicamente disponible | INSUFFICIENT_INFORMATION para VALUATION | ROUTE_ELIGIBLE | No afirmar oportunidad; ORE no crea tesis |
| Venue suspende retiros | Sin cambio automático | Ruta WITHDRAWAL ineligible/review | Bloquear retiro; no necesariamente bloquear análisis |
| Ruta de depósito aprobada y retiro no evaluado | AE independiente | DEPOSIT eligible; WITHDRAWAL not assessed | No inferir permiso de retiro |
| Exchange operativo, jurisdicción no resuelta | ELIGIBLE para RESEARCH | ROUTE_REVIEW_REQUIRED | No operar hasta resolver jurisdicción |
| Cambio de tamaño de orden | AE no cambia necesariamente | Recalcular ORE para nuevo tamaño | No heredar profundidad/coste de otro tamaño |
| Cambio de venue | AE puede mantenerse | Nueva ORE | No transferir aprobación entre venues |
| Oportunidad respaldada y autorización ausente | OPPORTUNITY_SUPPORTED | ROUTE_ELIGIBLE | No enviar orden; falta TradeAuthorization |
| Evento de seguridad posterior a evaluación | AE se reevalúa si afecta tesis | ORE invalidada/review | Bloqueo fail-closed según alcance |

## 13. Criterios de aceptación para M-03

- [ ] AE y ORE tienen estados, scopes, IDs y registros separados.
- [ ] Cada decisión AE especifica finalidad.
- [ ] Cada ORE especifica actividad y ruta completa.
- [ ] La matriz de independencia está revisada y no crea permisos implícitos.
- [ ] Venue eligibility, seguridad, custodia y autorización permanecen separados.
- [ ] UNKNOWN/STALE/INVALID/CONFLICT/NOT_ASSESSED bloquean la ruta cuando afectan controles obligatorios.
- [ ] Un fallo de ruta no bloquea automáticamente investigación.
- [ ] Un resultado analítico favorable no habilita una ruta.
- [ ] ORE no sustituye mandato, TradeAuthorization ni controles pre/post-trade.
- [ ] Se definen TTL, eventos invalidantes, reevaluación y segregación de funciones.
- [ ] Contratos y pruebas se diseñan solo después de revisión/aprobación metodológica.

## 14. Relación con documentos existentes

- `docs/asset-selection/eligibility-and-screening.md`
- `docs/methodology/01-global-crypto-asset-selection-methodology.md`
- `docs/methodology/02-methodology-consistency-audit.md`
- `docs/methodology/03-canonical-decision-state-model.md`
- `docs/architecture/portfolio-execution-integrated-requirements-draft.md`
- `docs/architecture/portfolio-execution-integration-resolution-matrix.md`

## 15. Estado y limitaciones

**DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.** La especificación resuelve conceptualmente la separación M-03, pero requiere revisión de arquitectura, riesgo, operaciones y compliance según jurisdicción. No modifica IIM-001 a IIM-037, no crea autorización de trading y no demuestra implementación ni validación empírica.
