# Borrador integrado de requisitos Portfolio & Execution

**Versión:** 0.2.0-draft  
**Estado:** NOT APPROVED / PENDING_REVIEW  
**Fecha:** 2026-10-31

Este documento consolida las propuestas IIM-025 a IIM-037 y los hallazgos INT-01 a INT-11. Es un borrador para revisión humana. No modifica el registro normativo, no demuestra implementación ni pruebas y no habilita uso operativo.

## Principios de integración

1. Mandato, política de riesgo, autorización y enforcement son conceptos distintos.
2. Medición de exposición, medición de riesgo, análisis de estrés y aplicación de límites tienen responsabilidades separadas.
3. Asignación inicial y rebalanceo comparten un contrato de propuesta, pero conservan sus disparadores específicos.
4. Estimaciones no se confunden con resultados observados.
5. Elegibilidad de terceros y seguridad técnica requieren decisiones independientes.
6. Ciclo de órdenes, controles y conciliación son dominios separados.
7. UNKNOWN, STALE, INVALID y CONFLICT no equivalen a PASS ni a cero.
8. Cada decisión y transición conserva identidad, versiones, tiempos y evidencia.

## 1. IIM-025 — Mandato de inversión

Definir mandatos versionados por cartera y subcartera con autoridad aprobadora, objetivos, horizonte, moneda de referencia, universo elegible, benchmark cuando aplique, necesidades de liquidez, restricciones, facultades, jurisdicción y vigencia. Cada propuesta debe referenciar una versión vigente e inmutable. Mandatos ausentes, ambiguos, vencidos o revocados bloquean nuevas propuestas. El mandato no constituye autorización para una acción operativa concreta.

## 2. IIM-026 — Construcción y asignación

Generar propuestas reproducibles a partir del mandato, posiciones, universo, exposición, riesgo, liquidez, costes y restricciones aprobadas. Declarar método, entradas, supuestos, factibilidad, sensibilidad, alternativas y limitaciones. Abstenerse cuando los datos o restricciones no permitan una solución defendible. La salida no autoriza ni ejecuta acciones.

## 3. IIM-027 — Exposición y concentración

Calcular snapshots reproducibles de exposiciones directas e indirectas mediante taxonomía versionada. Cada métrica declara unidad, denominador, moneda, timestamp, fuente, método de agregación, cobertura, look-through e incertidumbre. Exposiciones desconocidas permanecen explícitas; nunca se convierten en cero.

## 4. IIM-028 — Presupuestos y límites de riesgo

Aplicar políticas versionadas de apetito, presupuestos y límites, distinguiendo métricas, alertas, límites duros y excepciones autorizadas. Evaluar antes y después de cambios de cartera. Incumplimientos, datos inválidos o cobertura insuficiente bloquean la decisión afectada. Las excepciones requieren autoridad, alcance, vigencia, justificación y evidencia, y no anulan vetos no dispensables.

## 5. IIM-029 — Liquidez y coste

Estimar liquidez y coste esperado por instrumento, venue, lado, tamaño y horizonte. Declarar spread, profundidad, comisiones, slippage, impacto, gas o price impact cuando apliquen, además de fuente, timestamp, supuestos, cobertura e incertidumbre. El volumen reportado no demuestra capacidad ejecutable; una estimación no es un coste realizado.


## 6. IIM-030 — Escenarios y estrés

Evaluar carteras mediante escenarios históricos, hipotéticos y deterministas; usar escenarios probabilísticos solo cuando exista calibración validada. Declarar horizonte, shocks conjuntos, dependencias, supuestos, liquidez, costes y limitaciones. Incluir escenarios inversos para identificar condiciones que deterioren la viabilidad o infrinjan límites. Un escenario no calibrado no se presenta como predicción probabilística.

## 7. IIM-031 — Rebalanceo

Generar propuestas versionadas de transición entre posiciones actuales y objetivo, considerando mandato, límites, exposición, liquidez, costes y restricciones. Definir triggers, bandas, prioridades, eventos y condiciones de abstención. Cuantificar turnover, costes e impactos antes/después. Un trigger solo inicia evaluación; nunca autoriza por sí mismo una acción.

## 8. IIM-032 — Elegibilidad de venues y contrapartes

Mantener decisiones versionadas por entidad, jurisdicción, cuenta, instrumento, red, producto y tipo de operación. Distinguir consulta de datos, operaciones de mercado, depósitos/retiros y custodia. Conservar identidad, evidencia, incidentes, restricciones, vigencia y autoridad. Evidencia desconocida, contradictoria, restringida o vencida bloquea el alcance afectado. Esta decisión no sustituye controles de seguridad.

## 9. IIM-033 — Ciclo de vida de órdenes

Gestionar estados y eventos desde intención y autorización hasta envío, reconocimiento externo, fills parciales/totales, cancelación, rechazo, expiración o estado incierto. Mantener IDs internos/externos, correlación con propuesta y autorización, secuencia temporal e idempotencia específica del venue. Procesar eventos duplicados, tardíos o fuera de orden de forma determinista. Un resultado de envío incierto exige consultar y conciliar el estado externo antes de cualquier reintento; nunca inferir aceptación o fill por ausencia de error.

## 10. IIM-034 — Controles y parada

Aplicar controles independientes y fail-closed antes y después de acciones, verificando mandato, autorización, elegibilidad, identidad, saldo, exposición, riesgo, liquidez e instrumento. Prevenir carreras mediante reserva/validación atómica o mecanismo equivalente. Distinguir detener nuevas órdenes, solicitar cancelación de órdenes abiertas y reducir/cerrar posiciones; son acciones diferentes y requieren autoridad explícita. La reanudación requiere autorización independiente y evidencia.

## 11. IIM-035 — Seguridad, secretos y custodia

Proteger identidades, credenciales, secretos, material criptográfico e infraestructura mediante autenticación fuerte, mínimo privilegio, segregación, aislamiento, gestión de secretos, protección de firma, hardening, auditoría y respuesta a incidentes. Separar análisis, autorización, ejecución y custodia/firma. Prohibir secretos en código, repositorios y logs no autorizados. Exigir modelado de amenazas, revisión especializada y pruebas antes de cualquier uso con activos reales.

## 12. IIM-036 — Conciliación

Conciliar de forma reproducible y bidireccional órdenes, fills, posiciones, saldos, comisiones y movimientos contra fuentes autorizadas. Definir fuente de verdad por tipo de dato, snapshots, eventos incrementales, finality, tolerancias aprobadas y clasificación de discrepancias. Toda diferencia conserva evidencia, estado, responsable y resolución. No corregir silenciosamente ni marcar como conciliado un caso no resuelto.

## 13. IIM-037 — Aislamiento y promoción de entornos

Aislar investigación/backtest, simulación, paper y producción mediante separación verificable de identidades, permisos, secretos, redes, endpoints, cuentas, wallets, datos y configuración. Promover únicamente artefactos versionados e inmutables mediante gates independientes con evidencia de pruebas, riesgo, seguridad, cumplimiento y autorización humana. Producción permanece deshabilitada por defecto y requiere rollback y revocación verificables.

## 14. Contratos transversales

- **InvestmentMandate:** alcance, objetivos, restricciones y facultades.
- **RiskPolicy / RiskBudget:** límites y presupuestos aprobados.
- **ExposureSnapshot:** métricas, cobertura, unidad, denominador, timestamp y lineage.
- **PortfolioProposal:** contrato común para INITIAL_ALLOCATION y REBALANCE.
- **LiquidityCostEstimate:** estimación con supuestos y dominio de validez.
- **VenueEligibilityDecision:** elegibilidad contextual por entidad y operación.
- **TradeAuthorization:** permiso independiente y acotado por acción, cuenta, cartera, instrumento, venue, tamaño, entorno y vigencia.
- **TradeControlDecision:** decisión pre/post y enforcement.
- **OrderLifecycleEvent:** estado/evento de orden y fills.
- **ReconciliationCase:** discrepancia, evidencia y resolución.
- **EnvironmentPromotionRecord:** gates y autorización de promoción.
- **AuditEvent:** actor, IDs correlacionados, versiones, tiempos y evidencia.

Son contratos conceptuales; no schemas implementados.

## 15. Reglas de precedencia

1. Vetos legales, de seguridad o no dispensables prevalecen sobre scores y propuestas.
2. UNKNOWN, STALE, INVALID, CONFLICT o cobertura insuficiente nunca equivalen a PASS ni a exposición cero.
3. Mandato vigente no sustituye autorización específica.
4. Propuesta o trigger no genera una orden.
5. Coste estimado no demuestra coste realizado.
6. Estado de envío incierto no permite reintento ciego.
7. Detener nuevas órdenes, cancelar órdenes abiertas y reducir posiciones son acciones distintas.
8. Discrepancia no resuelta no equivale a conciliación.
9. Producción permanece deshabilitada por defecto.
10. Cambios de política, mandato o mapping generan nuevas versiones; no reescriben snapshots históricos.

## 16. Dependencias con IIM-001 a IIM-024

- IIM-001/019/020: identidad, listing y universo.
- IIM-002/018: fuentes, licencias, calidad, lineage y point-in-time.
- IIM-016/017: perfil de riesgo y vetos.
- IIM-022: decisión informativa; no autoriza operaciones.
- IIM-008/023: historial y snapshots; no sustituyen ledger transaccional.
- IIM-024: workflow de evaluación; no sustituye la máquina de estados externa.
- IIM-007/015: validación temporal y calibración; no sustituyen gates de seguridad.

## 17. Estado y limitaciones

**DRAFT FOR REVIEW — NOT APPROVED.** Los 13 requisitos siguen PENDING_REVIEW. Se requiere revisión humana de arquitectura, riesgo y operaciones; revisión independiente de seguridad; y revisión legal/compliance donde aplique. Este documento no declara implementación, pruebas, validación empírica ni readiness operativa.
