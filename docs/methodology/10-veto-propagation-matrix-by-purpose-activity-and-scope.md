# Matriz de propagación de vetos por finalidad, actividad y scope

**ID:** IIE-METH-VETO-PROP-006  
**Versión:** 0.1.0-draft  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo:** X-05  
**Fecha:** 2026-10-31

## 1. Propósito y límites

Definir una matriz para propagar cada `VetoDecision` únicamente a las finalidades analíticas y actividades operativas afectadas por su regla, evidencia y scope. El objetivo es impedir tanto el bloqueo excesivo de funciones no afectadas como la habilitación accidental de rutas que sí están comprometidas.

Este documento complementa M-04, M-03, la política de vetos y el catálogo ORE. No crea reglas de veto aprobadas, no interpreta por sí solo la legislación de ninguna jurisdicción, no sustituye a Risk/Legal/Compliance, no define autorizaciones de trading ni acciones de enforcement. No modifica el registro normativo IIM.

## 2. Invariantes obligatorios

1. Todo veto debe identificar `rule_id`, `policy_version`, objeto afectado, evidencia, `VetoDecision`, scope, finalidad, actividad, jurisdicción cuando aplique, inicio, vigencia/revisión, owner y condición de levantamiento.
2. El veto se propaga por coincidencia de scope y regla, nunca por similitud nominal del activo o por una etiqueta global no contextualizada.
3. Aplicar el **scope mínimo suficiente para contener el riesgo demostrado**. No ampliar a más activos, redes, venues, finalidades o actividades sin evidencia o regla explícita.
4. Si el alcance real no puede delimitarse y el riesgo es material, imponer bloqueo provisional al conjunto mínimo seguro que cubra la incertidumbre, con owner, plazo de resolución y revisión obligatoria. No convertirlo en bloqueo universal indefinido.
5. Un veto en una actividad no implica veto en otra: `DEPOSIT` no implica `WITHDRAWAL`; `TRADE` no implica `CUSTODY`; `MARKET_DATA` no implica `TRADE`.
6. Una restricción operativa no contamina automáticamente la elegibilidad analítica (AE). Una restricción analítica tampoco bloquea automáticamente una actividad operativa salvo que la regla tenga ese alcance.
7. `VETO_ACTIVE` bloquea el scope indicado; `VETO_REVIEW_REQUIRED` suspende conclusiones/acciones favorables en ese scope; `VETO_NOT_ASSESSED` impide conclusión favorable dependiente cuando la regla es obligatoria; `NO_VETO_IDENTIFIED` no constituye autorización.
8. Los vetos legales, sanciones y prohibiciones no dispensables se aplican conforme al objeto, conducta, persona, entidad, jurisdicción y alcance definidos por la fuente legal competente. La investigación y tratamiento de datos también deben cumplir restricciones legales.
9. No se levantan vetos por TTL, cambio de venue, diversificación, puntuación, liquidez, rendimiento esperado ni declaración del emisor.
10. Cada consumidor recibe la decisión original, su scope y la determinación de aplicabilidad; no copia una etiqueta global sin contexto.

## 3. Dimensiones canónicas del scope

El scope estructurado debe contemplar, según aplique:

- `subject`: canonical asset, representation, token contract, protocol, network, bridge, issuer, venue, counterparty, custodian, account, person/entity.
- `purpose`: DISCOVERY, RESEARCH, CLASSIFICATION, FUNDAMENTALS, TOKENOMICS, TECHNOLOGY, MARKET_ANALYSIS, RISK_ASSESSMENT, VALUATION, COMPARISON, BACKTEST.
- `activity`: MARKET_DATA, TRADE, DEPOSIT, WITHDRAWAL, TRANSFER, CUSTODY, REDEMPTION u otra acción explícita.
- `route`: venue, counterparty, instrument, account/subaccount, network, chain, bridge, wallet, endpoint.
- `jurisdiction`: país/región, persona/entidad y actividad a las que aplica la restricción.
- `constraints`: side, notional/size, currency, destination, time window, environment (RESEARCH/SIMULATION/PAPER/PRODUCTION).
- `validity`: effective_at, review_at/valid_until, evidence snapshot, policy version y event invalidators.

Los campos no aplicables deben declararse como tales con justificación; no se permiten comodines implícitos. `ALL` solo es válido si la regla aprobada y la evidencia soportan expresamente ese alcance.

## 4. Clases de veto y propagación base

Las clases siguientes son tipos de alcance, no nuevas conclusiones de riesgo. Una regla concreta puede pertenecer a más de una clase; cada impacto debe estar explícitamente declarado.

| Clase | Ejemplo de objeto/condición | Finalidades analíticas potencialmente afectadas | Actividades operativas potencialmente afectadas | Regla de contención |
|---|---|---|---|---|
| V-01 Asset / rights / fraud | Fraude demostrado, derechos inexistentes, activo falso o emisión no autorizada | CLASSIFICATION, FUNDAMENTALS, RISK_ASSESSMENT, VALUATION, COMPARISON; RESEARCH/DISCOVERY pueden continuar con etiqueta de alerta si es lícito y seguro | TRADE, DEPOSIT, WITHDRAWAL, TRANSFER, REDEMPTION según representación y regla | No extender a activos legítimos con nombre parecido; separar research de recomendación favorable |
| V-02 Protocol / contract | Exploit, admin compromise, código malicioso o fallo crítico de contrato | TECHNOLOGY, RISK_ASSESSMENT y gates dependientes; VALUATION/COMPARISON si el mecanismo afecta derechos o solvencia | Solo actividades que interactúan con contrato/protocolo afectado: TRADE/DEPOSIT/WITHDRAWAL/TRANSFER/CUSTODY/REDEMPTION según exposición | Identificar address, chain, versión, función y activos expuestos; no extender a contratos no afectados |
| V-03 Network / consensus | Compromiso de consenso, halt, finality failure o chain instability | TECHNOLOGY, MARKET_ANALYSIS, RISK_ASSESSMENT; VALUATION si depende de liquidación/seguridad | TRANSFER, DEPOSIT, WITHDRAWAL, TRADE/settlement y CUSTODY en la red afectada | Limitar a chain, ventana, confirmaciones y rutas dependientes; no extender a otra red sin dependencia demostrada |
| V-04 Bridge / wrapped representation | Bridge comprometido, reserva no verificable o redención suspendida | RISK_ASSESSMENT, TECHNOLOGY, CLASSIFICATION y valoración/comparación de la representación afectada | TRANSFER, DEPOSIT, WITHDRAWAL, CUSTODY, REDEMPTION y TRADE del wrapper cuando dependa de paridad/redención | Diferenciar token nativo y representación bridged; nunca generalizar a nativo sin dependencia causal |
| V-05 Venue / market integrity | Venue insolvente, suspendido, manipulación o mercado no íntegro | MARKET_ANALYSIS y VALUATION/COMPARISON que dependan de ese mercado; otras fuentes pueden sostener research | MARKET_DATA, TRADE, DEPOSIT, WITHDRAWAL según servicio/mercado concreto | Venue e instrumento específicos; un mercado afectado no invalida automáticamente todo el mercado global |
| V-06 Counterparty / issuer / reserve | Insolvencia, fraude de emisor, reservas insuficientes o contraparte fallida | FUNDAMENTALS, RISK_ASSESSMENT, VALUATION y COMPARISON de instrumentos expuestos | TRADE, DEPOSIT, WITHDRAWAL, CUSTODY, REDEMPTION según obligación/exposición | Identificar entidades, productos, obligaciones y exposición; no extender a entidades afiliadas sin base |
| V-07 Custody / account / key control | Claves comprometidas, segregación fallida, congelación de cuenta o pérdida de control | RISK_ASSESSMENT y valoración si afecta recuperación/derechos; research no afectado en general | CUSTODY, WITHDRAWAL, TRANSFER, DEPOSIT y TRADE en cuentas/rutas afectadas | Limitar a custodio, wallet, account, signer, chain y activos bajo control afectado |
| V-08 Legal / sanctions / jurisdiction | Prohibición, sanción, orden judicial o restricción de licencia | Finalidades que impliquen conducta prohibida, distribución o recomendación restringida; research solo si permitido y con controles | Actividades prohibidas para la persona/entidad, activo, contraparte y jurisdicción correspondientes | Aplicar exactamente fuente, sujeto, conducta y jurisdicción; no universalizar fuera de alcance legal |
| V-09 Liquidity / settlement / market access | Profundidad insuficiente, retiro no disponible, settlement/finality degradada | MARKET_ANALYSIS, VALUATION y COMPARISON para tamaño/horizonte afectados | TRADE, WITHDRAWAL, DEPOSIT o TRANSFER según servicio y tamaño | Limitar por lado, tamaño, ventana, venue, moneda y ruta; no tratar como veto absoluto fuera de esos límites |
| V-10 Data / oracle / evidence integrity | Oráculo comprometido, feed contaminado, identidad o datos falsificados | Solo las finalidades que consumen la fuente/atributo afectado; exigir fuente alternativa independiente | MARKET_DATA o acciones dependientes de precio/oráculo/feed afectado | Invalidar fuente y decisiones dependientes; no bloquear fuentes independientes ni todo el activo sin regla causal |

## 5. Matriz de propagación por finalidad y actividad

La matriz siguiente establece el comportamiento por defecto cuando el scope coincide. `B` = bloquear conclusión/acción dentro del scope; `R` = suspender y enviar a revisión; `C` = continuar con caveat/limitación, sin conclusión favorable sobre el punto vetado; `—` = no propagar automáticamente. La regla específica y la ley aplicable prevalecen sobre esta matriz.

| Tipo de scope del veto | DISCOVERY / RESEARCH | CLASSIFICATION / FUNDAMENTALS / TECHNOLOGY / RISK | MARKET_ANALYSIS | VALUATION / COMPARISON / BACKTEST | MARKET_DATA | TRADE | DEPOSIT | WITHDRAWAL | TRANSFER | CUSTODY | REDEMPTION |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Asset-wide critical fraud / invalid rights | C/R | B/R | B/R | B | R/C | B | B | B | B | B | B |
| Contract/protocol-specific exploit | C | B en scope | R/B según dependencia | B si afecta tesis/rights | C/R | B si interactúa | B si interactúa | B si interactúa | B si interactúa | B si depende | B si depende |
| Network-specific halt/consensus | C | R en scope | R/B para datos afectados | R/B si settlement/seguridad depende | R para esa red | B en esa red si settlement comprometido | B en esa red | B en esa red | B en esa red | B si custodia depende | B si depende |
| Bridge / wrapped representation | C | B/R para representación | R para mercados de wrapper | B/R para wrapper | C/R | B para wrapper/ruta afectada | B si depende | B si depende | B si depende | B si depende | B si redención depende |
| Venue-specific failure/suspension | C | — salvo dependencia analítica | B/R para ese venue | R/B si valoración depende solo de él | B/R en venue | B en venue | B si servicio suspendido | B si servicio suspendido | B si ruta usa venue | B si custodia en venue | B si redención vía venue |
| Custodian/account/key compromise | C | R solo si afecta tesis/recuperación | — salvo datos afectados | R/B según exposición | — salvo feed afectado | B en account/route | B en account/route | B en account/route | B en account/route | B | B si custodia/redención depende |
| Withdrawal-only suspension | — | — | C si relevante a liquidez | R/B si tesis exige salida en horizonte/tamaño | — | — salvo trade inseparable del settlement | — | B | — salvo ruta compartida | — salvo control común | — |
| Deposit-only suspension | — | — | C si afecta acceso/liquidez | R/B si estrategia depende de depósito | — | — salvo dependencia de financiación | B | — | — salvo ruta compartida | — | — |
| Legal/sanctions prohibition | R/C según ley | B para finalidad/conducta prohibida | B para conducta prohibida | B para conducta prohibida | B si la provisión de datos está prohibida | B | B | B | B | B | B |
| Data/oracle/feed integrity | C | R/B solo en análisis dependiente | B/R para feed afectado | B/R si depende de feed | B para feed | B si control usa feed | — | — | — | — | — |

**Advertencia:** la tabla no es un permiso. `—` significa únicamente “no propagar automáticamente este veto”; el consumidor aún debe ejecutar sus propios gates y controles. `C` no significa que el sistema pueda recomendar o transaccionar en contra del veto. Si la ley, la política aprobada o una regla específica prescribe bloqueo, se aplica el bloqueo.

## 6. Reglas de composición por scope

1. Resolver primero la identidad exacta del objeto afectado y sus dependencias técnicas/económicas.
2. Evaluar coincidencia de cada dimensión del veto con la finalidad/actividad solicitada.
3. Aplicar la regla específica, su consecuencia y su alcance aprobado; no deducir consecuencias no declaradas.
4. Si coincide directamente el scope y `VETO_ACTIVE`, bloquear el scope.
5. Si coincide y `VETO_REVIEW_REQUIRED`, suspender la conclusión/acción favorable y asignar revisión.
6. Si coincide y `VETO_NOT_ASSESSED`, abstenerse de una conclusión favorable dependiente cuando la regla sea obligatoria para ese scope.
7. Si no coincide, no propagar automáticamente; registrar `NOT_AFFECTED_BY_THIS_VETO` como resultado de aplicabilidad de la regla para ese consumidor, no como `NO_VETO_IDENTIFIED` global.
8. Si la coincidencia es parcial (por ejemplo, tamaño o jurisdicción), aplicar restricción solo al subconjunto afectado y registrar límites cuantificados.
9. Si hay varios vetos, conservar todos. El resultado de cada consumidor se compone sobre las causas aplicables; resolver una causa no libera las restantes.
10. Si existe conflicto de scope, identidad o versiones, emitir REVIEW_REQUIRED/INPUTS_INCONSISTENT y bloquear la acción que dependa de resolverlo.

## 7. Diferencia entre veto, elegibilidad ORE y enforcement

- `VetoDecision` declara el resultado de una regla de veto para un scope.
- `OperationalRouteEligibilityDecision` compone vetos aplicables junto con venue, counterparty, instrument, security, custody, liquidity/cost, legal y operational-health assessments.
- `TradeAuthorization` concede permiso independiente, acotado y emitido por autoridad autorizada.
- `TradeControlDecision` aplica controles pre/post-trade y enforcement.

Por tanto:

- Un veto activo de retiro puede hacer `ROUTE_INELIGIBLE` para `WITHDRAWAL`, sin cambiar `ORE` de `MARKET_DATA` ni la AE de `RESEARCH`.
- Un veto de contrato puede afectar transferencias y operaciones que invoquen el contrato, pero no necesariamente investigación histórica ni otros contratos.
- `ROUTE_ELIGIBLE` nunca levanta un veto ni crea `TradeAuthorization`.
- Un veto legal no dispensable no puede ser exceptuado por una autoridad interna.
- `STOP_NEW_ORDERS`, `CANCEL_OPEN_ORDERS`, `REDUCE_ONLY` y `CLOSE_ONLY` son acciones de control distintas. El veto puede requerir una o varias, pero la regla debe declarar la acción y autoridad; no inferirlas del estado VETO_ACTIVE.

## 8. Reglas de incertidumbre y contención provisional

- La falta de evidencia no crea automáticamente `VETO_ACTIVE`; usar RiskDomainStatus y `VETO_NOT_ASSESSED` o `VETO_REVIEW_REQUIRED` según exista señal concreta.
- No usar `NO_VETO_IDENTIFIED` para un scope cuya cobertura obligatoria no esté completa.
- Cuando no se sabe si una ruta comparte infraestructura comprometida, bloquear provisionalmente solo las rutas plausiblemente dependientes hasta resolver el grafo de dependencia.
- Todo bloqueo provisional debe incluir hipótesis, evidencia, universo provisional, owner, SLA/review_at y criterios de liberación.
- La incertidumbre no debe ser silenciosamente convertida en una restricción universal ni en permiso.

## 9. Casos de aceptación

| Caso | Veto | Resultado esperado |
|---|---|---|
| Exchange suspende solo retiros | VETO_ACTIVE / WITHDRAWAL / venue+asset scope | WITHDRAWAL bloqueado; no bloquear automáticamente RESEARCH, MARKET_DATA, TRADE, DEPOSIT |
| Exchange suspende depósitos | VETO_ACTIVE / DEPOSIT | DEPOSIT bloqueado; no inferir suspensión de retiros |
| Custodio pierde control de wallet de una red | VETO_ACTIVE / CUSTODY+network+account | Bloquear custody y rutas dependientes; no contaminar otras redes/cuentas |
| Exploit confirmado en contrato de wrapper | VETO_ACTIVE / contract address + chain | Bloquear interacción con wrapper y rutas dependientes; activo nativo solo si existe dependencia demostrada |
| Bridge comprometido, token nativo independiente | VETO_ACTIVE / bridge+wrapped representation | Bloquear representación bridged; permitir evaluación independiente del token nativo |
| Exchange insolvente, mercado alternativo independiente | VETO_ACTIVE / venue+counterparty | Bloquear venue afectado; investigar otros mercados y su contraparte por separado |
| Sanción aplicable a entidad/actividad concreta | VETO_ACTIVE / legal scope | Bloquear conducta prohibida para sujeto/jurisdicción; no extrapolar fuera del alcance legal |
| Oráculo comprometido usado por modelo de valoración | VETO_ACTIVE o REVIEW_REQUIRED / feed | Bloquear valoración dependiente del feed; usar fuente alternativa solo tras validar independencia |
| Evidencia insuficiente sobre dependencia de ruta | VETO_NOT_ASSESSED o REVIEW_REQUIRED | No declarar ruta favorable; bloqueo provisional mínimo con revisión temporal |
| Veto de retiro resuelto, pero custodia sigue comprometida | Veto withdrawal levantado; custody VETO_ACTIVE | La ruta sigue bloqueada por el segundo veto; no liberar por resolver el primero |
| Veto activo y score/ranking elevado | VETO_ACTIVE | El veto prevalece en scope; score no compensa |
| Veto operativo sin impacto analítico | Scope limitado a una ruta | AE puede continuar; registrar limitación operativa y no afirmar invertibilidad |

## 10. Requisitos mínimos del registro de propagación

Cada evaluación de propagación debe registrar:

- `propagation_decision_id`, `veto_decision_id`, `rule_id`, `policy_version`;
- consumer (`AE`, `DECISION`, `ORE`, `PORTFOLIO_EXECUTION`) y su versión;
- requested purpose/activity y scope completo;
- matched dimensions, non-matched dimensions y dependency evidence;
- applicability result (`AFFECTED`, `NOT_AFFECTED_BY_THIS_VETO`, `PARTIALLY_AFFECTED`, `SCOPE_UNRESOLVED`);
- consequence (`BLOCK`, `REVIEW`, `LIMIT`, `NO_PROPAGATION`) y rationale;
- constraints (venue, account, network, side, size, jurisdiction, time window);
- evidence snapshot, timestamps, valid_until/review_at;
- owner, reviewer, decision event y supersedes reference.

`NOT_AFFECTED_BY_THIS_VETO` es solo una conclusión de no coincidencia con esta regla concreta. No equivale a que el objeto/actividad esté libre de otros vetos ni a que sea elegible.

## 11. Criterios de aceptación X-05

- [ ] Cada regla de veto declara objeto, purpose/activity, scope y consecuencia.
- [ ] La matriz distingue finalidades analíticas y actividades operativas.
- [ ] No existe propagación implícita entre DEPOSIT/WITHDRAWAL/TRANSFER/TRADE/CUSTODY.
- [ ] Los vetos de venue, custodio, contrato, bridge y red se limitan a dependencias demostradas.
- [ ] Se distingue no afectado por una regla de ausencia global de vetos.
- [ ] La incertidumbre produce abstención/revisión o contención provisional delimitada, nunca permiso implícito.
- [ ] Los vetos múltiples se conservan y componen sin liberación prematura.
- [ ] AE, ORE, TradeAuthorization y TradeControlDecision permanecen separados.
- [ ] Los casos de aceptación incluyen escenarios positivos, negativos, parciales y conflictivos.
- [ ] Revisión de Risk, Architecture, Operations, Security y Legal/Compliance según el tipo de regla.
- [ ] La matriz se sincroniza con M-04, veto-policy, M-03, ORE y Portfolio & Execution tras aprobación.

## 12. Estado y limitaciones

Propuesta documental de propagación. No constituye política de veto aprobada, matriz legal jurisdiccional, autorización de trading, algoritmo implementado ni validación empírica. Las marcas B/R/C/— son defaults para revisión, no decisiones automáticas de producción. X-05 permanece abierto hasta revisión especializada, aprobación de owners y posterior suite de conformidad. No crear motor de propagación ni schemas antes de resolver X-06 y los contratos de scope.
