# Gobernanza RACI y segregación de funciones (SoD)

**ID:** IIE-METH-GOV-RACI-009  
**Versión:** 0.1.0-draft  
**Fecha:** 2026-10-31  
**Estado:** DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED  
**Hallazgo:** X-08 (MEDIUM), auditoría cruzada M-02/M-03/M-04  
**Dependencias:** M-02 a M-12; especialmente composición DECISION (11) y lifecycle/invalidation (12).

## 1. Propósito

Definir quién puede producir, revisar, aprobar, ejecutar, supervisar y auditar las actividades críticas del Investment Intelligence Ecosystem (IIE). La propuesta establece una matriz RACI, incompatibilidades SoD, autoridades de decisión y controles compensatorios.

La gobernanza organizativa precede al diseño de RBAC/ABAC y de los mecanismos de aprobación. No basta con que un motor aplique correctamente permisos si una misma identidad humana o de servicio puede controlar todas las etapas de un proceso crítico.

Este documento es una propuesta metodológica: no aprueba requisitos IIM, no crea autoridad real para ninguna persona, no configura permisos, no implementa controles y no demuestra eficacia. Requiere aprobación de los responsables institucionales y revisión legal/compliance, seguridad, riesgo, operaciones y arquitectura.

## 2. Principios no negociables propuestos

1. **Separación de producción y validación:** quien origina evidencia o un assessment no puede ser su único validador.
2. **Separación de recomendación y aprobación:** el autor de una recomendación no debe aprobarla en solitario cuando sea material.
3. **Separación de emisión y levantamiento de vetos:** quien propone/emite un veto no puede levantarlo unilateralmente.
4. **Separación de desarrollo y validación independiente:** el model owner no valida ni aprueba por sí solo su modelo.
5. **Separación de operación y reconciliación/auditoría:** operadores no certifican unilateralmente sus propios resultados.
6. **Independencia de DECISION:** el compositor consume assessments; no los crea, modifica, aprueba ni rebaja.
7. **Independencia de enforcement:** un resultado AE/ORE no es mandato, TradeAuthorization ni permiso técnico.
8. **Trazabilidad append-only:** cada acto privilegiado conserva actor, rol, objeto, scope, motivo, evidencia, versión, timestamp y resultado.
9. **Mínimo privilegio y necesidad de conocer:** el acceso se limita a acciones, objetos, scopes y entornos necesarios.
10. **Fail closed:** falta de autoridad, conflicto de interés, identidad no verificable o aprobación incompleta no equivale a aprobación.
11. **Independencia proporcional al riesgo:** mayor materialidad, irreversibilidad o impacto sistémico exige mayor separación, revisión y quorum.
12. **No autoexcepción:** nadie puede aprobar una excepción a una restricción que esa misma persona solicitó o controla, salvo mecanismo de emergencia expresamente limitado y revisado posteriormente.

## 3. Distinciones conceptuales

### 3.1 RACI no es autorización

RACI describe responsabilidad organizativa:
- **R — Responsible:** ejecuta/prepara el trabajo.
- **A — Accountable:** responde por el resultado y toma la decisión de aprobación dentro de su mandato.
- **C — Consulted:** aporta criterio antes de decidir.
- **I — Informed:** recibe notificación.

Cada actividad crítica tendrá exactamente un A en el modelo ordinario. Si la legislación, estructura o naturaleza colegiada exige aprobación conjunta, se definirá un órgano/quorum y una autoridad accountable explícita; no se representará ambiguamente con múltiples A.

RACI por sí solo no impide que una persona acumule roles. La separación debe reflejarse en asignaciones de personas, grupos, identidades de servicio, políticas de acceso, workflow y controles verificables.

### 3.2 Capas de autoridad que no deben confundirse

1. **Autoridad organizativa:** rol, mandato, delegación y límites de competencia.
2. **Autenticación:** prueba de identidad humana o de servicio.
3. **Autorización:** permiso contextual para una acción concreta.
4. **Enforcement:** componente que permite, bloquea o limita la acción.
5. **Supervisión y aseguramiento:** control independiente de que los anteriores funcionaron.

Una aprobación humana no debe convertirse automáticamente en permiso técnico sin comprobar contexto, vigencia, scope, estado del objeto y política aplicable.

### 3.3 Objetos de decisión independientes

Mantener separados EvidenceRecord, EvidenceAssessment, IdentityAssessment, RiskDomainAssessment, VetoDecision, AE, ORE, SelectionDecision, PortfolioDecision, TradeAuthorization, TradeControlDecision y AuditEvent. Cada objeto conserva propietario, productor, revisor, aprobador, versión y referencias de evidencia.

## 4. Catálogo de roles propuesto

Los roles son funciones, no nombres de personas. Una persona puede ocupar varios roles no incompatibles según criticidad, pero nunca se presume independencia por el mero hecho de usar cuentas distintas.

| Código | Rol | Responsabilidad primaria |
|---|---|---|
| GOV-01 | Governing Body / Investment Governance Committee | Aprobar mandato, apetito de riesgo, delegaciones y políticas reservadas |
| GOV-02 | Methodology Owner | Mantener metodología, taxonomía y criterios; proponer cambios |
| GOV-03 | Change Control Authority | Aprobar cambios versionados y su promoción |
| DAT-01 | Source Owner / Data Producer | Publicar observaciones y procedencia de datos |
| DAT-02 | Data Steward / Quality Reviewer | Evaluar calidad, integridad, cobertura y conflictos |
| DAT-03 | Data/Platform Engineer | Operar ingestión, almacenamiento, pipelines y disponibilidad |
| ID-01 | Asset Identity Analyst | Proponer identidad, representación y relaciones |
| ID-02 | Identity Reviewer | Revisar independientemente identidad y scope |
| ANA-01 | Domain Analyst | Elaborar análisis fundamental, tokenomics, tecnología, mercado o valoración |
| ANA-02 | Independent Analytical Reviewer | Revisar evidencia, método, supuestos y reproducibilidad |
| RSK-01 | Risk Analyst | Evaluar dominios de riesgo y documentar hallazgos |
| RSK-02 | Risk Approver / Risk Officer | Aprobar evaluaciones y decisiones de riesgo dentro de mandato |
| RSK-03 | Independent Risk Reviewer | Revisar evaluaciones críticas y desacuerdos |
| VET-01 | Veto Proposer | Proponer veto con regla, evidencia y scope |
| VET-02 | Veto Approver | Aprobar, rechazar o mantener veto conforme a política |
| VET-03 | Veto Release Authority | Autorizar levantamiento tras remediación verificada |
| DEC-01 | Selection Analyst | Preparar recomendación de selección/abstención |
| DEC-02 | Decision Approver | Aprobar/rechazar decisión analítica dentro de delegación |
| PFM-01 | Portfolio Manager | Proponer construcción, asignación y rebalanceo |
| PFM-02 | Portfolio Risk Controller | Controlar límites, concentración, presupuesto de riesgo y exposición |
| EXE-01 | Trade Operator | Preparar y ejecutar órdenes autorizadas |
| EXE-02 | Trade Authorizer | Autorizar la acción concreta dentro de mandato |
| EXE-03 | Pre/Post-Trade Control | Controlar límites y cumplimiento antes/después de ejecución |
| CUS-01 | Custodian / Key Control Owner | Custodia de activos, claves y segregación de acceso |
| REC-01 | Reconciliation Officer | Conciliar órdenes, ejecuciones, saldos y movimientos |
| MOD-01 | Model Owner / Developer | Desarrollar, mantener y documentar modelos |
| VAL-01 | Independent Model Validator | Validar modelos, supuestos, rendimiento y limitaciones |
| LCM-01 | Lifecycle / Incident Owner | Coordinar incidentes, invalidación y reevaluación |
| CMP-01 | Legal / Compliance Officer | Interpretar obligaciones, jurisdicción, sanciones y restricciones |
| SEC-01 | Security Officer | Seguridad técnica, amenazas, accesos, incidentes y controles |
| AUD-01 | Internal / Independent Auditor | Evaluar diseño y eficacia de controles con independencia |
| IAM-01 | IAM / System Administrator | Administrar identidades, grupos, políticas y plataforma sin autoridad de negocio |

La asignación real, competencia, independencia y suplencias deben registrarse en un directorio de roles aprobado y revisarse periódicamente.

## 5. Matriz RACI de actividades críticas

**Convención:** R=Responsible, A=Accountable, C=Consulted, I=Informed. Cada fila propone un único A. La matriz es baseline de diseño, no delegación efectiva.

| Actividad | R | A | C | I |
|---|---|---|---|---|
| Aprobar universo y taxonomía | GOV-02 | GOV-01 | RSK-02, CMP-01, ANA-02 | AUD-01 |
| Publicar fuente/observación | DAT-01 | Source Owner designado | DAT-02, SEC-01 | DAT-03 |
| Validar calidad y admisibilidad de evidencia | DAT-02 | Data Quality Owner designado | DAT-01, SEC-01 | ANA-01, RSK-01 |
| Operar pipelines y plataforma de datos | DAT-03 | Platform Owner designado | DAT-02, SEC-01 | AUD-01 |
| Proponer identidad de activo/representación | ID-01 | Identity Function Owner | DAT-02, SEC-01 | ANA-01, RSK-01 |
| Aprobar identidad crítica | ID-02 | Identity Function Owner independiente | ID-01, SEC-01 | DEC-01 |
| Elaborar assessment analítico | ANA-01 | Domain Function Owner | DAT-02, ID-02, CMP-01 | DEC-01 |
| Revisar assessment analítico | ANA-02 | Head of Analytical Review | ANA-01, RSK-03 | DEC-02 |
| Evaluar riesgo por dominio | RSK-01 | RSK-02 | SEC-01, CMP-01, DAT-02 | DEC-01, PFM-02 |
| Revisar riesgo material | RSK-03 | RSK-02 | RSK-01, CMP-01, SEC-01 | DEC-02 |
| Proponer veto | VET-01 / RSK-01 | Risk Function Owner | CMP-01, SEC-01, RSK-03 | DEC-02, ORE Owner |
| Aprobar o rechazar veto | RSK-02 / VET-02 | Risk Function Head | CMP-01, SEC-01, RSK-03 | DEC-01, DEC-02, LCM-01 |
| Autorizar levantamiento de veto | VET-03 | Risk Function Head independiente | RSK-03, CMP-01, SEC-01 | DEC-02, ORE Owner |
| Determinar AE / ORE | DEC-01 / ORE Assessment Owner | Respective Eligibility Owner | RSK-02, CMP-01, Operations | DEC-02, EXE-02 |
| Preparar selección y ranking | DEC-01 | Selection Function Owner | ANA-02, RSK-02 | PFM-01 |
| Aprobar/rechazar decisión de selección | DEC-01 | DEC-02 | RSK-02, CMP-01, ANA-02 | PFM-01 |
| Aprobar mandato y risk policy de cartera | PFM-01 / PFM-02 | GOV-01 | RSK-02, CMP-01 | EXE-02, AUD-01 |
| Proponer asignación/rebalanceo | PFM-01 | Portfolio Manager Supervisor | PFM-02, DEC-02 | EXE-02 |
| Verificar límites de cartera | PFM-02 | Portfolio Risk Head | RSK-02, PFM-01 | EXE-03 |
| Generar/preparar orden | EXE-01 | Trading Operations Owner | PFM-01, PFM-02 | EXE-02 |
| Autorizar orden | EXE-02 | Trading Authority Owner | PFM-02, CMP-01, EXE-03 | EXE-01 |
| Ejecutar orden | EXE-01 | Trading Operations Owner | EXE-03 | REC-01, PFM-01 |
| Controles pre/post-trade | EXE-03 | Trading Control Head independiente | PFM-02, CMP-01, SEC-01 | AUD-01 |
| Administrar custodia y claves | CUS-01 | Custody Control Owner | SEC-01, CMP-01 | REC-01, AUD-01 |
| Conciliar operaciones y saldos | REC-01 | Reconciliation Head | CUS-01, EXE-03 | PFM-02, AUD-01 |
| Detectar/iniciar invalidación | LCM-01 / DAT-02 / RSK-01 | Lifecycle Owner | SEC-01, CMP-01, RSK-02 | DEC-02, PFM-02, EXE-03 |
| Resolver impacto y cerrar incidente | LCM-01 | Incident Authority según severidad | RSK-03, SEC-01, CMP-01, AUD-01 | consumidores afectados |
| Aprobar política TTL/freshness | GOV-02 / LCM-01 | Methodology Governance Authority | DAT-02, RSK-02, SEC-01 | AUD-01 |
| Desarrollar modelo | MOD-01 | Model Owner Function Head | ANA-01, RSK-02 | VAL-01 |
| Validar modelo independientemente | VAL-01 | Head of Model Risk / Validation | MOD-01, RSK-02, AUD-01 | GOV-01 |
| Aprobar promoción de modelo | MOD-01 | Model Governance Committee | VAL-01, SEC-01, RSK-02 | usuarios del modelo |
| Solicitar excepción/override | Owner de la actividad | Autoridad funcional distinta | CMP-01, RSK-02, SEC-01 | AUD-01 |
| Aprobar excepción material | Solicitante prepara expediente | Autoridad de excepción designada por GOV-01 | CMP-01, RSK-02, SEC-01, control independiente | AUD-01 |
| Aprobar cambio metodológico | GOV-02 | GOV-01 / Change Authority delegada | ANA-02, RSK-02, CMP-01, SEC-01 | AUD-01 |
| Administrar privilegios técnicos | IAM-01 | Security/IAM Owner | SEC-01, dueño de sistema | AUD-01 |
| Auditar diseño y eficacia | AUD-01 | Chief Audit / órgano independiente | CMP-01, SEC-01 | GOV-01 |

Las denominaciones “Owner designado” y “Head” requieren asignación formal. No se permite inferir una autoridad por cargo técnico, seniority o propiedad del repositorio.

## 6. Autoridad por tipo de decisión

### 6.1 Evidencia

- El productor puede registrar y corregir su propia observación mediante un nuevo evento versionado, sin borrar la historia.
- El Data Steward determina admisibilidad/calidad según política, con revisión independiente para evidencia crítica o controvertida.
- El analista consumidor puede señalar discrepancias, pero no alterar silenciosamente la evidencia fuente ni autoaprobar su calidad.
- Evidencia de procedencia no verificable, integridad fallida o conflicto material no resuelto se marca como tal; no se convierte en evidencia favorable por ausencia de alternativa.

### 6.2 Riesgos y vetos

- Risk Analyst documenta evaluación y hallazgos; Risk Approver es responsable de la aprobación de la evaluación.
- Veto Proposer presenta regla, evidencia, scope, materialidad, duración/review date y consecuencia.
- Veto Approver decide sobre el veto. Para veto crítico, transversal, legal/sanciones, custodia/seguridad o de alto impacto se requiere revisión independiente y consulta obligatoria a la función competente.
- El levantamiento requiere evidencia nueva de remediación, verificación independiente y aprobación por Veto Release Authority distinta del proponente original y, cuando sea practicable, del aprobador inicial.
- El motor lifecycle no levanta vetos automáticamente. TTL, silencio, ausencia de incidentes o una nueva versión no equivalen a liberación.
- La ausencia de veto registrado no equivale a NO_VETO_IDENTIFIED.

### 6.3 Decisiones analíticas

- Selection Analyst prepara recomendación, alternativas, evidencia, incertidumbre, vetos y motivos de abstención.
- Decision Approver puede aprobar, rechazar, devolver o exigir abstención; no puede editar assessments de riesgo o evidencia para obtener un resultado deseado.
- Una decisión aprobada sigue sin ser autorización operativa, mandato de cartera o autorización de trading.
- Los conflictos materiales y desacuerdos no resueltos deben registrarse; la autoridad no puede borrar el dissent.

### 6.4 Excepciones y levantamiento de restricciones

- Toda excepción se solicita con scope, objeto, regla afectada, justificación, riesgo residual, controles compensatorios, duración, expiración y plan de remediación.
- La autoridad de excepción debe ser superior o independiente del solicitante y no puede ser el dueño único del control que se pretende exceptuar.
- No son exceptuables por conveniencia: obligaciones legales/sanciones aplicables, autenticación, integridad de auditoría, controles de custodia críticos, límites no delegables definidos por mandato o controles que la ley/contrato prohíba dispensar.
- Una excepción no modifica retroactivamente el assessment original; genera decisión separada y trazable.
- Las excepciones temporales se revocan al expirar; la renovación exige nueva aprobación, no extensión implícita.
- Toda excepción que permita una acción bloqueada debe tener enforcement técnico del scope, importe, actividad, cuenta, entorno y ventana autorizados.

### 6.5 Operaciones, supervisión y auditoría

- Quien prepara una orden no debe ser su único autorizador ni su único ejecutor.
- Pre-trade control debe ser independiente del trader y verificar mandato, límites, ORE, autorización, venue, cuenta, tamaño, precio y estado de controles.
- Post-trade control y reconciliación contrastan órdenes, fills, comisiones, saldos y movimientos con fuentes independientes.
- Custodia de claves y activos se separa de trading, administración de sistemas y conciliación.
- Auditoría debe tener acceso de lectura suficiente, independencia funcional, protección frente a represalias y capacidad de reportar a un órgano de gobierno, no al owner auditado.
- El administrador técnico no puede alterar registros de auditoría ni concederse permisos de negocio sin flujo de aprobación independiente.

## 7. Matriz de incompatibilidades SoD

| Combinación de funciones | Regla propuesta | Control requerido |
|---|---|---|
| Producir evidencia + validarla en solitario | Prohibida en evidencia material | Revisión independiente |
| Proponer identidad + aprobarla en solitario | Prohibida para identidad crítica | Dual review |
| Elaborar assessment + aprobarlo en solitario | Prohibida para decisiones materiales | Independent review |
| Evaluar riesgo + aprobar riesgo en solitario | Prohibida para riesgo crítico | Risk approver distinto |
| Proponer/emitar veto + levantarlo | Prohibida | Release authority independiente |
| Recomendar selección + aprobarla en solitario | Prohibida cuando sea material | Decision approver separado |
| Solicitar excepción + aprobarla | Prohibida | Autoridad independiente |
| Desarrollar modelo + validarlo | Prohibida | Independent Model Validation |
| Cambiar política + aprobar su propia excepción | Prohibida | Change authority / risk approval |
| Preparar orden + autorizarla | Prohibida en operación real | Dual control |
| Ejecutar operación + reconciliarla | Prohibida como control único | Reconciliation independiente |
| Administrar privilegios + auditar esos privilegios | Prohibida | Auditoría independiente |
| Administrar logs + poder alterarlos sin detección | Prohibida | Append-only/WORM, firmas y monitor externo |
| Componer DECISION + mutar assessments fuente | Prohibida | Interfaces de solo lectura |
| Custodiar claves + ejecutar y aprobar retiros | Prohibida como control único | Múltiples custodios y autorización dual |

Una separación de cuentas no demuestra separación de funciones si la misma persona controla ambas credenciales, los factores de autenticación o el workflow de aprobación.

## 8. Excepciones para equipos pequeños

La falta de personal no elimina el riesgo ni convierte un conflicto en aceptable. Cuando no sea viable separar personas, se requieren controles compensatorios documentados y aprobados por autoridad independiente:

1. doble autorización de una persona externa o de un segundo responsable competente;
2. revisión independiente posterior, con SLA corto según criticidad;
3. límites reducidos de importe, exposición, scope y duración;
4. privilegios just-in-time y acceso temporal;
5. logs inmutables, alertas externas y evidencia de cada acción;
6. prohibición de que la persona con conflicto modifique el registro de revisión;
7. muestreo y auditoría reforzada;
8. plan y fecha para eliminar la excepción estructural.

La excepción debe identificar conflicto, causa, riesgo residual, controles compensatorios, owner, aprobador, inicio, expiración y criterio de salida. No se permite que el mismo actor solicite, apruebe, ejecute y certifique el control compensatorio.

## 9. Conflictos de interés, recusación y delegación

- Declarar conflictos personales, financieros, comerciales, de proveedor, token holdings, remuneración o relación con el proyecto/contraparte analizada.
- La persona conflictuada se recusa de análisis, aprobación y revisión del caso afectado; su aportación factual se conserva identificada.
- Registrar recusación, sustituto y motivo sin exponer datos personales innecesarios.
- Delegaciones deben ser explícitas, limitadas por scope, acción, importe/criticidad, jurisdicción y periodo.
- No se permite subdelegación implícita.
- Las decisiones colegiadas definen quorum, mayoría, recusaciones, voto disidente y desempate antes de conocer el caso.
- Las autoridades no delegables se identifican en la política aprobada: apetito de riesgo, límites máximos, excepciones prohibidas, levantamiento de vetos críticos, promoción a producción de modelos críticos y aceptación de riesgo residual extremo.

## 10. Emergencias y break-glass

Break-glass solo puede utilizarse para contener daño o restaurar seguridad/servicio, no para eludir una aprobación de inversión o habilitar una operación comercial bloqueada.

Requisitos:
- identidad fuerte y autenticación multifactor;
- motivo y ticket/incidente obligatorio;
- alcance mínimo, acción específica y expiración automática;
- doble control cuando sea viable; si no, notificación inmediata a supervisor independiente;
- registro append-only fuera del control del operador;
- alerta en tiempo real a Security, Risk y autoridad de guardia;
- revisión independiente posterior dentro del plazo definido por criticidad;
- revocación de privilegios temporales y análisis de causa raíz.

La acción de emergencia no levanta vetos ni crea autorización permanente. Las facultades de cancelación, pausa, reducción o cierre de posiciones se definen por separado en políticas de riesgo/operación; no se presume que un kill switch habilite cualquier acción.

## 11. Traducción a controles técnicos (RBAC/ABAC)

La futura implementación debe evaluar, como mínimo:

- **Principal:** identidad humana o de servicio autenticada.
- **Role:** función organizativa vigente y aprobada.
- **Action:** verbo permitido (create, submit, review, approve, reject, release, execute, reconcile, audit).
- **Object:** tipo e ID del objeto y versión exacta.
- **Scope:** activo, representación, red, venue, cuenta, actividad, jurisdicción, tamaño y finalidad.
- **Environment:** development, test, staging o production.
- **Context:** estado del workflow, riesgo, conflicto, quorum, dual control y segregación.
- **Time:** vigencia de delegación, sesión y autorización.
- **Policy:** versión de política y reglas aplicadas.

Controles mínimos propuestos:
1. RBAC para funciones base y ABAC para contexto, scope, cuantía, entorno y estado.
2. Deny-by-default y comprobación de segregación en servidor, no solo interfaz.
3. autorización por acción + objeto + scope + entorno + vigencia;
4. doble autorización independiente para actos críticos;
5. identidad de servicio propia por componente, credenciales no compartidas y secretos gestionados;
6. service-to-service authorization con audiencias y scopes limitados;
7. separación entre permisos de lectura, escritura, aprobación, liberación, ejecución y administración;
8. logs firmados/append-only, sincronización temporal y exportación a un dominio de auditoría independiente;
9. recertificación periódica de accesos y revisión inmediata ante cambios de función;
10. joiner/mover/leaver: alta aprobada, cambio de rol con revocación de privilegios previos y baja oportuna;
11. detección de autoaprobación, colusión simple, delegaciones vencidas y cuentas de emergencia;
12. pruebas de autorización negativas y pruebas de bypass de API.

El sistema debe rechazar una aprobación si el aprobador es el mismo principal que originó la solicitud cuando la política exige independencia, aunque use otra sesión o cuenta vinculada. La correlación de identidad debe considerar delegaciones, grupos y relaciones de control conocidas.

## 12. Interfaces con X-06 y X-07

### 12.1 Composición DECISION (X-06)

- El compositor es consumidor determinista de inputs inmutables; carece de autoridad organizativa para aprobarlos.
- La decisión registra quién preparó, quién aprobó, política/versiones, evidencias, causas, scope, abstenciones y dissent.
- DECISION no altera RiskDomainAssessment, VetoDecision, AE u ORE.
- Un resultado técnico no favorable no puede ser convertido manualmente en favorable sin nuevo assessment y workflow de aprobación.

### 12.2 Lifecycle e invalidación (X-07)

- Cualquier actor autorizado puede reportar una señal o solicitar reevaluación; reportar no equivale a invalidar o levantar restricciones.
- Lifecycle Owner coordina el evento y la propagación; los owners de dominio determinan impacto en sus assessments.
- El levantamiento de veto sigue el flujo VET-03 y la aprobación de riesgo; nunca es una acción autónoma del lifecycle engine.
- Cualquier cierre de incidente conserva evidencia, actores, dependencias, revisiones y decisión posterior.
- Los eventos retroactivos y los cambios de policy no reescriben la historia ni producen liberación automática.

## 13. Supervisión y aseguramiento

- Recertificar accesos y asignaciones de roles con periodicidad basada en riesgo y ante cada cambio de función.
- Revisar conflictos de interés antes de asignar casos sensibles y periódicamente.
- Medir autoaprobaciones bloqueadas, excepciones abiertas/vencidas, break-glass, conflictos, overrides, acciones fuera de horario, accesos privilegiados y retrasos de revisión.
- Muestrear decisiones, vetos, liberaciones, operaciones y reconciliaciones.
- Mantener un registro de hallazgos, remediación, owner, due date y evidencia de cierre.
- Internal Audit evalúa diseño y eficacia sin asumir ownership del control.
- La dirección recibe reporting agregado de incumplimientos, excepciones, riesgo residual y concentración de privilegios.

## 14. Escenarios de aceptación propuestos

1. Un productor de evidencia no puede aprobar su propia evidencia crítica.
2. Un analista no puede aprobar en solitario su propio assessment material.
3. Un Risk Analyst no puede aprobar un veto que propuso cuando se exige separación.
4. Un Veto Proposer no puede liberar el mismo veto.
5. Un veto activo no puede levantarse por TTL, silencio, cambio de turno o evento automático.
6. Una señal de incidente puede abrir revisión sin permitir que el informante declare por sí solo el hecho probado.
7. Una decisión favorable AE no autoriza una ruta ORE ni una orden.
8. Un Selection Analyst no puede aprobar unilateralmente su propia recomendación material.
9. Un solicitante de excepción no puede aprobarla ni autoextenderla.
10. Una excepción expirada deja de habilitar la acción sin intervención manual.
11. Un trader no puede autorizar su propia orden cuando aplica dual control.
12. Un trader no puede reconciliar unilateralmente su propia operación.
13. Un administrador IAM no puede otorgarse permisos de negocio sin aprobación independiente y log externo.
14. Un administrador de logs no puede borrar o reescribir eventos sin detección.
15. Un desarrollador de modelo no puede emitir su validación independiente.
16. Una persona recusada no puede votar ni aprobar el caso.
17. Una delegación vencida o fuera de scope no habilita una acción.
18. Una cuenta de servicio no puede heredar permisos humanos ni ejecutar aprobaciones reservadas a personas.
19. Break-glass produce alerta, expiración y revisión posterior.
20. La misma persona detrás de dos cuentas vinculadas sigue detectándose como conflicto de SoD.
21. Un consumidor DECISION no puede mutar el assessment fuente.
22. Internal Audit puede inspeccionar evidencia y logs sin poder cambiar decisiones operativas.

Estos son criterios de diseño. No se han convertido en pruebas ejecutables ni se han ejecutado.

## 15. Invariantes

- Ningún actor aprueba en solitario su propio trabajo material cuando la política exige independencia.
- Ningún veto se libera sin evidencia de remediación, revisión y autoridad competente.
- Ninguna excepción se aprueba por su solicitante ni supera restricciones no dispensables.
- Ninguna decisión analítica equivale a autorización operativa o transaccional.
- Ningún operador controla unilateralmente ejecución, conciliación y auditoría de la misma operación.
- Ningún administrador técnico tiene autoridad implícita de inversión o riesgo.
- Ningún sistema automático crea autoridad organizativa por ausencia de objeción.
- Toda autorización es contextual, limitada, temporal y trazable.
- Toda concentración de funciones requiere evaluación y controles compensatorios explícitos.
- Auditoría no diseña, opera ni aprueba el control que luego debe auditar.

## 16. Decisiones pendientes para aprobación institucional

1. Designar órgano de gobierno y autoridades accountable reales.
2. Definir clases de criticidad y qué decisiones exigen doble aprobación o quorum.
3. Aprobar límites de delegación y decisiones no delegables.
4. Definir autoridad y procedimiento para vetos legales, de seguridad, custodia, liquidez y riesgo.
5. Establecer independencia mínima y plazos de revisión para equipos pequeños.
6. Determinar frecuencia de recertificación y retención de logs según jurisdicción.
7. Definir el modelo de identidad vinculada para detectar conflictos entre cuentas y servicios.
8. Aprobar política de excepciones, break-glass y controles compensatorios.
9. Definir líneas de reporte y acceso de Internal Audit.
10. Mapear roles a requisitos IIM y contratos técnicos solo después de aprobación.

## 17. Criterios de aceptación del documento

- RACI cubre el ciclo de vida analítico, decisional, de cartera, ejecución y validación.
- Cada actividad crítica tiene un A único o un órgano colegiado con autoridad/quorum inequívocos.
- Incompatibilidades SoD cubren evidencia, riesgo, veto, decisión, excepciones, trading, custodia, conciliación, modelos, IAM y auditoría.
- Autoridad para emitir, revisar y levantar vetos está diferenciada.
- Excepciones, recusaciones, delegaciones, emergencias y equipos pequeños tienen controles explícitos.
- RACI se distingue de RBAC/ABAC, autenticación y enforcement.
- Interfaces con X-06 y X-07 no permiten mutación ni levantamiento automático.
- Escenarios negativos y positivos son suficientemente precisos para futura suite de conformidad.
- Revisión por Governance, Risk, Operations, Security, Legal/Compliance, Architecture y Audit documentada.

## 18. Limitaciones y estado

Revisión documental asistida y propuesta de diseño. No se entrevistó a responsables, no se verificó estructura legal/organizativa, no se asignaron personas, no se revisaron contratos laborales o de custodia, no se configuró IAM/RBAC/ABAC, no se ejecutaron pruebas ni auditorías independientes.

**Estado final: DRAFT / REVIEW_REQUIRED / NOT_APPROVED / NOT_IMPLEMENTED / NOT_VALIDATED.**

No modificar `requirements/requirements-register.md`, requisitos IIM, schemas o código como consecuencia de este documento hasta recibir aprobación explícita y resolver los comentarios de revisión.
