# X-09 — Resolución A11/A12: valoración wrapped y agregación atómica

**ID:** IIE-METH-X09-AGG-013  
**Versión:** 0.2.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Fuentes:** X-05, X-06, M-02, catálogo de valoración, matriz de aplicabilidad y X-09 v0.3.0.

## 1. Dictamen

Este documento propone condiciones explícitas de valoración para representaciones wrapped/bridged (A11) y un procedimiento determinista de partición y agregación por unidades atómicas (A12).

A11 y A12 quedan **ADDRESSED_IN_DRAFT**, no aprobados. A11 conserva un GAP de armonización/migración con el catálogo legacy de valoración. Se mantienen independientes:
- `ValuationStatus`: estado del modelo/estimación de valor.
- `VetoDecision`: resultado de regla de veto y scope.
- `DecisionOutcome`: conclusión analítica para una finalidad y unidad.
- `RouteEligibilityStatus`: aptitud de una ruta/actividad concreta.
- `ValidationEnvelope`: validez/integridad de inputs y posibilidad de componer.

Una valoración estimable no elimina un veto. Una valoración no estimable no demuestra fraude ni ineligibilidad. El estado del wrapper no se extrapola al token nativo sin dependencia causal demostrada.

## 2. A11 — Objeto de valoración wrapped

### 2.1 Identidad de la unidad

La unidad de valoración es la representación concreta, no una etiqueta comercial ambigua:
- canonical_asset_id;
- representation_id;
- chain_id/network_id;
- contract_address y token standard cuando aplique;
- bridge_id y versión/contrato de bridge cuando aplique;
- issuer/custodian/counterparty y obligor cuando aplique;
- derechos de titular, mecanismo de respaldo, redención y settlement;
- valuation_purpose, currency, valuation_timestamp, horizon y methodology_version.

La identidad del token nativo y la representación wrapped deben conservar IDs separados. La relación derivativa se registra como dependencia, no como identidad equivalente.

### 2.2 Evidencia mínima y dependencias

Antes de elegir método, evaluar aplicabilidad y admisibilidad de evidencia para:
1. identidad del contrato y representación;
2. derechos jurídicos/económicos del holder;
3. mecanismo de emisión, bloqueo, mint/burn y redemption;
4. respaldo/reservas, activos segregados y reconciliación cuando sean parte de la tesis;
5. bridge, custodios, multisig/admin keys, validadores y dependencias externas;
6. solvencia/capacidad del obligor y contraparte;
7. paridad, exchange rate, fees, slippage, redemption queue, límites, delays y finality;
8. mercados y fuentes de precio independientes, con calidad, profundidad y timestamp;
9. restricciones de jurisdicción, freeze/blacklist y condiciones de transferencia;
10. evidencia de incidentes, exploit, pausa, depeg o cambios materiales.

La lista es contextual: no todo wrapper tiene reserva/redención centralizada. Cuando un mecanismo no aplique, registrar NOT_APPLICABLE con justificación; no asumir respaldo ni redención por el nombre wrapped.

### 2.3 Reglas de asignación de ValuationStatus

Usar el catálogo canónico de X-06:
- `NOT_RUN`: valoración no iniciada.
- `INSUFFICIENT_EVIDENCE`: no se puede identificar/verificar suficientemente el objeto, los derechos, la estructura de respaldo o los inputs necesarios para seleccionar y parametrizar un método aplicable.
- `NOT_ESTIMABLE`: objeto y mecanismo identificados, pero el modelo no puede producir una estimación defendible debido a una dependencia material rota, no verificable o no modelable; o no existe método aplicable con supuestos defendibles.
- `ESTIMABLE_WITH_LIMITATIONS`: existe modelo defendible para una parte/claim/escenario claramente delimitado, con fuentes suficientemente independientes, supuestos y límites cuantificados, sensibilidad y caveats explícitos; la limitación puede no ser compensable para SelectionDecision.
- `ESTIMABLE`: derechos, inputs y método aplicables están suficientemente verificados y no existen limitaciones materiales no resueltas que deban restringir la conclusión.

Regla para distinguir los dos estados negativos:
- evidencia insuficiente para establecer qué se está valorando o cómo funciona el derecho → INSUFFICIENT_EVIDENCE;
- objeto/derechos suficientemente identificados, pero modelo/estimación no defendible → NOT_ESTIMABLE.

No convertir automáticamente:
- precio de mercado observado en valor razonable;
- cotización de wrapper en evidencia de redención a la par;
- reservas publicadas en reservas verificadas;
- existencia de bridge en solvencia/seguridad del bridge;
- NAV del subyacente en NAV del wrapper;
- paridad histórica en paridad futura;
- ausencia de incidente conocido en evidencia de seguridad.

### 2.3.1 Compatibilidad con el catálogo de valoración existente — GAP

La propuesta específica de crosswalk, versionado y migración se desarrolla en [valuation-status-vocabulary-migration.md](../valuation/valuation-status-vocabulary-migration.md) v0.1.0-draft. Este documento complementa, pero no aprueba ni ejecuta, la migración.

El catálogo `docs/valuation/valuation-method-catalog.md` v0.1.0 todavía publica los estados legacy `VALUATION_SUPPORTED`, `VALUATION_CONDITIONAL`, `INSUFFICIENT_EVIDENCE`, `NO_APPLICABLE_METHOD` y `NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE`. No coinciden uno-a-uno con el catálogo canónico de X-06.

No realizar migración automática. Como propuesta para revisión:
- `VALUATION_SUPPORTED` podría mapear a `ESTIMABLE` solo después de verificar método, evidencia, derechos y ausencia de limitación material.
- `VALUATION_CONDITIONAL` podría mapear a `ESTIMABLE_WITH_LIMITATIONS` únicamente cuando condiciones, límites y enforcement estén identificados; si no, REVIEW/ABSTAIN según el caso.
- `INSUFFICIENT_EVIDENCE` mantiene el mismo significado.
- `NO_APPLICABLE_METHOD` podría mapear a `NOT_ESTIMABLE` solo si objeto y derechos están suficientemente identificados y se ha determinado que no existe método defendible.
- `NOT_VALUABLE_WITH_AVAILABLE_EVIDENCE` es ambiguo entre insuficiencia de evidencia y falta de método; requiere reclasificación caso por caso, no mapping automático.

La matriz `valuation-applicability-matrix.md` usa `CONDITIONAL` como estado de aplicabilidad de una familia de métodos, no como ValuationStatus. Son dimensiones distintas y no deben mezclarse.

**GAP A11:** el crosswalk semántico y el plan de migración están ahora documentados como propuesta en `docs/valuation/valuation-status-vocabulary-migration.md`. El catálogo legacy, la matriz y sus consumidores todavía no han sido armonizados ni migrados. Se requiere aprobación de owners, inventario de consumidores, compatibilidad probada y actualización versionada antes de considerar A11 cerrado.

### 2.4 Condiciones para ESTIMABLE_WITH_LIMITATIONS

Se requiere todo lo siguiente:
1. identidad de la representación y derechos materiales verificados;
2. método aplicable al claim concreto;
3. fuentes alternativas independientes de la dependencia comprometida;
4. independencia demostrada, no solo declarada, respecto del bridge/venue/oracle/issuer afectado;
5. alcance estimado claramente identificado (por ejemplo, claim recuperable, escenario de liquidación o valor de mercado secundario);
6. haircuts, costes, delays, probabilidad/escenarios y sensibilidad documentados cuando sean estimables;
7. limitaciones y riesgos residuales visibles en el output;
8. aprobación del modelo/uso y revisión de conflictos materiales;
9. ausencia de contradicción con regla legal o de valoración obligatoria.

Si un veto bloquea la valoración del wrapper, el estado de valoración puede seguir describiendo lo que el modelo puede/no puede estimar, pero SelectionDecision no puede ignorar el VetoDecision. Si el bloqueo legal prohíbe incluso producir o distribuir una estimación, debe aplicarse la policy legal correspondiente.

### 2.5 Separación wrapper / nativo

- Un VETO_ACTIVE sobre bridge/wrapped representation se propaga al wrapper y dependencias demostradas según X-05.
- El activo nativo puede mantener su evaluación/valoración si su identidad, mercado, derechos y dependencias son independientes.
- Si el activo nativo depende de la misma red, custodio, reserva, emisor u obligación comprometida, la dependencia se evalúa y propaga por regla explícita.
- No se promedian precios native/wrapped ni se trasladan automáticamente descuentos, paridad o valor intrínseco.
- Reportar resultados separados, con dependency graph y source lineage.

### 2.6 Expected outcomes A11

| Caso | Evidencia/condición | ValuationStatus | Otros objetos/efecto |
|---|---|---|---|
| W-01 | No se identifica contrato/representación o derechos de holder | INSUFFICIENT_EVIDENCE | Identity/evidence assessment no favorable; no inventar modelo |
| W-02 | Derechos identificados, pero bridge comprometido y redención/paridad son inputs materiales no verificables | NOT_ESTIMABLE | VetoDecision permanece separado; bloquear conclusión favorable dependiente |
| W-03 | Solo existe precio secundario del wrapper, sin mecanismo defendible para inferir claim/value | NOT_ESTIMABLE | Precio observado se conserva como market data, no fair value |
| W-04 | Bridge comprometido, pero existe modelo independiente defendible para un claim recuperable delimitado, con haircut y escenarios | ESTIMABLE_WITH_LIMITATIONS | No libera veto; selección puede quedar INELIGIBLE/REVIEW/ABSTAIN según scope |
| W-05 | Respaldo/redención y derechos verificados, fuentes independientes, método aplicable y supuestos estables | ESTIMABLE | Decisión aún consume riesgos/vetos y demás gates |
| W-06 | No se ha ejecutado valoración | NOT_RUN | No inferir insuficiencia ni inelegibilidad |
| W-07 | Wrapper comprometido; token nativo es independiente y evidencia suficiente | Wrapper: según W-02/W-03; nativo: evaluación separada | No propagar al nativo sin dependencia causal |

### 2.7 Prohibiciones A11

- No asignar ESTIMABLE solo porque exista precio líquido.
- No asignar NOT_ESTIMABLE cuando ni siquiera se conoce con certeza el objeto/derecho; usar INSUFFICIENT_EVIDENCE.
- No usar ESTIMABLE_WITH_LIMITATIONS para ocultar un veto activo ni una restricción que el consumidor no puede hacer cumplir.
- No convertir NOT_RUN en NOT_ESTIMABLE.
- No extrapolar el resultado de wrapper al token nativo o viceversa.
- No liberar un veto porque el modelo de valoración encuentre un precio recuperable.

## 3. A12 — Partición atómica

### 3.1 Definición

Una unidad atómica es la combinación mínima de dimensiones materiales sobre la que:
- identidad y representación son homogéneas;
- finalidad/actividad y decision_type son iguales;
- los inputs y sus versiones/snapshots son compatibles;
- la aplicabilidad de reglas y dependencias es homogénea;
- los vetos, restricciones y controles producen la misma consecuencia;
- la salida puede aplicarse y hacerse cumplir sin ambigüedad.

No crear particiones por dimensiones irrelevantes. No combinar unidades solo porque comparten símbolo, ticker, issuer o nombre.

### 3.2 Dimensiones candidatas de partición

Aplicar solo cuando sean materiales:
- canonical_asset_id, representation_id, contract_address;
- network/chain, bridge, protocol version;
- purpose y activity;
- decision_type;
- jurisdiction;
- venue, market, instrument y counterparty;
- custody provider, account, wallet/key domain;
- side (buy/sell), order type, size/notional band, quote currency;
- liquidity window, settlement/finality, time window;
- investment horizon y valuation timestamp;
- policy_version, methodology_version, evidence snapshot/version.

### 3.3 Algoritmo determinista propuesto

1. **Freeze request:** normalizar requested_scope, decision_type, purpose/activity, versiones y timestamp de evaluación.
2. **Collect boundaries:** extraer todos los límites de scopes de inputs, vetos, restricciones, dependencias, venues, cuentas, redes, tamaños, jurisdicciones y ventanas temporales.
3. **Build atoms:** calcular intersecciones y diferencias necesarias para generar unidades disjuntas y exhaustivas dentro del requested_scope.
4. **Check partition:** demostrar que cada punto del requested_scope pertenece a exactamente una unidad; ninguna unidad se solapa; ninguna parte solicitada queda sin clasificar.
5. **Resolve applicability:** para cada unidad y regla, emitir AFFECTED, NOT_AFFECTED_BY_THIS_VETO, PARTIALLY_AFFECTED o SCOPE_UNRESOLVED con evidencia y rule_id.
6. **Compose causes:** aplicar la precedencia de X-06 §9 de forma independiente por unidad; conservar causas coincidentes y no coincidentes.
7. **Evaluate enforceability:** una limitación parcial solo es válida si las unidades permitidas/excluidas son identificables y existe enforcement probado/controles formalmente asignados.
8. **Aggregate:** agregar solo unidades compatibles según X-06 §10; conservar el vector completo de resultados.
9. **Validate:** comprobar integridad, exhaustividad, disjunción, compatibilidad de versiones y ausencia de pérdida de causas.
10. **Emit:** registrar partition_id/version, atom_id, reglas, causas, resultados, digest, versiones, timestamp y supersession.

El orden de llegada de inputs, el ranking de activos y el score no pueden cambiar la partición ni el outcome.

### 3.4 Reglas por unidad

Por cada unidad, evaluar independientemente:
- ValidationEnvelope (integrity/schema/version/scope);
- identity/evidence/applicability;
- RiskDomainStatus;
- VetoDecision y su aplicabilidad;
- AE o ORE según decision_type;
- valuation si purpose lo exige;
- límites y capacidad de enforcement;
- lifecycle/freshness.

Un veto en una unidad no se traslada a unidades no afectadas. Si una dependencia material cruza unidades, modelar una relación de dependencia explícita; no duplicar ni ignorar la causa.

### 3.5 Precedencia y agregación

Aplicar la precedencia canónica de X-06 §9 en cada unidad. No redefinirla aquí:
- Analytical: invalid input → review conflict → ineligible por incumplimiento demostrado/veto → review → abstain → in progress/not assessed → eligible with limitations → eligible.
- Operational route: invalid input → conflict → route ineligible por veto/control fallido → route review → invalid/stale → insufficient information/not assessed → in progress → eligible with limitations → eligible.

Para el scope agregado:
1. Todas las unidades analíticas DECISION_ELIGIBLE → DECISION_ELIGIBLE.
2. Todas elegibles o limitadas, con al menos una limitada → DECISION_ELIGIBLE_WITH_LIMITATIONS.
3. Cualquier unidad DECISION_REVIEW_REQUIRED impide resultado plenamente favorable → DECISION_REVIEW_REQUIRED.
4. Cualquier unidad DECISION_ABSTAIN impide conclusión favorable para el scope completo → DECISION_ABSTAIN.
5. Una o más unidades DECISION_INELIGIBLE impiden resultado favorable; se reporta DECISION_INELIGIBLE con desglose, salvo que exista review/abstain de mayor precedencia conforme a X-06 §9 y §10. Conservar todos los outcomes por unidad.
6. Si una unidad es ineligible y otra review/abstain, el resumen aplica la precedencia exacta aprobada para el tipo de decisión; no ocultar ninguna condición en el detalle. La matriz debe conservar vector completo, aunque el summary elija un outcome.
7. ORE nunca se reduce a una autorización global: mantener una fila por ruta. Si el consumidor solicita varias rutas, presentar matriz por ruta y resumen restrictivo sin permiso implícito.
8. DECISION_ELIGIBLE_WITH_LIMITATIONS/ROUTE_ELIGIBLE_WITH_LIMITATIONS solo si el subset permitido está identificado y su enforcement es efectivo. Si no, REVIEW/ABSTAIN/INELIGIBLE según causa.

**Nota de precedencia:** para evitar ambigüedad de agregación cuando coexisten INELIGIBLE, REVIEW y ABSTAIN, la salida agregada deberá seguir el orden de severidad explícito que aprueben los owners; hasta entonces, conservar siempre el vector por unidad y marcar `ValidationEnvelope.REVIEW_REQUIRED` por ambigüedad de agregación, sin inventar un nuevo DecisionOutcome. Este envelope es una condición de composición, no sustituye los outcomes atómicos.

### 3.6 Escenarios de agregación obligatorios

| Caso | Unidades | Resultados por unidad | Resumen |
|---|---|---|---|
| A-01 | Wrapper chain-A; native chain-A independiente | Wrapper DECISION_INELIGIBLE por veto; native DECISION_ELIGIBLE | No global ELIGIBLE; vector por unidad; resumen no favorable |
| A-02 | Wrapper chain-A; wrapper chain-B | Chain-A DECISION_REVIEW_REQUIRED; chain-B DECISION_ELIGIBLE | REVIEW_REQUIRED para scope completo; preservar chain-B favorable solo como resultado local |
| A-03 | Mismo wrapper y ruta, dos vetos solapados | DECISION_INELIGIBLE; causas V-04 y V-09 preservadas | INELIGIBLE; levantar una causa no libera la otra |
| A-04 | Scope parcialmente limitado y enforcement efectivo | Unidad permitida ELIGIBLE; unidad excluida INELIGIBLE por regla específica | ELIGIBLE_WITH_LIMITATIONS solo si policy permite selección de subset y enforcement está garantizado; nunca ocultar excluida |
| A-05 | Scope parcial pero consumidor no puede aislar/enforzar subset | Parte favorable, parte incierta/no controlable | REVIEW_REQUIRED/ABSTAIN; nunca limitar de palabra |
| A-06 | Varias rutas de venues/cuentas diferentes | Una ROUTE_ELIGIBLE, otra ROUTE_INELIGIBLE | Matriz por ruta; no emitir ORE global ELIGIBLE |
| A-07 | Dependencia de settlement atraviesa unidad supuestamente no afectada | SCOPE_UNRESOLVED o dependency affected | Bloquear/revisar unidad dependiente; no asumir independencia |
| A-08 | Unidades disjuntas, homogéneas y todas favorables | Todas DECISION_ELIGIBLE | DECISION_ELIGIBLE para el requested_scope exactamente cubierto |

### 3.7 Invariantes A12

- Partición exhaustiva: la unión de átomos equivale exactamente al requested_scope.
- Disjunción: intersección entre átomos distintos vacía.
- Homogeneidad: inputs/causas aplicables y consecuencias uniformes dentro de cada átomo.
- Conservación: ninguna causa se pierde al particionar o agregar.
- No expansión: no evaluar ni afirmar permiso fuera del requested_scope.
- No contaminación: causa no aplicable no se propaga.
- No ocultación: resumen no favorable si alguna unidad no es favorable.
- Enforcement: limitación requiere control verificable; si falla, reabrir composición.
- Determinismo: mismos inputs/versiones/policy/scope producen la misma partición y outcomes.
- Reproducibilidad: snapshot y digest permiten reconstruir decisión.

### 3.8 Métricas de cobertura A12

Reportar por partition_algorithm_version:
- requested-scope coverage = unidades cubiertas / unidades esperadas;
- atom disjointness violations;
- uncovered-scope count;
- overlapping-atom count;
- cause preservation ratio;
- applicability resolution rate;
- aggregation rule coverage;
- enforcement control pass/fail;
- determinism/replay pass rate.

No declarar cobertura completa sin tests de límites, intersecciones, scopes anidados, scopes parcialmente coincidentes, dependencias transitivas, cambios temporales y versiones incompatibles.

## 4. Criterios de aceptación A11/A12

A11:
- owners de Valuation/Risk/Legal validan la distinción entre insufficient evidence y not estimable;
- catálogo y applicability matrix se alinean con ValuationStatus canónico de X-06;
- W-01..W-07 tienen input manifest y expected outcome revisados;
- prueba separada demuestra que valuation status no elimina VetoDecision.

A12:
- formalizar algoritmo de partición y agregación en X-06 o documento aprobado;
- aprobar precedence cuando coexisten resultados no favorables;
- definir scope algebra, dependencias y límites;
- aprobar enforcement contract para partial limitations;
- construir pruebas property-based y escenarios C-07/C-14 ampliados;
- demostrar exhaustividad, disjunción, conservación de causas y determinismo.

**Estado:** A11/A12 ADDRESSED_IN_DRAFT; NOT_APPROVED; NOT_IMPLEMENTED; NOT_VALIDATED. Los futuros escenarios son especificaciones, no pruebas ejecutadas.


## 5. Relación con la matriz X-09

Los casos W-01..W-07 se materializan como escenarios S-085..S-091; los casos A-01..A-08 se materializan como S-092..S-099 en X-09 v0.4.0-draft. C-07 y C-14 reciben expected outcomes ampliados por unidad. Las invariantes INV-30..INV-35 se añaden a la matriz maestra de conformidad.

La precedencia de agregación cuando coexisten outcomes atómicos INELIGIBLE, REVIEW y ABSTAIN requiere aprobación explícita de los owners. Mientras no exista, se conservan todos los resultados por unidad y se marca ValidationEnvelope.REVIEW_REQUIRED para la agregación ambigua; no se inventa un nuevo DecisionOutcome.
