# Auditoría de consistencia de X-09 contra X-01 a X-08

**ID:** IIE-METH-CONFORMANCE-AUDIT-011  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** AUDIT_COMPLETE / REVISE / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Objeto auditado:** `docs/methodology/14-integrated-conformance-scenario-matrix.md` (IIE-METH-CONFORMANCE-010, v0.1.0-draft)  
**Dependencias revisadas:** documentos 03 a 13, con foco en resoluciones X-01 a X-08.  
**Propósito:** evaluar coherencia semántica, cobertura declarada, determinismo, lifecycle, scope y gobernanza antes de aprobar X-09 como baseline de conformidad.

## 1. Dictamen ejecutivo

**Dictamen: REVISE. X-09 es una buena matriz de escenarios de alto nivel, pero todavía no es una matriz de conformidad exhaustiva ni suficientemente precisa para convertirse directamente en fixtures normativos.**

La auditoría confirma que X-09 recoge correctamente varios principios fundamentales: separación de dimensiones, no compensabilidad de vetos, independencia AE/ORE, ausencia de autorización implícita, trazabilidad temporal, conservación de causas y SoD. También cubre 60 escenarios individuales, 16 combinados y 24 invariantes.

Sin embargo, existen discrepancias de vocabulario, resultados esperados ambiguos, cobertura no demostrada de catálogos completos y escenarios de lifecycle/enforcement que no especifican una conducta segura suficientemente concreta. La propia X-09 reconoce que los catálogos y las políticas siguen pendientes; por tanto, no procede aprobarla todavía.

Esta auditoría es documental. No ejecuta código, tests, fixtures, backtests ni validación empírica. No altera X-09, requisitos IIM, schemas ni código.

## 2. Alcance y método

Se contrastaron:
- M-02: modelo canónico de estados.
- M-03: elegibilidad analítica y operativa.
- M-04: RiskAssessment, RiskDomainStatus y VetoDecision.
- X-01/X-03: vocabulario riesgo/veto.
- X-02: catálogo de 12 dominios, 9 tipologías y aplicabilidad.
- X-04: catálogo canónico de 10 estados ORE.
- X-05: 10 clases de veto y propagación por finalidad/actividad/scope.
- X-06: outcomes analíticos, validación de inputs, composición y causas.
- X-07: lifecycle, freshness, TTL, eventos e invalidación.
- X-08: RACI, autoridad, SoD, excepciones y break-glass.

Criterios: equivalencia exacta de estados; independencia de objetos; alcance de las reglas; resultado por unidad atómica; fail-closed sin fail-broad; cobertura de catálogos y transiciones; trazabilidad escenario-regla-requisito-test; autoridad y enforcement.

## 3. Hallazgos

### X09-A01 — HIGH — Estados de outcome no canónicos o abreviados en escenarios

**Evidencia:** B-05 utiliza `VETOED`; S-025/S-027 usan `INELIGIBLE` para ORE; S-031 utiliza `REVIEW_REQUIRED/INPUTS_INCONSISTENT`. X-06 distingue DecisionOutcome (`DECISION_INELIGIBLE`, `DECISION_REVIEW_REQUIRED`, `DECISION_ABSTAIN`, etc.), RouteEligibilityStatus (`ROUTE_INELIGIBLE`, `ROUTE_REVIEW_REQUIRED`, etc.) y resultados de validación (`REJECTED_INVALID_INPUT`, `REVIEW_REQUIRED`, `INCOMPLETE`, `VALID`).

**Riesgo:** un implementador puede crear enums paralelos o confundir el estado de veto con el outcome analítico o el estado ORE.

**Corrección requerida:** reemplazar abreviaturas por outcomes tipados y exactos; especificar decision_type, scope, unidad atómica y envelope de validación en cada expected outcome. `VETO_ACTIVE` debe permanecer como estado de VetoDecision, no como sustituto de DecisionOutcome.

**Aceptación:** ningún escenario utiliza un estado sin namespace/tipo; todos los valores están en catálogos versionados o marcados explícitamente como placeholders no normativos.

### X09-A02 — HIGH — La matriz B-03 mezcla obsolescencia del riesgo con vigencia del veto

**Evidencia:** B-03 combina RiskDomainStatus STALE con un `NO_VETO_IDENTIFIED` antiguo y ordena “reevaluar veto y riesgo”. X-06 y X-07 separan los objetos y prohíben que la expiración de un assessment levante un VETO_ACTIVE. No toda obsolescencia de riesgo invalida automáticamente la decisión de veto ni demuestra que la evidencia base del veto haya caducado.

**Riesgo:** invalidación excesiva o, en sentido contrario, conservación acrítica de una conclusión de veto que dependa de evidencia invalidada.

**Corrección requerida:** modelar por separado freshness/validity de RiskDomainAssessment, VetoDecision, evidencia subyacente y policy version. La reevaluación del veto debe dispararse solo si sus dependencias o su propia política de vigencia lo requieren. Hasta adjudicar la vigencia, no convertir un NO_VETO_IDENTIFIED obsoleto en resultado favorable.

**Aceptación:** escenarios diferenciados para assessment de riesgo stale con veto vigente; evidencia del veto invalidada; policy de veto expirada; y NO_VETO_IDENTIFIED stale sin veto activo.

### X09-A03 — HIGH — Cobertura de catálogos declarada, pero no demostrada por crosswalk

**Evidencia:** X-02 define RSK-01..RSK-12 y CAT-01..CAT-09; X-05 define V-01..V-10; X-04 define diez estados ORE. X-09 exige cobertura del 100% de estados y tipos de veto, pero no contiene una tabla que demuestre qué escenario cubre cada dominio RSK, tipología CAT, clase V, estado ORE y transición.

**Riesgo:** la cifra de escenarios puede dar una impresión de exhaustividad que no puede auditarse. Dominios o estados podrían quedar sin pruebas sin que sea visible.

**Corrección requerida:** añadir matrices de cobertura explícitas para: (a) 12 dominios RSK × aplicabilidad; (b) 9 tipologías CAT y combinaciones relevantes; (c) 10 clases V de veto; (d) 10 estados ORE; (e) actividades/purpose de X-05; (f) transiciones de lifecycle y gobernanza. Marcar N/A con justificación aprobada, no por omisión.

**Aceptación:** cada elemento tiene al menos un escenario identificable o una exclusión justificada y revisada; el porcentaje de cobertura se calcula desde esas matrices.

### X09-A04 — HIGH — P-01 simplifica el veto de retiro y omite dependencias de settlement

**Evidencia:** P-01 afirma que un veto exclusivo de WITHDRAWAL no bloquea TRADE “por inferencia”. X-05 permite no propagar automáticamente, pero conserva la excepción cuando trade sea inseparable del settlement o comparta una ruta/servicio comprometido.

**Riesgo:** habilitar una operación cuya liquidación, financiación, custodia o salida depende del servicio suspendido.

**Corrección requerida:** hacer el expected outcome condicional a una comprobación de dependencias de settlement, financiación, custodia y ruta. `NO_PROPAGATION` solo puede utilizarse tras demostrar independencia; si la dependencia es desconocida, usar scope unresolved/review y bloquear la unidad operativa dependiente.

**Aceptación:** casos separados de trade independiente, trade con settlement compartido y dependencia desconocida.

### X09-A05 — HIGH — Los escenarios de invalidación parcial no definen el estado seguro del consumidor afectado

**Evidencia:** S-042 indica que un gap en dependency map no permite declarar invalidación completa; S-044 permite “estado parcial explícito, retry/reconciliation”. X-06 exige no producir outcome favorable para inputs inválidos/incompletos; X-07 exige determinar consumidores afectados y controlar la propagación.

**Riesgo:** el sistema puede reportar honestamente que la propagación fue parcial y, aun así, dejar decisiones dependientes utilizables.

**Corrección requerida:** establecer que todo consumidor potencialmente dependiente no resuelto queda en estado no favorable (stale, review, incomplete o equivalente tipado) hasta reconciliar el grafo y recomponer. Distinguir dependencias demostradas, no afectadas y desconocidas.

**Aceptación:** el test verifica tanto la honestidad del estado de propagación como el bloqueo de usos dependientes no reconciliados.

### X09-A06 — HIGH — VetoDecision, TTL y expiración carecen de un resultado temporal suficientemente tipado

**Evidencia:** S-022 indica que un VETO_ACTIVE con TTL vencido permanece bloqueado hasta decisión autorizada. X-07 define FRESHNESS_EXPIRED y separa eventos de estados; X-08 prohíbe levantamiento automático por TTL. X-09 no define si el veto queda vigente, stale, bajo revisión o sujeto a revalidación, ni cómo se representa esa dimensión sin sobrescribir VETO_ACTIVE.

**Riesgo:** los consumidores pueden tratar la expiración como liberación, o conservar indefinidamente una decisión sin revisión.

**Corrección requerida:** mantener VetoDecision y su estado decisional separados de lifecycle/freshness; definir evento de expiración, revisión obligatoria, bloqueo conservador durante revisión, SLA/owner y evento explícito de reemplazo/liberación autorizado.

**Aceptación:** TTL nunca libera el veto; expiración crea revisión trazable; solo nueva VetoDecision autorizada cambia el resultado aplicable.

### X09-A07 — HIGH — Break-glass y acciones de contención no están ligados al catálogo de acciones permitido

**Evidencia:** S-054 permite acción de emergencia delimitada; C-08 menciona “suspensión de acciones afectadas”. X-05 distingue STOP_NEW_ORDERS, CANCEL_OPEN_ORDERS, REDUCE_ONLY y CLOSE_ONLY y prohíbe inferir una acción de control desde VETO_ACTIVE. X-08 limita break-glass a contención/restauración, no a eludir autorización ni habilitar trade.

**Riesgo:** interpretación amplia de “contención” que podría permitir cancelar, reducir, cerrar, transferir o ejecutar operaciones sin autoridad definida.

**Corrección requerida:** referenciar un catálogo separado de emergency/control actions, autoridad por acción, condiciones, scope, prioridad, doble control y audit trail. Aclarar que break-glass no autoriza acciones comerciales ni levanta vetos.

**Aceptación:** cada acción de emergencia tiene permiso explícito; las acciones no listadas se deniegan por defecto.

### X09-A08 — HIGH — La excepción de aprobación automatizada de service identity es demasiado abierta

**Evidencia:** S-053 permite una excepción “salvo política explícita y aprobación automatizada permitida”. X-08 establece identidad de servicio propia, permisos limitados, separación de aprobaciones reservadas a personas y controles RBAC/ABAC; las decisiones no delegables deben identificarse formalmente.

**Riesgo:** una excepción vaga puede convertirse en vía de bypass de SoD o en autoridad implícita de un job.

**Corrección requerida:** exigir policy_id/version, tipo exacto de decisión automatizable, owner accountable, aprobación institucional, límites de scope, controles de identidad/credenciales, independencia técnica, logs y prohibiciones no delegables. En ausencia de esa política aprobada, denegar.

**Aceptación:** ningún service principal aprueba una decisión reservada a persona; automatización permitida solo mediante política aprobada y test negativo de bypass.

### X09-A09 — MEDIUM — Los invariantes no tienen una relación explícita con escenarios y reglas fuente

**Evidencia:** INV-01..INV-24 son verificables en principio, pero no se incluye crosswalk INV → S/C → regla X-01..X-08 → requisito → futuro test.

**Riesgo:** invariantes sin prueba, pruebas redundantes y regresiones que no se detectan cuando cambia una regla fuente.

**Corrección requerida:** añadir una matriz bidireccional de trazabilidad. Un invariante puede tener varios escenarios; todo escenario debe declarar qué regla e invariante verifica.

**Aceptación:** 24/24 invariantes mapeados a escenarios concretos y a reglas fuente; ningún escenario huérfano.

### X09-A10 — MEDIUM — La cobertura de estados/transiciones no está enumerada por catálogo

**Evidencia:** X-09 establece 100% de cobertura de estados canónicos y transiciones permitidas/prohibidas, pero el documento no enumera el conjunto completo de transiciones contra el que se mide. Tampoco define una métrica reproducible de denominador.

**Riesgo:** un porcentaje de cobertura no reproducible o una aceptación subjetiva.

**Corrección requerida:** declarar catálogo y versión de cada state machine; enumerar transiciones, guards, eventos, actor autorizado, estado destino y transiciones prohibidas. Calcular cobertura sobre ese inventario.

**Aceptación:** cobertura reproducible por máquina de estados y versión; ninguna transición se considera cubierta por mención genérica.

### X09-A11 — MEDIUM — P-02 no especifica con precisión la salida de valoración para representación wrapped

**Evidencia:** P-02 ordena “valorar representación afectada con cautela”. X-06 separa DECISION outcomes de valoración (ESTIMABLE, ESTIMABLE_WITH_LIMITATIONS, NOT_ESTIMABLE, INSUFFICIENT_EVIDENCE, NOT_RUN).

**Riesgo:** “con cautela” puede interpretarse como una valoración numérica utilizable pese a dependencia comprometida o evidencia insuficiente.

**Corrección requerida:** especificar si el resultado es NOT_ESTIMABLE, INSUFFICIENT_EVIDENCE o ESTIMABLE_WITH_LIMITATIONS según dependencias verificadas; identificar qué fuentes alternativas son independientes y qué condiciones habilitan estimación.

**Aceptación:** no existe el término libre “con cautela” como expected outcome ejecutable; cada caso mapea a estado de valoración tipado.

### X09-A12 — MEDIUM — Faltan condiciones explícitas de partición atómica y agregación para escenarios con scopes solapados

**Evidencia:** X-06 exige particionar el scope en unidades atómicas homogéneas antes de componer y agregar solo unidades compatibles. C-07 y C-14 prueban representaciones mezcladas y vetos solapados, pero no fijan el expected output por cada unidad ni la regla de agregación.

**Riesgo:** una propuesta de cartera o conjunto de rutas puede ocultar una unidad bloqueada dentro de un resultado agregado favorable.

**Corrección requerida:** para C-07/C-14 y escenarios equivalentes, enumerar las unidades atómicas, scope afectado/no afectado, outcomes por unidad y agregación final sin promediar ni ocultar bloqueos.

**Aceptación:** cada unidad conserva su outcome y causas; el agregado no puede mejorar el resultado de ninguna unidad ni autorizarla implícitamente.

## 4. Matriz de prioridades

| Hallazgo | Severidad | Acción mínima |
|---|---|---|
| A01 | HIGH | Tipar y normalizar outcomes |
| A02 | HIGH | Separar freshness de riesgo, veto y evidencia |
| A03 | HIGH | Construir crosswalk de catálogos completo |
| A04 | HIGH | Modelar dependencia de settlement para withdrawal-only |
| A05 | HIGH | Definir estado seguro de consumidores ante propagación parcial |
| A06 | HIGH | Tipar expiración/revisión de veto sin liberación automática |
| A07 | HIGH | Catálogo explícito de acciones break-glass |
| A08 | HIGH | Cerrar la excepción de aprobación automatizada |
| A09 | MEDIUM | Crosswalk invariantes-escenarios-reglas-tests |
| A10 | MEDIUM | Inventario versionado de estados y transiciones |
| A11 | MEDIUM | Outcomes tipados de valoración wrapped |
| A12 | MEDIUM | Expected outcomes por unidad atómica y agregación |

## 5. Elementos que sí son consistentes

La auditoría no invalida el diseño general. Se consideran alineados, a nivel documental y con las reservas indicadas:
- separación entre RiskDomainStatus y VetoDecision;
- no equivalencia entre VETO_NOT_ASSESSED y NO_VETO_IDENTIFIED;
- no compensabilidad de VETO_ACTIVE;
- separación entre AE, ORE, Decision y TradeAuthorization;
- no propagación automática entre actividades;
- preservación de decisiones históricas y versiones;
- idempotencia como requisito de lifecycle;
- separación entre aprobación, ejecución, reconciliación y auditoría;
- no liberación de vetos por TTL o break-glass;
- uso de escenarios combinados y pruebas metamórficas/failure injection como estrategia futura.

## 6. Condiciones para cerrar la auditoría

X-09 no debe pasar a APPROVED hasta que:
1. se resuelvan los 8 hallazgos HIGH;
2. se resuelvan o acepten formalmente con rationale los 4 hallazgos MEDIUM;
3. se añadan crosswalks de catálogos, invariantes, escenarios, reglas y transiciones;
4. los owners de Risk, Architecture, Operations, Security, Legal/Compliance y Governance revisen los expected outcomes que les corresponden;
5. se mantenga la distinción entre propuesta documental, aprobación institucional, implementación y validación.

## 7. Limitaciones

La revisión fue documental y asistida. No es auditoría independiente certificada, revisión legal, evaluación de seguridad, validación cuantitativa ni prueba del comportamiento de un sistema ejecutable. No se ejecutaron tests ni se verificaron roles reales. Los hallazgos identifican defectos de especificación y evidencia insuficiente de cobertura, no incidentes de producción.

**Estado final: AUDIT_COMPLETE / REVISE / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

No se modificaron X-09, requisitos IIM, schemas ni código. Las correcciones de X-09 deben realizarse mediante revisión versionada y aprobación explícita de sus owners.
