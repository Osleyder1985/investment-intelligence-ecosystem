# Auditoría transversal de integración — Portfolio & Execution IIM-025 a IIM-037

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado:** INTEGRATION_REVIEW_REQUIRED  
**Baseline:** propuestas y revisiones de lotes A–D existentes en main  
**Seguimiento:** Issue #39

> Revisión documental asistida. No es aprobación independiente, especificación normativa aprobada, auditoría de seguridad/legal, implementación ni validación empírica. Los IDs IIM-025 a IIM-037 siguen siendo provisionales y PENDING_REVIEW.

## 1. Conclusión ejecutiva

Los cuatro lotes cubren el dominio funcional esperado, pero no deben pasar directamente al registro. Se detectan solapamientos semánticos y contratos transversales aún no consolidados. La recomendación es mantener REVISE en los 13 requisitos y producir una versión integrada para revisión humana.

| Dimensión | Resultado |
|---|---|
| Cobertura funcional | 13 requisitos propuestos en cuatro lotes |
| Revisión individual | 13/13 con recomendaciones documentales |
| Consistencia transversal | Requiere acciones antes de aprobación |
| Contratos técnicos ejecutables | No demostrados |
| Implementación y pruebas | No demostradas por esta auditoría |
| Aprobación normativa | Pendiente |

## 2. Hallazgos transversales

### INT-01 — Mandato, política de riesgo y autorización operativa no están suficientemente separados
**Severidad:** CRITICAL  
**Requisitos:** IIM-025, IIM-028, IIM-034, IIM-035, IIM-037.

El mandato define objetivos, alcance y facultades; la política de riesgo define presupuestos, límites y excepciones; la autorización concede permiso acotado para una acción concreta; el enforcement verifica y aplica las condiciones. No son intercambiables.

**Acción:** fijar cuatro contratos separados y prohibir que una propuesta/score o mandato general se interprete como autorización de orden. IIM-025 no debe duplicar límites cuantitativos de IIM-028 ni controles técnicos de IIM-035/037.

### INT-02 — Exposición, medición de riesgo y enforcement pueden duplicarse
**Severidad:** HIGH  
**Requisitos:** IIM-016, IIM-027, IIM-028, IIM-030, IIM-034.

IIM-027 debe producir exposiciones semánticas reproducibles; IIM-016 produce perfil/riesgo analítico según su alcance; IIM-030 calcula impactos de escenarios; IIM-028 compara métricas contra política; IIM-034 aplica controles operativos.

**Acción:** definir un único contrato de métricas con unidad, denominador, moneda, timestamp, cobertura, lineage y estado de calidad. El motor de medición no define límites; la política no recalcula datos fuente; el enforcement no altera métricas.

### INT-03 — Construcción inicial y rebalanceo comparten un contrato de propuesta
**Severidad:** HIGH  
**Requisitos:** IIM-026, IIM-031, IIM-025–030.

IIM-026 genera propuesta de asignación inicial/objetivo; IIM-031 detecta triggers y propone transición desde posiciones actuales. Ambos podrían producir propuestas de pesos y repetir validaciones.

**Acción:** usar un contrato común PortfolioProposal versionado, con proposal_type (INITIAL_ALLOCATION o REBALANCE), cartera/mandato versionados, posiciones base, inputs, restricciones, costes, factibilidad y diff antes/después. Mantener triggers y política de rebalanceo propios de IIM-031. Ninguno autoriza órdenes.

### INT-04 — Coste estimado y coste realizado necesitan reconciliación explícita
**Severidad:** HIGH  
**Requisitos:** IIM-029, IIM-031, IIM-033, IIM-036.

IIM-029 estima capacidad/costes; IIM-031 incorpora estimaciones en propuestas; IIM-033 conserva órdenes y fills reales; IIM-036 concilia comisiones y movimientos externos.

**Acción:** diferenciar estimated_cost de realized_cost, conservar versión de estimación y vincularla con órdenes/fills; medir slippage y error de estimación sin sobrescribir la estimación original.

### INT-05 — Elegibilidad de venue y seguridad/custodia tienen fronteras distintas
**Severidad:** CRITICAL  
**Requisitos:** IIM-032, IIM-035, IIM-034, IIM-037.

IIM-032 decide si una entidad/mercado/operación es elegible en un scope y jurisdicción. IIM-035 gobierna identidades, claves, permisos, infraestructura y custodia. Una aprobación de venue no autoriza credenciales ni firma; controles de seguridad no prueban elegibilidad legal/comercial.

**Acción:** dos decisiones independientes y versionadas, ambas requeridas cuando aplican. Mantener aprobación por scope: datos, trading, depósitos, retiros y custodia.

### INT-06 — Ciclo de orden, control de autorización y conciliación deben ser capas separadas
**Severidad:** CRITICAL  
**Requisitos:** IIM-033, IIM-034, IIM-036; IIM-008, IIM-023, IIM-024.

IIM-033 registra estados y eventos de órdenes; IIM-034 permite/bloquea acciones y gestiona parada; IIM-036 compara ledger interno y fuentes externas. IIM-024 es workflow de evaluación, no máquina de estados de órdenes. IIM-008/IIM-023 aportan historial/snapshots, no son el ledger transaccional.

**Acción:** mantener OrderLifecycle, TradeControlDecision y ReconciliationCase como contratos distintos, enlazados por IDs. Un estado SUBMISSION_UNKNOWN nunca se resuelve por inferencia ni habilita retry ciego.

### INT-07 — Kill switch, cancelación y liquidación no son equivalentes
**Severidad:** CRITICAL  
**Requisitos:** IIM-034, IIM-033, IIM-035.

Una parada local no garantiza cancelación en venue. Cancelar órdenes no liquida posiciones. Liquidar posiciones puede aumentar riesgo y requiere autoridad propia.

**Acción:** separar STOP_NEW_ORDERS, CANCEL_OPEN_ORDERS y REDUCE/CLOSE_POSITIONS; mantener estado de parada persistente, evidencia de propagación y autorización independiente para reanudar.

### INT-08 — Reconciliación y finality multi-fuente requieren estados explícitos
**Severidad:** HIGH  
**Requisitos:** IIM-036, IIM-033, IIM-035.

Venue, custodio, broker y cadena pueden ofrecer tiempos, granularidad y finality diferentes. Una transacción observada on-chain no necesariamente está finalizada; una API puede estar atrasada.

**Acción:** declarar fuente de verdad por tipo de entidad/campo, cursor/cobertura/lag, finality policy por red, estados PENDING/CONFLICT/UNEXPLAINED/RECONCILED y proceso de ajuste append-only.

### INT-09 — Vocabulario de estado y semántica UNKNOWN no están consolidados
**Severidad:** HIGH  
**Requisitos:** todos; especialmente IIM-027–037.

Los lotes usan UNKNOWN, STALE, INVALID, BLOCKED, INFEASIBLE, INCONCLUSIVE, APPROVED, RESTRICTED y estados de órdenes. Sin un vocabulario común puede ocurrir que UNKNOWN sea interpretado como cero, PASS o no aplicable.

**Acción:** publicar catálogo de estados por dominio y reglas de transición. No forzar un único enum global: separar estado de dato, decisión, propuesta, autorización, orden, control, entorno y conciliación. UNKNOWN/STALE/INVALID/CONFLICT no equivalen a PASS.

### INT-10 — Falta contrato común de trazabilidad y temporalidad para cartera y ejecución
**Severidad:** HIGH  
**Requisitos:** IIM-023, IIM-025–037.

No se consolida todavía el envelope común de IDs, timestamps, actor, versiones, snapshots, lineage y evidencia a lo largo del ciclo completo.

**Acción:** definir IDs correlacionables para mandate_version, portfolio_id, proposal_id, authorization_id, risk_policy_version, exposure_snapshot_id, order_id, venue_order_id, fill_id, reconciliation_case_id, environment_id y audit_event_id. Distinguir event time, observed/received time y processing time; normalizar UTC sin perder timezone/sequence originales.

### INT-11 — Matriz editorial presenta duplicación y falta de estructura uniforme
**Severidad:** MEDIUM  
**Evidencia:** la matriz repite la sección 2.3 para el lote C y agrega la revisión del lote D al final como sección 8, fuera de la secuencia A–D.

**Acción:** corregir numeración y mantener secciones 2.1–2.4 homogéneas; conservar la matriz como artefacto de seguimiento, no como registro aprobado.

## 3. Modelo integrado de responsabilidades

| Dominio | Produce | Consume / aplica | No debe hacer |
|---|---|---|---|
| IIM-025 Mandate | Mandato vigente y facultades | IIM-026/031/028/034 | Autorizar una orden concreta |
| IIM-027 Exposure | Snapshot de exposiciones y cobertura | IIM-026/028/030/034 | Definir límites |
| IIM-028 Risk Policy | Resultado PASS/BREACH/BLOCKED según política | IIM-026/031/034 | Alterar métricas o ignorar vetos |
| IIM-026 Allocation | Propuesta de asignación | IIM-028/029/030/031 | Autorizar/crear órdenes |
| IIM-029 Cost/Liquidity | Estimación con dominio de validez | IIM-026/031/034 | Declarar fills reales |
| IIM-030 Stress | Impactos por escenarios | IIM-026/028/031 | Presentar escenarios no calibrados como probabilidades |
| IIM-031 Rebalance | Propuesta de transición | IIM-028/029/030/034 | Ejecutar por trigger |
| IIM-032 Venue Eligibility | Decisión contextual de elegibilidad | IIM-034/033 | Gestionar claves |
| IIM-035 Security/Custody | Identidades, permisos, secretos y firma segura | IIM-033/034/037/036 | Aprobar riesgo de contraparte |
| IIM-037 Environment | Evidencia de aislamiento/promoción | Gateway y controles | Promover por configuración implícita |
| IIM-033 Order Lifecycle | Estados y eventos de orden/fill | IIM-034/036 | Autorizar el envío |
| IIM-034 Trade Controls | Permiso/bloqueo y estado de parada | Gateway/Order Lifecycle | Alterar el historial de órdenes |
| IIM-036 Reconciliation | Casos y estado de conciliación | Portfolio ledger/reporting | Corregir historia sin evento compensatorio |

## 4. Cadena de extremo a extremo propuesta

1. Mandato versionado y vigente (IIM-025).
2. Universo e identidad elegibles (IIM-001/019/020).
3. Posiciones y exposición con cobertura conocida (IIM-027).
4. Riesgo medido y política aplicada (IIM-016/017/028).
5. Propuesta inicial o de rebalanceo (IIM-026/031).
6. Liquidez/costes y estrés evaluados (IIM-029/030).
7. Elegibilidad contextual del venue/contraparte (IIM-032).
8. Autorización humana o política explícita, acotada y auditable; no inferida del score.
9. Controles pre-trade, reserva y enforcement (IIM-034).
10. Order Gateway aislado y protegido por IIM-035/037.
11. Máquina de estados y eventos de orden/fills (IIM-033).
12. Controles post-trade y ledger actualizado por eventos.
13. Conciliación con venue/custodia/on-chain (IIM-036).
14. Reporting, snapshots e historial (IIM-008/010/023) y observabilidad.

Un gate bloqueante en cualquier etapa impide avanzar en el alcance afectado. La recuperación requiere evidencia y nueva autorización cuando corresponda.

## 5. Contratos conceptuales que deben especificarse antes de implementación

- InvestmentMandate y MandateVersion.
- RiskPolicy / RiskBudget y RiskControlResult.
- ExposureSnapshot / ExposureMetric con taxonomía y coverage.
- PortfolioProposal común a asignación inicial y rebalanceo.
- LiquidityCostEstimate con instrumento, venue, lado, tamaño, timestamp y dominio de validez.
- StressScenarioSet / PortfolioStressResult.
- VenueEligibilityDecision.
- TradeAuthorization acotada por acción, cuenta, cartera, instrumento, venue, tamaño, vigencia y entorno.
- PreTradeControlResult / PostTradeControlResult.
- OrderLifecycleEvent y OrderState.
- Fill y Fee/Movement normalizados.
- ReconciliationCase y LedgerAdjustmentEvent.
- EnvironmentPromotionRecord.
- AuditEvent / evidence envelope común.

Son nombres de contratos conceptuales, no schemas implementados o aprobados.

## 6. Decisiones que no deben fijarse en esta etapa

No definir sin datos, justificación y aprobación:
- umbrales de concentración, drawdown, exposición o liquidez;
- algoritmo/solver de optimización;
- frecuencia o trigger numérico de rebalanceo;
- límites de slippage, notional o tamaño de orden;
- tolerancias de conciliación;
- venues, custodios, jurisdicciones o redes aprobadas;
- políticas de finality específicas sin perfil técnico revisado;
- límites de reintentos/timeout genéricos no derivados de contratos de proveedor;
- custodia propia, apalancamiento, derivados o live trading.

## 7. Recomendación por requisito y acciones de salida

Los 13 requisitos permanecen PENDING_REVIEW con recomendación técnica REVISE. No se recomienda ACCEPTED_FOR_REGISTER todavía.

Para pasar a decisión formal:
1. resolver INT-01 a INT-10 en la redacción integrada;
2. corregir INT-11 en la matriz;
3. revisión de arquitectura y riesgo por revisores competentes;
4. revisión independiente de seguridad para IIM-034/035/037;
5. revisión legal/compliance aplicable para IIM-032/035/037;
6. registrar para cada ID decisión, revisor, rol, fecha, versión exacta, rationale, acciones, dependencias y evidencia;
7. actualizar el registro oficial solo después de aprobación explícita.

## 8. Límites de esta auditoría

No se inspeccionó una implementación transaccional porque la evidencia documental revisada no demuestra que exista en este repositorio. No se ejecutaron pruebas. No se verificaron proveedores, endpoints, licencias, contratos legales, exchanges, custodia ni infraestructura productiva. No se declara readiness para paper/live trading.
