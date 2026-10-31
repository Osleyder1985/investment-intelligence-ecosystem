# Revisión técnica del lote D — IIM-033, IIM-034 e IIM-036

**Versión:** 0.1.0  
**Fecha:** 2026-10-31  
**Estado:** REVIEW_RECOMMENDATIONS_ONLY  
**Issue de seguimiento:** #38  
**Baseline:** propuesta IIM-025 a IIM-037 v0.1.0 y matriz de revisión v0.1.0

> Revisión técnica-documental asistida. No es aprobación formal, auditoría independiente, certificación de seguridad, validación operativa ni autorización de trading. Los IDs permanecen provisionales y sus estados formales permanecen PENDING_REVIEW.

## 1. Conclusión ejecutiva

| ID | Recomendación | Brecha principal |
|---|---|---|
| IIM-033 | REVISE | La máquina de estados debe modelar incertidumbre externa, eventos duplicados/fuera de orden, fills parciales y cancelación no confirmada. |
| IIM-034 | REVISE | Los controles deben ser independientes, fail-closed, secuenciados y capaces de detener nuevas órdenes y gestionar órdenes abiertas. |
| IIM-036 | REVISE | Se necesitan fuentes de verdad por dato, ledger de eventos, reconciliación de doble vía y resolución controlada de discrepancias. |

La documentación existente de workflow ya cubre request_id, idempotency_key, recuperación, estados y observabilidad para evaluaciones. Es una base reutilizable, pero no basta para órdenes: un timeout del cliente no permite concluir que el venue no recibió o no ejecutó la orden.

## 2. IIM-033 — Ciclo de vida de órdenes

### Hallazgos

La formulación inicial no define suficientemente:
- distinción entre intención, propuesta, autorización, orden lógica, intento de envío, orden externa y ejecución/fill;
- estados de envío incierto cuando se pierde conexión después de transmitir;
- identificadores internos y externos, client_order_id, idempotency key y correlación;
- eventos duplicados, retrasados o fuera de orden;
- fills parciales, cancelación pendiente y carreras entre cancelación y fill;
- reemplazos/amendments y cancel-replace;
- expiración, rechazo, suspensión del mercado y órdenes huérfanas;
- semántica de idempotencia específica de cada venue/API;
- reconciliación antes de reintentar una operación externa ambigua.

### Redacción propuesta

**IIM-033 — Gestionar el ciclo de vida versionado y auditable de cada orden desde la intención y propuesta hasta su autorización, envío, reconocimiento externo, fills parciales/totales, cancelación, rechazo, expiración o resolución de estado incierto. Mantener identificadores internos y externos, correlación con mandato, propuesta y autorización, secuencia temporal de eventos y semántica de idempotencia específica del venue. Los eventos duplicados, tardíos o fuera de orden deben procesarse de forma determinista; ante resultado de envío incierto, el sistema debe consultar/reconciliar el estado externo antes de reintentar y nunca inferir aceptación o fill por ausencia de error.**

### Criterios de aceptación recomendados

1. Diferencia intent, proposal, authorization, logical order, submission attempt, venue order y execution/fill.
2. Mantiene IDs internos inmutables y client_order_id/exchange_order_id cuando el venue los soporte; documenta limitaciones cuando no.
3. Una autorización está ligada a cartera, instrumento, lado, tamaño/notional, tipo de orden, venue, límites, vigencia y policy version.
4. Distingue CREATED, VALIDATED, AUTHORIZED, SUBMISSION_PENDING, SUBMISSION_UNKNOWN, ACKNOWLEDGED/OPEN, PARTIALLY_FILLED, FILLED, CANCEL_PENDING, CANCELLED, REJECTED, EXPIRED y terminales/reconciliados equivalentes.
5. SUBMISSION_UNKNOWN y CANCEL_PENDING no son estados terminales ni autorizan reenvío ciego.
6. Registra cada intento y evento con timestamp de origen/recepción, secuencia cuando exista, payload normalizado, source, correlation IDs y deduplication key.
7. Eventos duplicados son idempotentes; eventos fuera de orden se retienen/procesan mediante reglas deterministas sin retroceder estados válidos.
8. Los fills parciales actualizan cantidades acumuladas, remaining quantity, precio medio y comisiones sin duplicación.
9. Una cancelación solicitada no equivale a cancelación confirmada; puede competir con un fill y exige reconciliar ambos resultados.
10. Retry de envío solo después de consulta externa o mecanismo idempotente documentado y validado para ese venue.
11. Rechazos, expiraciones, suspensiones, desconexiones y órdenes huérfanas tienen tratamiento y escalado definidos.
12. Conserva historial append-only; correcciones y reconciliaciones agregan eventos, no reescriben silenciosamente el pasado.
13. Si el venue no proporciona semántica suficiente para garantizar idempotencia/consulta, la política limita o bloquea la operación afectada.

### Decisión recomendada

**REVISE.** Es necesario definir un contrato de orden independiente del workflow de evaluación. La garantía de exactly-once no debe prometerse de extremo a extremo cuando el venue no ofrece soporte; diseñar envío al menos una vez más deduplicación/reconciliación controlada es más honesto.

## 3. IIM-034 — Controles pre-trade, post-trade y parada de emergencia

### Hallazgos

La propuesta inicial no define:
- orden obligatorio de controles y autoridad de cada uno;
- validación de autorización, mandato, identidad, venue, mercado, saldo, exposición, liquidez y límites;
- controles de precio/tamaño, precisión, tick/lot size, min notional y restricciones del venue;
- reservas de capital/exposición para órdenes abiertas y parciales;
- race conditions entre validación y envío;
- diferencia entre bloquear nuevas órdenes, cancelar abiertas y cerrar posiciones;
- independencia y alcance del kill switch;
- protección frente a reinicio accidental y criterios de reanudación;
- controles post-trade de desviaciones, límites y alertas;
- evidencia de pruebas negativas y de fallo.

### Redacción propuesta

**IIM-034 — Aplicar controles independientes, versionados y fail-closed antes de autorizar/enviar órdenes y después de cada evento de ejecución, verificando mandato, autorización, elegibilidad, identidad, venue, saldo, exposición, riesgo, liquidez, parámetros del instrumento y límites operativos. Los controles deben prevenir condiciones de carrera mediante validación/reserva atómica o mecanismo equivalente, registrar bloqueos y excepciones autorizadas, y proporcionar mecanismos de parada que impidan nuevas órdenes, gestionen órdenes abiertas y definan de forma explícita las acciones permitidas sobre posiciones. La reanudación exige autorización independiente y evidencia de resolución.**

### Criterios de aceptación recomendados

1. Controles pre-trade incluyen mandato vigente, usuario/servicio autorizado, scope de autorización, activo/instrumento, red, venue, listing, estado del mercado, saldo disponible y reservado, exposición, límites, liquidez/costes y parámetros de orden.
2. Validación de tick size, lot size, min/max quantity/notional, precision, time-in-force y reglas específicas del venue.
3. Reevalúa límites considerando órdenes abiertas, fills parciales y órdenes concurrentes para evitar sobreasignación.
4. Reserva de saldo/riesgo y autorización de envío son atómicas o protegidas por control equivalente frente a race conditions.
5. Los estados UNKNOWN, STALE, INVALID, CONFLICTING o insuficientemente cubiertos producen DENY/BLOCK o abstención.
6. No se permite que ranking, score o rentabilidad esperada compensen un veto o límite duro.
7. Controles post-trade recalculan posiciones, exposición, límites, costes, desviaciones y estado de conciliación.
8. Define kill switch con autoridad, alcance, autenticación fuerte, activación, propagación, persistencia y auditoría.
9. Diferencia STOP_NEW_ORDERS, CANCEL_OPEN_ORDERS y REDUCE/CLOSE_POSITIONS; estas últimas acciones requieren política y autorización separadas.
10. Al activarse, bloquea nuevas órdenes y define tratamiento de órdenes pendientes/ambiguas; no supone que el venue haya cancelado nada hasta confirmación.
11. Reinicio no desactiva automáticamente el estado de parada; reanudación requiere revisión y autorización independiente.
12. Alertas, escalado, runbooks y pruebas de fallo incluyen pérdida de conectividad, credenciales comprometidas, datos stale, límites excedidos y fallos de persistencia.
13. Cada control registra resultado, versión, inputs, evidencia, actor, timestamp y razón.
14. Umbrales cuantitativos y excepciones se aprueban mediante gobernanza; no se fijan valores universales en este requisito.

### Decisión recomendada

**REVISE.** El kill switch no debe describirse como botón mágico de liquidación. Detener nuevas órdenes, cancelar órdenes abiertas y liquidar posiciones son capacidades distintas con riesgos y permisos distintos.

## 4. IIM-036 — Conciliación

### Hallazgos

La propuesta inicial enumera órdenes, fills, balances y movimientos, pero no establece:
- fuente de verdad por campo/objeto y precedencia entre API, stream, ledger y custodia;
- reconciliación interna→externa y externa→interna;
- snapshots de balances y eventos incrementales;
- consistencia eventual, ventanas de retraso y reorgs/finalidad on-chain;
- comisiones, rebates, funding, staking rewards, depósitos/retiros, transferencias y ajustes;
- clasificación de discrepancias y materialidad aprobada;
- proceso de investigación, resolución y aprobación;
- prohibición de corregir balances internos silenciosamente;
- controles para datos faltantes, duplicados, reversos y eventos tardíos.

### Redacción propuesta

**IIM-036 — Conciliar de forma reproducible y bidireccional el ledger interno de órdenes, ejecuciones, posiciones, saldos, comisiones y movimientos contra las fuentes externas autorizadas de venue, broker, custodia y redes on-chain. Definir fuente de verdad y precedencia por tipo de dato, snapshots y eventos incrementales, reglas de finalización/consistencia, tolerancias aprobadas y clasificación de discrepancias. Toda diferencia debe conservar evidencia, estado, materialidad, responsable y resolución aprobada; ninguna corrección de ledger o balance puede realizarse silenciosamente ni convertir una discrepancia no resuelta en estado conciliado.**

### Criterios de aceptación recomendados

1. Define fuentes autorizadas y precedencia por entidad: orden, fill, saldo, posición, comisión, transferencia y estado on-chain.
2. Distingue ledger interno, estado reportado por venue/custodio y estado observado on-chain; no asume equivalencia automática.
3. Soporta snapshots y deltas/eventos, con cursor/secuencia, timestamps, cobertura y detección de huecos.
4. Realiza reconciliación bidireccional: registros internos ausentes externamente y eventos externos no reflejados internamente.
5. Detecta duplicados, eventos tardíos, reversos, fills parciales, comisiones, funding, rebates, rewards, depósitos/retiros y transferencias cuando apliquen.
6. Define finality/confirmations y tratamiento de reorganizaciones para redes compatibles; estado pendiente no se presenta como definitivo.
7. Define tolerancias por moneda/activo y fuente mediante política aprobada, sin valores universales implícitos.
8. Clasifica diferencias: timing/pending, missing, duplicate, quantity, price, fee, identity, valuation, chain finality, unauthorized y unexplained.
9. Una discrepancia material o unexplained bloquea el estado RECONCILED y puede bloquear nuevas órdenes según política.
10. Toda resolución conserva evidencia, actor, aprobador, timestamp, causa raíz, ajuste y relación con evento original.
11. Los ajustes se representan como nuevos eventos compensatorios/ajustes aprobados; no se mutan registros históricos.
12. Puede reconstruir balance/posición desde eventos y comparar contra snapshot externo.
13. Registra cobertura, última conciliación, lag, diferencias abiertas y estado global por cuenta/venue/cartera.
14. Define segregación entre quien propone un ajuste y quien lo aprueba; escalado y aging de discrepancias.
15. La falta de endpoint o fuente de verdad suficiente queda explícita y limita el alcance de conciliación.

### Decisión recomendada

**REVISE.** Conciliación debe ser un control contable-operativo independiente, no una tarea de reporting. No declarar saldo correcto cuando la fuente externa está atrasada o incompleta.

## 5. Contratos y límites entre requisitos

- **IIM-024:** idempotencia y recuperación de workflows de análisis; aporta patrones, no define semántica de órdenes externas.
- **IIM-033:** estado y eventos de cada orden y fill.
- **IIM-034:** autorización, validaciones y enforcement de límites; decide si se puede enviar/continuar.
- **IIM-036:** compara ledger interno con evidencia externa y gestiona discrepancias.
- **IIM-008/IIM-023:** snapshots e historial/auditoría generales; no sustituyen el ledger transaccional.
- **IIM-035/IIM-037:** seguridad, custodia y aislamiento; condicionan los permisos y entornos donde estos controles operan.

Flujo conceptual:
1. Propuesta de orden vinculada a mandato/cartera.
2. Controles pre-trade y reserva de capacidad.
3. Autorización explícita y acotada.
4. Envío mediante gateway; estado incierto se consulta antes de reintentar.
5. Eventos de venue/fills procesados idempotentemente.
6. Controles post-trade y ledger actualizado por eventos.
7. Conciliación externa y resolución de discrepancias.
8. Auditoría inmutable y monitorización.

## 6. Casos negativos obligatorios para una futura suite

- Timeout inmediatamente después de transmitir; el venue sí aceptó la orden.
- Timeout antes de transmitir; el venue no recibió la orden.
- Reenvío de mismo client_order_id con soporte y sin soporte de idempotencia del venue.
- Dos fills parciales duplicados/retrasados y evento total recibido antes del parcial.
- Cancelación compite con fill final.
- Kill switch durante SUBMISSION_UNKNOWN y con órdenes abiertas.
- Reinicio del servicio mientras el kill switch está activo.
- Saldo externo difiere por comisión, funding, depósito o retiro pendiente.
- Reorganización on-chain revierte una observación previa.
- Evento externo sin orden interna asociada.
- Ledger interno contiene fill sin respaldo externo.
- Dos órdenes concurrentes intentan consumir el mismo saldo o presupuesto de riesgo.

Estos son casos de diseño; no se afirma que estén automatizados ni ejecutados.

## 7. Estado y límites

- IIM-033: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-034: **PENDING_REVIEW** — recomendación técnica: REVISE.
- IIM-036: **PENDING_REVIEW** — recomendación técnica: REVISE.

Esta revisión no modifica el registro oficial IIM-001 a IIM-024, no aprueba IIM-025 a IIM-037, no implementa controles y no habilita paper/live trading. Se requiere revisión humana competente, revisión independiente de seguridad para controles de ejecución y pruebas técnicas reproducibles antes de cualquier promoción.
